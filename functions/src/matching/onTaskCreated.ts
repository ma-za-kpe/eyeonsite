import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { haversine } from "./haversine";
import { InspectorCandidate, InspectorDocument, TaskDocument } from "./types";

const MAX_CANDIDATES = 3;
const EXPIRY_MS = 30 * 60 * 1000; // 30 minutes

export async function queryAvailableInspectors(
  db: admin.firestore.Firestore,
): Promise<Array<{ uid: string; doc: InspectorDocument }>> {
  const snap = await db
    .collection("inspectors")
    .where("available", "==", true)
    .get();
  return snap.docs.map((d) => ({ uid: d.id, doc: d.data() as InspectorDocument }));
}

export function rankCandidates(
  taskLat: number,
  taskLng: number,
  radiusKm: number,
  inspectors: Array<{ uid: string; doc: InspectorDocument }>,
): InspectorCandidate[] {
  return inspectors
    .filter((i) => i.doc.location != null)
    .map((i) => ({
      uid: i.uid,
      fcmToken: i.doc.fcmToken,
      distanceKm: haversine(
        taskLat, taskLng,
        i.doc.location.latitude, i.doc.location.longitude,
      ),
    }))
    .filter((c) => c.distanceKm <= radiusKm)
    .sort((a, b) => a.distanceKm - b.distanceKm)
    .slice(0, MAX_CANDIDATES);
}

export async function sendTaskPings(
  messaging: admin.messaging.Messaging,
  taskId: string,
  task: TaskDocument,
  candidates: InspectorCandidate[],
): Promise<void> {
  const tokens = candidates.map((c) => c.fcmToken).filter((t): t is string => !!t);
  if (tokens.length === 0) return;

  const distanceStr = candidates[0].distanceKm.toFixed(1);
  await messaging.sendEachForMulticast({
    tokens,
    notification: {
      title: "New verification task nearby",
      body: `$${task.bountyUsd} — ${distanceStr} km away`,
    },
    data: { taskId, type: "TASK_PING" },
  });
}

export const onTaskCreated = onDocumentCreated(
  { document: "tasks/{taskId}", region: "africa-south1" },
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const task = snap.data() as TaskDocument;
    if (task.status !== "POSTED") return;

    const db = admin.firestore();
    const taskId = event.params.taskId;
    const taskLat = task.location.latitude;
    const taskLng = task.location.longitude;
    const radiusKm = task.locationRadiusKm ?? 10;

    const inspectors = await queryAvailableInspectors(db);
    const candidates = rankCandidates(taskLat, taskLng, radiusKm, inspectors);

    if (candidates.length === 0) {
      await snap.ref.update({ status: "EXPIRED" });
      console.log(`Task ${taskId}: no inspectors in radius — marked EXPIRED`);
      return;
    }

    await sendTaskPings(admin.messaging(), taskId, task, candidates);

    await snap.ref.update({
      pinnedInspectorIds: candidates.map((c) => c.uid),
      expiresAtMs: Date.now() + EXPIRY_MS,
    });

    console.log(`Task ${taskId}: pinged ${candidates.length} inspector(s)`);
  },
);
