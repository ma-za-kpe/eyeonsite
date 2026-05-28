import * as admin from "firebase-admin";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { sendAndCleanup } from "../utils/fcm";

interface AcceptableTaskData {
  status?: unknown;
  pinnedInspectorIds?: unknown;
}

export function buildTaskAcceptanceUpdate(
  task: AcceptableTaskData | undefined,
  inspectorUid: string,
  matchedAtMs: number,
): { update: Record<string, unknown>; losers: string[] } {
  if (!task) throw new HttpsError("not-found", "Task does not exist");
  if (task.status !== "POSTED") {
    throw new HttpsError("failed-precondition", "Task is no longer available");
  }

  const pinnedIds = Array.isArray(task.pinnedInspectorIds) ?
    task.pinnedInspectorIds.filter((uid): uid is string => typeof uid === "string") :
    [];

  if (!pinnedIds.includes(inspectorUid)) {
    throw new HttpsError("permission-denied", "Inspector was not matched to this task");
  }

  return {
    update: {
      status: "INSPECTOR_MATCHED",
      matchedInspectorId: inspectorUid,
      matchedAtMs,
    },
    losers: pinnedIds.filter((uid) => uid !== inspectorUid),
  };
}

export const acceptTask = onCall(
  { region: "africa-south1", enforceAppCheck: true },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Must be signed in to accept a task");
    }

    const rawTaskId = (request.data as { taskId?: unknown }).taskId;
    const taskId = typeof rawTaskId === "string" ? rawTaskId.trim() : "";
    if (!taskId) {
      throw new HttpsError("invalid-argument", "taskId is required");
    }

    const db = admin.firestore();
    const inspectorUid = request.auth.uid;
    const taskRef = db.doc(`tasks/${taskId}`);
    const inspectorRef = db.doc(`inspectors/${inspectorUid}`);

    let losers: string[] = [];

    await db.runTransaction(async (tx) => {
      const snap = await tx.get(taskRef);
      const inspectorSnap = await tx.get(inspectorRef);
      const inspector = inspectorSnap.data();

      if (!inspector) {
        throw new HttpsError("permission-denied", "Inspector profile does not exist");
      }
      if (inspector["available"] !== true) {
        throw new HttpsError("failed-precondition", "Inspector is not available");
      }

      const matchedAtMs = Date.now();
      const result = buildTaskAcceptanceUpdate(
        snap.data() as AcceptableTaskData | undefined,
        inspectorUid,
        matchedAtMs,
      );

      losers = result.losers;
      tx.update(taskRef, result.update);
      tx.update(inspectorRef, {
        available: false,
        activeTaskId: taskId,
        activeTaskStartedAtMs: matchedAtMs,
      });
    });

    // Notify other pinged inspectors that the task was taken.
    if (losers.length > 0) {
      await notifyTaskTaken(db, admin.messaging(), taskId, losers);
    }

    console.log(`Task ${taskId} accepted by inspector ${inspectorUid}`);
    return { success: true };
  },
);

async function notifyTaskTaken(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  taskId: string,
  inspectorUids: string[],
): Promise<void> {
  const snaps = await Promise.all(
    inspectorUids.map((uid) => db.doc(`inspectors/${uid}`).get()),
  );
  const recipients = snaps
    .map((s, i) => {
      const token = (s.data() as { fcmToken?: string } | undefined)?.fcmToken;
      return token ? { docPath: `inspectors/${inspectorUids[i]}`, token } : null;
    })
    .filter((r): r is NonNullable<typeof r> => r !== null);

  await sendAndCleanup(
    db,
    messaging,
    recipients,
    { title: "Task taken", body: "Another inspector accepted this task first" },
    { taskId, type: "TASK_TAKEN" },
  );
}
