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
const GPS_TOLERANCE_KM = 0.5;
const SPREAD_THRESHOLD_KM = 0.01;
const EXIF_GPS_TOLERANCE_KM = 0.2;
const LOW_ACCURACY_THRESHOLD_METERS = 50;
const HIGH_ACCURACY_THRESHOLD_METERS = 100;
const TIMESTAMP_DRIFT_THRESHOLD_MS = 5 * 60 * 1000;
const FUTURE_CAPTURE_THRESHOLD_MS = 2 * 60 * 1000;
const IMPOSSIBLE_SPEED_KMH = 120;

const W_COUNT_VISION = 25;
const W_GPS_VISION = 40;
const W_SPREAD_VISION = 15;
const W_VISION = 20;

const W_COUNT = 30;
const W_GPS = 50;
const W_SPREAD = 20;

interface PhotoPoint {
  latitude: number;
  longitude: number;
}

export interface EvidenceExifInput {
  latitude?: number;
  longitude?: number;
  capturedAtMs?: number;
}

export interface EvidencePhotoInput extends PhotoPoint {
  id?: string;
  accuracyMeters?: number;
  isMockLocation?: boolean;
  capturedAtMs?: number;
  capturedAtDeviceMs?: number;
  receivedAtServerMs?: number;
  exif?: EvidenceExifInput;
  perceptualHash?: string;
}

interface TaskData {
  clientUid?: string;
  matchedInspectorId?: string;
  location?: { latitude: number; longitude: number };
}

export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";
export type ContentRisk = "UNKNOWN" | RiskLevel;
export type ProvenanceStatus = "UNVERIFIED" | "PASSED" | "FAILED";
export type AttestationVerdict = "UNKNOWN" | "PASSED" | "FAILED" | "UNAVAILABLE";

export type FraudSignalType =
  | "MOCK_LOCATION"
  | "LOW_ACCURACY"
  | "GPS_EXIF_MISMATCH"
  | "TIMESTAMP_DRIFT"
  | "IMPOSSIBLE_TRAVEL"
  | "PHASH_DUPLICATE"
  | "GPS_OFF_SITE"
  | "CONTENT_UNSAFE"
  | "ATTESTATION_FAILED";

export interface FraudSignal {
  type: FraudSignalType;
  severity: RiskLevel;
  message: string;
  photoId?: string;
}

export interface EvidenceTrustOptions {
  visionResults?: VisionPhotoResult[];
  attestationVerdict?: AttestationVerdict;
  receivedAtServerMs?: number;
}

export interface EvidenceTrustAnalysis {
  qualityScore: number;
  gpsValid: boolean;
  fraudRisk: RiskLevel;
  contentRisk: ContentRisk;
  provenanceStatus: ProvenanceStatus;
  attestationVerdict: AttestationVerdict;
  fraudSignals: FraudSignal[];
}

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
    gpsScore = wGps / 2;
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

export function analyzeEvidenceTrust(
  photos: EvidencePhotoInput[],
  taskLat?: number,
  taskLng?: number,
  options: EvidenceTrustOptions = {},
): EvidenceTrustAnalysis {
  const visionResults = options.visionResults ?? [];
  const visionScore = visionResults.length > 0 ? visionScoreFromResults(visionResults) : undefined;
  const { score, gpsValid } = computeQualityScore(photos, taskLat, taskLng, visionScore);
  const fraudSignals: FraudSignal[] = [];

  if (!gpsValid) {
    fraudSignals.push({
      type: "GPS_OFF_SITE",
      severity: "HIGH",
      message: "Less than 80 percent of submitted photos are within task GPS tolerance.",
    });
  }

  addPhotoIntegritySignals(fraudSignals, photos, options.receivedAtServerMs);
  addDuplicateHashSignals(fraudSignals, photos);
  addTravelSignals(fraudSignals, photos);

  const contentRisk = contentRiskFromVision(visionResults, fraudSignals);
  const attestationVerdict = options.attestationVerdict ?? "UNKNOWN";
  if (attestationVerdict === "FAILED") {
    fraudSignals.push({
      type: "ATTESTATION_FAILED",
      severity: "HIGH",
      message: "Device or app attestation failed.",
    });
  }

  const fraudRisk = riskFromSignals(fraudSignals);
  const provenanceStatus = provenanceFromSignals(attestationVerdict, fraudRisk, contentRisk);

  return {
    qualityScore: score,
    gpsValid,
    fraudRisk,
    contentRisk,
    provenanceStatus,
    attestationVerdict,
    fraudSignals,
  };
}

