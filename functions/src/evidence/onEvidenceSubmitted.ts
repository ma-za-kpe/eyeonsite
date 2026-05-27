import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { haversine } from "../matching/haversine";
import {
  analyzePhotoWithVision,
  photoGcsUri,
  VisionPhotoResult,
  visionScoreFromResults,
} from "./visionQuality";

const REGION = "africa-south1";
const MIN_PHOTOS = 3;
const GPS_TOLERANCE_KM = 0.5; // photos must be within 500 m of task location
const SPREAD_THRESHOLD_KM = 0.01; // 10 m — minimum movement to earn full spread score

// Score weights when Cloud Vision results are available.
const W_COUNT_VISION = 25;
const W_GPS_VISION = 40;
const W_SPREAD_VISION = 15;
const W_VISION = 20;

// Score weights when Vision is unavailable (original behaviour).
const W_COUNT = 30;
const W_GPS = 50;
const W_SPREAD = 20;

interface PhotoPoint {
  latitude: number;
  longitude: number;
}

interface TaskData {
  clientUid?: string;
  matchedInspectorId?: string;
  location?: { latitude: number; longitude: number };
}

/**
 * Pure quality-score function, exported for unit testing.
 *
 * Without visionScore (undefined) — original weights (0–100):
 *   30 pts  photo count  — min(count, MIN_PHOTOS) / MIN_PHOTOS × 30
 *   50 pts  GPS proximity — fraction on-site × 50 (no task location → 25)
 *   20 pts  spatial spread — max inter-photo distance / SPREAD_THRESHOLD × 20
 *
 * With visionScore (0-1) — Vision-augmented weights (0–100):
 *   25 pts  photo count
 *   40 pts  GPS proximity (no task location → 20)
 *   15 pts  spatial spread
 *   20 pts  Vision quality (avg appropriate + colour-variety score)
 *
 * gpsValid = true when ≥ 80 % of photos are within GPS_TOLERANCE_KM of task,
 *            or when no task location is available (can't validate).
 */
export function computeQualityScore(
  photos: PhotoPoint[],
  taskLat?: number,
  taskLng?: number,
  visionScore?: number,
): { score: number; gpsValid: boolean } {
  if (photos.length === 0) return { score: 0, gpsValid: false };

  const withVision = visionScore !== undefined;
  const wCount = withVision ? W_COUNT_VISION : W_COUNT;
  const wGps = withVision ? W_GPS_VISION : W_GPS;
  const wSpread = withVision ? W_SPREAD_VISION : W_SPREAD;

  const countScore = (Math.min(photos.length, MIN_PHOTOS) / MIN_PHOTOS) * wCount;

  let gpsScore: number;
  let gpsValid: boolean;
  if (taskLat !== undefined && taskLng !== undefined) {
    const onSite = photos.filter(
      (p) => haversine(p.latitude, p.longitude, taskLat, taskLng) <= GPS_TOLERANCE_KM,
    );
    const ratio = onSite.length / photos.length;
    gpsScore = ratio * wGps;
    gpsValid = ratio >= 0.8;
  } else {
    gpsScore = wGps / 2; // partial credit when task location unknown
    gpsValid = true;
  }

  let maxDist = 0;
  for (let i = 0; i < photos.length; i++) {
    for (let j = i + 1; j < photos.length; j++) {
      const d = haversine(
        photos[i].latitude, photos[i].longitude,
        photos[j].latitude, photos[j].longitude,
      );
      if (d > maxDist) maxDist = d;
    }
  }
  const spreadScore = Math.min(maxDist / SPREAD_THRESHOLD_KM, 1) * wSpread;
  const visionPts = withVision ? (visionScore as number) * W_VISION : 0;

  return {
    score: Math.round(countScore + gpsScore + spreadScore + visionPts),
    gpsValid,
  };
}

