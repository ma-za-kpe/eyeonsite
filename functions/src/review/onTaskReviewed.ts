import * as admin from "firebase-admin";
import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { sendAndCleanup } from "../utils/fcm";

const REVIEW_TERMINAL_STATUSES = new Set([
  "ACCEPTED",
  "DISPUTED",
  "REVISION_REQUESTED",
]);

export const onTaskReviewed = onDocumentUpdated(
  { document: "tasks/{taskId}", region: "africa-south1" },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after) return;

    const newStatus = String(after.status ?? "");
    if (before.status === newStatus) return;
    if (!REVIEW_TERMINAL_STATUSES.has(newStatus)) return;

    const db = admin.firestore();
    const messaging = admin.messaging();
    const taskId = event.params.taskId;
    const inspectorUid = String(after.inspectorUid ?? "");

    if (newStatus === "REVISION_REQUESTED") {
      await handleRevisionRequested(db, messaging, taskId, inspectorUid, after);
    } else if (newStatus === "DISPUTED") {
      await handleDisputed(db, messaging, taskId, inspectorUid, after);
    }
    // ACCEPTED: handled downstream by releaseEscrow Cloud Function
  },
);

async function resolveInspectorToken(
  db: admin.firestore.Firestore,
  inspectorUid: string,
): Promise<{ token: string; docPath: string } | null> {
  if (!inspectorUid) return null;
  const docPath = `inspectors/${inspectorUid}`;
  const snap = await db.doc(docPath).get();
  const token = snap.data()?.fcmToken as string | undefined;
  return token ? { token, docPath } : null;
}

async function handleRevisionRequested(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  taskId: string,
  inspectorUid: string,
  taskData: admin.firestore.DocumentData,
): Promise<void> {
  // Roll task status back to IN_PROGRESS so inspector can re-submit evidence.
  await db.doc(`tasks/${taskId}`).update({ status: "IN_PROGRESS" });

  const recipient = await resolveInspectorToken(db, inspectorUid);
  if (!recipient) return;

  const notes = String(taskData.revisionNotes ?? "");
  await sendAndCleanup(
    db,
    messaging,
    [recipient],
    {
      title: "Revision Requested",
      body: notes || "The client needs additional evidence for this task.",
    },
    { taskId, type: "REVISION_REQUESTED" },
  );
}

async function handleDisputed(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  taskId: string,
  inspectorUid: string,
  taskData: admin.firestore.DocumentData,
): Promise<void> {
  const recipient = await resolveInspectorToken(db, inspectorUid);
  if (!recipient) return;

  await sendAndCleanup(
    db,
    messaging,
    [recipient],
    {
      title: "Evidence Disputed",
      body: "The client has disputed the submitted evidence. Our team will review within 24 hours.",
    },
    {
      taskId,
      type: "DISPUTED",
      reason: String(taskData.disputeReason ?? "OTHER"),
    },
  );
}
