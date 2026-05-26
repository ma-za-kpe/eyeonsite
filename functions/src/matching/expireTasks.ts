import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";

/** Sweeps POSTED tasks whose expiresAtMs is in the past and marks them EXPIRED. */
export const expireStaleTasks = onSchedule(
  { schedule: "every 5 minutes", region: "africa-south1" },
  async () => {
    const db = admin.firestore();
    const now = Date.now();

    const snap = await db
      .collection("tasks")
      .where("status", "==", "POSTED")
      .where("expiresAtMs", "<=", now)
      .get();

    if (snap.empty) return;

    const batch = db.batch();
    snap.docs.forEach((doc) => batch.update(doc.ref, { status: "EXPIRED" }));
    await batch.commit();

    await notifyExpiredClients(db, admin.messaging(), snap.docs);

    console.log(`Expired ${snap.size} stale task(s)`);
  },
);

async function notifyExpiredClients(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  taskDocs: admin.firestore.QueryDocumentSnapshot[],
): Promise<void> {
  const tokens = taskDocs
    .map((d) => (d.data() as { clientFcmToken?: string }).clientFcmToken)
    .filter((t): t is string => !!t);

  if (tokens.length === 0) return;

  await messaging.sendEachForMulticast({
    tokens,
    notification: {
      title: "No inspector available",
      body: "Try posting your task again — more inspectors are on the way",
    },
    data: { type: "TASK_EXPIRED" },
  });
}