function addPhotoIntegritySignals(
  fraudSignals: FraudSignal[],
  photos: EvidencePhotoInput[],
  receivedAtServerMs?: number,
): void {
  for (const photo of photos) {
    if (photo.isMockLocation) {
      fraudSignals.push({
        type: "MOCK_LOCATION",
        severity: "HIGH",
        message: "Photo reports a mock location provider.",
        photoId: photo.id,
      });
    }

    if (photo.accuracyMeters !== undefined && photo.accuracyMeters > LOW_ACCURACY_THRESHOLD_METERS) {
      fraudSignals.push({
        type: "LOW_ACCURACY",
        severity: photo.accuracyMeters > HIGH_ACCURACY_THRESHOLD_METERS ? "HIGH" : "MEDIUM",
        message: `Location accuracy ${photo.accuracyMeters}m exceeds evidence threshold.`,
        photoId: photo.id,
      });
    }

    addExifSignals(fraudSignals, photo);
    addDeviceTimeSignals(fraudSignals, photo, receivedAtServerMs);
  }
}

function addExifSignals(fraudSignals: FraudSignal[], photo: EvidencePhotoInput): void {
  if (photo.exif?.latitude !== undefined && photo.exif.longitude !== undefined) {
    const exifDistanceKm = haversine(
      photo.latitude,
      photo.longitude,
      photo.exif.latitude,
      photo.exif.longitude,
    );
    if (exifDistanceKm > EXIF_GPS_TOLERANCE_KM) {
      fraudSignals.push({
        type: "GPS_EXIF_MISMATCH",
        severity: "HIGH",
        message: "Photo GPS and EXIF GPS are materially different.",
        photoId: photo.id,
      });
    }
  }

  const deviceTime = photo.capturedAtDeviceMs ?? photo.capturedAtMs;
  if (photo.exif?.capturedAtMs !== undefined && deviceTime !== undefined) {
    const driftMs = Math.abs(photo.exif.capturedAtMs - deviceTime);
    if (driftMs > TIMESTAMP_DRIFT_THRESHOLD_MS) {
      fraudSignals.push({
        type: "TIMESTAMP_DRIFT",
        severity: "MEDIUM",
        message: "EXIF timestamp differs from device capture time.",
        photoId: photo.id,
      });
    }
  }
}

function addDeviceTimeSignals(
  fraudSignals: FraudSignal[],
  photo: EvidencePhotoInput,
  receivedAtServerMs?: number,
): void {
  const deviceTime = photo.capturedAtDeviceMs ?? photo.capturedAtMs;
  const serverTime = photo.receivedAtServerMs ?? receivedAtServerMs;
  if (deviceTime === undefined || serverTime === undefined) return;
  if (deviceTime - serverTime > FUTURE_CAPTURE_THRESHOLD_MS) {
    fraudSignals.push({
      type: "TIMESTAMP_DRIFT",
      severity: "HIGH",
      message: "Device capture time is ahead of server receipt time.",
      photoId: photo.id,
    });
  }
}

function addDuplicateHashSignals(fraudSignals: FraudSignal[], photos: EvidencePhotoInput[]): void {
  const seen = new Set<string>();
  const duplicated = new Set<string>();
  for (const photo of photos) {
    if (!photo.perceptualHash) continue;
    if (seen.has(photo.perceptualHash)) duplicated.add(photo.perceptualHash);
    seen.add(photo.perceptualHash);
  }
  for (const hash of duplicated) {
    fraudSignals.push({
      type: "PHASH_DUPLICATE",
      severity: "MEDIUM",
      message: `Duplicate perceptual hash detected: ${hash}`,
    });
  }
}

function addTravelSignals(fraudSignals: FraudSignal[], photos: EvidencePhotoInput[]): void {
  const timed = photos
    .map((photo) => ({ photo, timeMs: photo.capturedAtDeviceMs ?? photo.capturedAtMs }))
    .filter((item): item is { photo: EvidencePhotoInput; timeMs: number } => item.timeMs !== undefined)
    .sort((a, b) => a.timeMs - b.timeMs);

  for (let i = 1; i < timed.length; i++) {
    const previous = timed[i - 1];
    const current = timed[i];
    const elapsedHours = (current.timeMs - previous.timeMs) / (60 * 60 * 1000);
    if (elapsedHours <= 0) continue;

    const distanceKm = haversine(
      previous.photo.latitude,
      previous.photo.longitude,
      current.photo.latitude,
      current.photo.longitude,
    );
    if (distanceKm / elapsedHours > IMPOSSIBLE_SPEED_KMH) {
      fraudSignals.push({
        type: "IMPOSSIBLE_TRAVEL",
        severity: "HIGH",
        message: "Photo sequence implies impossible travel speed.",
        photoId: current.photo.id,
      });
      return;
    }
  }
}