export const onEvidenceSubmitted = onDocumentCreated(
  { document: "evidence/{evidenceId}", region: REGION },
  async (event) => {
    const evidenceId = event.params.evidenceId;
    const raw = event.data?.data() as Record<string, unknown> | undefined;
    if (!raw) return;

    const taskId = typeof raw["taskId"] === "string" ? raw["taskId"] : null;
    const inspectorUid = typeof raw["inspectorUid"] === "string" ? raw["inspectorUid"] : null;
    if (!taskId || !inspectorUid) {
      console.error(`evidence/${evidenceId}: missing taskId or inspectorUid`);
      return;
    }

    // Idempotency guard — skip if already processed.
    if (typeof raw["qualityScore"] === "number") return;

    const db = admin.firestore();
    const taskRef = db.doc(`tasks/${taskId}`);
    const taskSnap = await taskRef.get();
    const task = taskSnap.data() as TaskData | undefined;

    if (!task) {
      console.error(`evidence/${evidenceId}: task ${taskId} not found`);
      return;
    }

    if (task.matchedInspectorId !== inspectorUid) {
      console.error(`evidence/${evidenceId}: inspector ${inspectorUid} not matched to ${taskId}`);
      return;
    }

    const rawPhotos = Array.isArray(raw["photos"]) ? raw["photos"] as unknown[] : [];
    const photos: PhotoPoint[] = rawPhotos
      .filter((p): p is Record<string, unknown> => typeof p === "object" && p !== null)
      .map((p) => ({
        latitude: typeof p["latitude"] === "number" ? p["latitude"] : 0,
        longitude: typeof p["longitude"] === "number" ? p["longitude"] : 0,
      }));

    const taskLat = task.location?.latitude;
    const taskLng = task.location?.longitude;

    // Run Cloud Vision on all photos in parallel; degrade gracefully on failure.
    const visionResults = await runVisionAnalysis(taskId, rawPhotos);
    const visionScore = visionResults.length > 0
      ? visionScoreFromResults(visionResults)
      : undefined;

    const { score, gpsValid } = computeQualityScore(photos, taskLat, taskLng, visionScore);
    const now = Date.now();

    await Promise.all([
      db.doc(`evidence/${evidenceId}`).update({
        qualityScore: score,
        gpsValid,
        visionResults: visionResults.map((r) => ({
          photoId: r.photoId,
          appropriate: r.appropriate,
          colorVariety: r.colorVariety,
        })),
        status: "SUBMITTED",
        processedAtMs: now,
      }),
      taskRef.update({
        status: "EVIDENCE_SUBMITTED",
        evidenceId,
        evidenceSubmittedAtMs: now,
        evidenceQualityScore: score,
      }),
    ]);

    if (task.clientUid) {
      await notifyClient(db, admin.messaging(), task.clientUid, taskId, score);
    }

    console.log(`evidence/${evidenceId} processed — task=${taskId} score=${score} gpsValid=${gpsValid}`);
  },
);

/**
 * Calls Cloud Vision API for each photo whose ID is present in the photos array.
 * Errors for individual photos are caught and logged; they do not abort the batch.
 */
async function runVisionAnalysis(
  taskId: string,
  rawPhotos: unknown[],
): Promise<VisionPhotoResult[]> {
  const photoIds = rawPhotos
    .filter((p): p is Record<string, unknown> => typeof p === "object" && p !== null)
    .map((p) => (typeof p["id"] === "string" ? p["id"] : null))
    .filter((id): id is string => id !== null);

  const settled = await Promise.allSettled(
    photoIds.map((id) =>
      analyzePhotoWithVision(id, photoGcsUri(taskId, id)),
    ),
  );

  const results: VisionPhotoResult[] = [];
  for (const outcome of settled) {
    if (outcome.status === "fulfilled") {
      results.push(outcome.value);
    } else {
      console.warn(`Vision analysis failed for a photo in task ${taskId}:`, outcome.reason);
    }
  }
  return results;
}

async function notifyClient(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  clientUid: string,
  taskId: string,
  qualityScore: number,
): Promise<void> {
  const snap = await db.doc(`users/${clientUid}`).get();
  const token = (snap.data() as { fcmToken?: string } | undefined)?.fcmToken;
  if (!token) return;

  const label = qualityScore >= 80 ? "excellent" : qualityScore >= 60 ? "good" : "needs review";
  await messaging.send({
    token,
    notification: {
      title: "Evidence ready for your review",
      body: `Inspector submitted photos. Quality: ${label}. Tap to review.`,
    },
    data: { taskId, type: "EVIDENCE_SUBMITTED", qualityScore: String(qualityScore) },
  });
}
