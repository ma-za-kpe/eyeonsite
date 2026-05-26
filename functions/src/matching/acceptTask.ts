import * as admin from "firebase-admin";
import { onCall } from "firebase-functions/v2/https";
import { HttpsError } from "firebase-functions/v2/https";

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
  { region: "africa-south1" },
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