function contentRiskFromVision(
  visionResults: VisionPhotoResult[],
  fraudSignals: FraudSignal[],
): ContentRisk {
  if (visionResults.length === 0) return "UNKNOWN";
  if (visionResults.some((result) => !result.appropriate)) {
    fraudSignals.push({
      type: "CONTENT_UNSAFE",
      severity: "HIGH",
      message: "Cloud Vision marked one or more photos as unsafe.",
    });
    return "HIGH";
  }
  if (visionResults.some((result) => result.colorVariety < 0.5)) {
    return "MEDIUM";
  }
  return "LOW";
}

function riskFromSignals(signals: FraudSignal[]): RiskLevel {
  if (signals.some((signal) => signal.severity === "HIGH")) return "HIGH";
  if (signals.some((signal) => signal.severity === "MEDIUM")) return "MEDIUM";
  return "LOW";
}

function provenanceFromSignals(
  attestationVerdict: AttestationVerdict,
  fraudRisk: RiskLevel,
  contentRisk: ContentRisk,
): ProvenanceStatus {
  if (attestationVerdict === "FAILED" || fraudRisk === "HIGH" || contentRisk === "HIGH") {
    return "FAILED";
  }
  if (attestationVerdict === "PASSED" && fraudRisk === "LOW" && (contentRisk === "LOW" || contentRisk === "UNKNOWN")) {
    return "PASSED";
  }
  return "UNVERIFIED";
}

function readNumber(source: Record<string, unknown>, key: string): number | undefined {
  const value = source[key];
  return typeof value === "number" ? value : undefined;
}

function readBoolean(source: Record<string, unknown>, key: string): boolean | undefined {
  const value = source[key];
  return typeof value === "boolean" ? value : undefined;
}

function readString(source: Record<string, unknown>, key: string): string | undefined {
  const value = source[key];
  return typeof value === "string" ? value : undefined;
}

function readExif(source: Record<string, unknown>): EvidenceExifInput {
  const rawExif = source["exif"];
  const exif = typeof rawExif === "object" && rawExif !== null ? rawExif as Record<string, unknown> : {};
  return {
    latitude: readNumber(exif, "latitude") ?? readNumber(source, "exifLatitude"),
    longitude: readNumber(exif, "longitude") ?? readNumber(source, "exifLongitude"),
    capturedAtMs: readNumber(exif, "capturedAtMs") ?? readNumber(source, "exifTimestampMs"),
  };
}

function parseEvidencePhoto(raw: unknown): EvidencePhotoInput | null {
  if (typeof raw !== "object" || raw === null) return null;
  const source = raw as Record<string, unknown>;
  return {
    id: readString(source, "id"),
    latitude: readNumber(source, "latitude") ?? 0,
    longitude: readNumber(source, "longitude") ?? 0,
    accuracyMeters: readNumber(source, "accuracyMeters"),
    isMockLocation: readBoolean(source, "isMockLocation") ?? false,
    capturedAtMs: readNumber(source, "capturedAtMs"),
    capturedAtDeviceMs: readNumber(source, "capturedAtDeviceMs"),
    receivedAtServerMs: readNumber(source, "receivedAtServerMs"),
    exif: readExif(source),
    perceptualHash: readString(source, "perceptualHash"),
  };
}

function parseAttestationVerdict(raw: unknown): AttestationVerdict {
  if (raw === "PASSED" || raw === "FAILED" || raw === "UNAVAILABLE") return raw;
  return "UNKNOWN";
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
    const photos = rawPhotos
      .map(parseEvidencePhoto)
      .filter((photo): photo is EvidencePhotoInput => photo !== null);

    const taskLat = task.location?.latitude;
    const taskLng = task.location?.longitude;
    const now = Date.now();

    const visionResults = await runVisionAnalysis(taskId, rawPhotos);
    const trust = analyzeEvidenceTrust(photos, taskLat, taskLng, {
      visionResults,
      attestationVerdict: parseAttestationVerdict(raw["attestationVerdict"]),
      receivedAtServerMs: now,
    });

    await Promise.all([
      db.doc(`evidence/${evidenceId}`).update({
        qualityScore: trust.qualityScore,
        gpsValid: trust.gpsValid,
        fraudRisk: trust.fraudRisk,
        contentRisk: trust.contentRisk,
        provenanceStatus: trust.provenanceStatus,
        attestationVerdict: trust.attestationVerdict,
        fraudSignals: trust.fraudSignals,
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
        evidenceQualityScore: trust.qualityScore,
        evidenceFraudRisk: trust.fraudRisk,
        evidenceProvenanceStatus: trust.provenanceStatus,
      }),
    ]);

    if (task.clientUid) {
      await notifyClient(db, admin.messaging(), task.clientUid, taskId, trust.qualityScore);
    }

    console.log(
      `evidence/${evidenceId} processed: task=${taskId} score=${trust.qualityScore} fraud=${trust.fraudRisk}`,
    );
  },
);

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
