import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { FcmRecipient, sendAndCleanup } from "../utils/fcm";
import { geohashQueryBounds } from "../utils/geohash";
import { haversine } from "./haversine";
import { InspectorCandidate, InspectorDocument, TaskDocument } from "./types";

const MAX_CANDIDATES = 3;
const EXPIRY_MS = 30 * 60 * 1000; // 30 minutes
const RATING_FACTOR = 0.5;  // 1 star ≈ 0.5 km advantage in ranking
const TIER_FACTOR = 1.0;    // each tier level ≈ 1 km advantage

export async function queryAvailableInspectors(
  db: admin.firestore.Firestore,
  taskLat: number,
  taskLng: number,
): Promise<Array<{ uid: string; doc: InspectorDocument }>> {
  const bounds = geohashQueryBounds(taskLat, taskLng);
  const queries = bounds.map(([start, end]) =>
    db
      .collection("inspectors")
      .where("available", "==", true)
      .where("geohash", ">=", start)
      .where("geohash", "<=", end)
      .get(),
  );
  const snaps = await Promise.all(queries);

  // Deduplicate by uid — geohash cells can overlap at borders
  const seen = new Set<string>();
  const results: Array<{ uid: string; doc: InspectorDocument }> = [];
  for (const snap of snaps) {
    for (const d of snap.docs) {
      if (!seen.has(d.id)) {
        seen.add(d.id);
        results.push({ uid: d.id, doc: d.data() as InspectorDocument });
      }
    }
  }
  return results;
}

export function rankCandidates(
  taskLat: number,
  taskLng: number,
  radiusKm: number,
  inspectors: Array<{ uid: string; doc: InspectorDocument }>,
): InspectorCandidate[] {
  return inspectors
    .filter((i) => i.doc.location != null)
    .map((i) => {
      const distanceKm = haversine(
        taskLat, taskLng,
        i.doc.location.latitude, i.doc.location.longitude,
      );
      const rating = i.doc.rating ?? 4.0;
      const tierLevel = i.doc.tierLevel ?? 1;
      const score = distanceKm - (rating - 4.0) * RATING_FACTOR - (tierLevel - 1) * TIER_FACTOR;
      return { uid: i.uid, fcmToken: i.doc.fcmToken, distanceKm, rating, tierLevel, score };
    })
    .filter((c) => c.distanceKm <= radiusKm)
    .sort((a, b) => a.score - b.score)
    .slice(0, MAX_CANDIDATES)
    .map(({ score: _score, ...c }) => c);
}

export async function sendTaskPings(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  taskId: string,
  task: TaskDocument,
  candidates: InspectorCandidate[],
): Promise<void> {
  const recipients: FcmRecipient[] = candidates
    .filter((c): c is InspectorCandidate & { fcmToken: string } => !!c.fcmToken)
    .map((c) => ({ docPath: `inspectors/${c.uid}`, token: c.fcmToken }));

  const distanceStr = candidates[0]?.distanceKm.toFixed(1) ?? "?";
  await sendAndCleanup(
    db,
    messaging,
    recipients,
    { title: "New verification task nearby", body: `$${task.bountyUsd} - ${distanceStr} km away` },
    { taskId, type: "TASK_PING" },
  );
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

    const inspectors = await queryAvailableInspectors(db, taskLat, taskLng);
    const candidates = rankCandidates(taskLat, taskLng, radiusKm, inspectors);

    if (candidates.length === 0) {
      await snap.ref.update({ status: "EXPIRED" });
      console.log(`Task ${taskId}: no inspectors in radius - marked EXPIRED`);
      return;
    }

    await snap.ref.update({
      pinnedInspectorIds: candidates.map((c) => c.uid),
      expiresAtMs: Date.now() + EXPIRY_MS,
    });

    await sendTaskPings(db, admin.messaging(), taskId, task, candidates);

    console.log(`Task ${taskId}: pinged ${candidates.length} inspector(s)`);
  },
);
