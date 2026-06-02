import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { FcmRecipient, sendAndCleanup } from "../utils/fcm";
import { geohashQueryBounds } from "../utils/geohash";
import { h3CandidateCellsForTask, h3CoverageForPoint } from "../utils/h3";
import { haversine } from "./haversine";
import { InspectorCandidate, InspectorDocument, TaskDocument } from "./types";

const MAX_CANDIDATES = 3;
const EXPIRY_MS = 30 * 60 * 1000;
const RATING_FACTOR = 0.5;
const TIER_FACTOR = 1.0;
const FIRESTORE_IN_LIMIT = 30;

type InspectorResult = { uid: string; doc: InspectorDocument };

function collectInspectorResults(snaps: admin.firestore.QuerySnapshot[]): InspectorResult[] {
  const seen = new Set<string>();
  const results: InspectorResult[] = [];
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

function chunk<T>(values: T[], size: number): T[][] {
  const chunks: T[][] = [];
  for (let start = 0; start < values.length; start += size) {
    chunks.push(values.slice(start, start + size));
  }
  return chunks;
}

async function queryInspectorsByH3(
  db: admin.firestore.Firestore,
  cells: string[],
): Promise<InspectorResult[]> {
  if (cells.length === 0) return [];
  const queries = chunk(cells, FIRESTORE_IN_LIMIT).map((cellChunk) =>
    db
      .collection("inspectors")
      .where("available", "==", true)
      .where("h3Res9", "in", cellChunk)
      .get(),
  );
  return collectInspectorResults(await Promise.all(queries));
}

async function queryInspectorsByGeohash(
  db: admin.firestore.Firestore,
  taskLat: number,
  taskLng: number,
): Promise<InspectorResult[]> {
  const bounds = geohashQueryBounds(taskLat, taskLng);
  const queries = bounds.map(([start, end]) =>
    db
      .collection("inspectors")
      .where("available", "==", true)
      .where("geohash", ">=", start)
      .where("geohash", "<=", end)
      .get(),
  );
  return collectInspectorResults(await Promise.all(queries));
}

export async function queryAvailableInspectors(
  db: admin.firestore.Firestore,
  taskLat: number,
  taskLng: number,
  task: Pick<TaskDocument, "h3Res9" | "h3NeighborRes9"> = {},
): Promise<InspectorResult[]> {
  const h3Results = await queryInspectorsByH3(db, h3CandidateCellsForTask(task, taskLat, taskLng));
  if (h3Results.length > 0) {
    return h3Results;
  }
  return queryInspectorsByGeohash(db, taskLat, taskLng);
}

export function rankCandidates(
  taskLat: number,
  taskLng: number,
  radiusKm: number,
  inspectors: InspectorResult[],
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
    const h3Coverage = h3CoverageForPoint(taskLat, taskLng);

    const inspectors = await queryAvailableInspectors(db, taskLat, taskLng, task);
    const candidates = rankCandidates(taskLat, taskLng, radiusKm, inspectors);

    if (candidates.length === 0) {
      await snap.ref.update({ ...h3Coverage, status: "EXPIRED" });
      console.log(`Task ${taskId}: no inspectors in radius - marked EXPIRED`);
      return;
    }

    await snap.ref.update({
      ...h3Coverage,
      pinnedInspectorIds: candidates.map((c) => c.uid),
      expiresAtMs: Date.now() + EXPIRY_MS,
    });

    await sendTaskPings(db, admin.messaging(), taskId, task, candidates);

    console.log(`Task ${taskId}: pinged ${candidates.length} inspector(s)`);
  },
);
