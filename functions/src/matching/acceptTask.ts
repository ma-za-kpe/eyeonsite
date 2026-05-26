import * as admin from "firebase-admin";
import { onCall } from "firebase-functions/v2/https";
import { HttpsError } from "firebase-functions/v2/https";

export const acceptTask = onCall(
  { region: "africa-south1" },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Must be signed in to accept a task");
    }

    const { taskId } = request.data as { taskId: string };
    if (!taskId) {
      throw new HttpsError("invalid-argument", "taskId is required");
    }

    const db = admin.firestore();
    const inspectorUid = request.auth.uid;
    const taskRef = db.doc(`tasks/${taskId}`);

    let pinnedIds: string[] = [];

    await db.runTransaction(async (tx) => {
      const snap = await tx.get(taskRef);
      const task = snap.data();

      if (!task) throw new HttpsError("not-found", "Task does not exist");
      if (task["status"] !== "POSTED") {
        throw new HttpsError("failed-precondition", "Task is no longer available");
      }

      pinnedIds = (task["pinnedInspectorIds"] as string[] | undefined) ?? [];

      tx.update(taskRef, {
        status: "INSPECTOR_MATCHED",
        matchedInspectorId: inspectorUid,
        matchedAtMs: Date.now(),
      });
    });

    // Notify other pinged inspectors that the task was taken
    const losers = pinnedIds.filter((uid) => uid !== inspectorUid);
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
  const tokens = snaps
    .map((s) => (s.data() as { fcmToken?: string } | undefined)?.fcmToken)
    .filter((t): t is string => !!t);

  if (tokens.length === 0) return;

  await messaging.sendEachForMulticast({
    tokens,
    notification: {
      title: "Task taken",
      body: "Another inspector accepted this task first",
    },
    data: { taskId, type: "TASK_TAKEN" },
  });
}
