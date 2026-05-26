import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { haversine } from "../matching/haversine";

const REGION = "africa-south1";
const MIN_PHOTOS = 3;
const GPS_TOLERANCE_KM = 0.5; // photos must be within 500 m of task location
const SPREAD_THRESHOLD_KM = 0.01; // 10 m — minimum movement to earn full spread score

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
 * Score breakdown (0–100):
 *   30 pts  photo count  — min(count, MIN_PHOTOS) / MIN_PHOTOS × 30
 *   50 pts  GPS proximity — fraction on-site × 50 (no task location → 25)
 *   20 pts  spatial spread — max inter-photo distance / SPREAD_THRESHOLD × 20
 *
 * gpsValid = true when ≥ 80 % of photos are within GPS_TOLERANCE_KM of task,
 *            or when no task location is available (can't validate).
 */
export function computeQualityScore(
  photos: PhotoPoint[],
  taskLat?: number,
  taskLng?: number,
): { score: number; gpsValid: boolean } {
  if (photos.length === 0) return { score: 0, gpsValid: false };

  const countScore = (Math.min(photos.length, MIN_PHOTOS) / MIN_PHOTOS) * 30;

  let gpsScore: number;
  let gpsValid: boolean;
  if (taskLat !== undefined && taskLng !== undefined) {
    const onSite = photos.filter(
      (p) => haversine(p.latitude, p.longitude, taskLat, taskLng) <= GPS_TOLERANCE_KM,
    );
    const ratio = onSite.length / photos.length;
    gpsScore = ratio * 50;
    gpsValid = ratio >= 0.8;
  } else {
    gpsScore = 25;
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
  const spreadScore = Math.min(maxDist / SPREAD_THRESHOLD_KM, 1) * 20;

  return {
    score: Math.round(countScore + gpsScore + spreadScore),
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
    const { score, gpsValid } = computeQualityScore(photos, taskLat, taskLng);
    const now = Date.now();

    await Promise.all([
      db.doc(`evidence/${evidenceId}`).update({
        qualityScore: score,
        gpsValid,
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
