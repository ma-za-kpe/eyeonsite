import { ImageAnnotatorClient } from "@google-cloud/vision";

const STORAGE_BUCKET = "eyeonsite-3b8e0.appspot.com";

// SafeSearch: LIKELY=4, VERY_LIKELY=5 in the protobuf enum; also compared as strings.
function isLikelihoodFlagged(value: unknown): boolean {
  if (value === "LIKELY" || value === "VERY_LIKELY") return true;
  // Numeric protobuf enum: LIKELY=4, VERY_LIKELY=5
  if (typeof value === "number" && value >= 4) return true;
  return false;
}

// If a single colour covers > 85 % of pixels the image is likely solid/blank.
const SOLID_COLOUR_THRESHOLD = 0.85;

export interface VisionPhotoResult {
  photoId: string;
  appropriate: boolean;   // false = SafeSearch flagged adult or violent content
  colorVariety: number;   // 0-1; low score signals a blank/dark/overexposed frame
}

/** Lazily-initialised singleton so tests can inject a mock before first use. */
let _client: ImageAnnotatorClient | null = null;

export function setVisionClient(client: ImageAnnotatorClient): void {
  _client = client;
}

function getClient(): ImageAnnotatorClient {
  if (!_client) _client = new ImageAnnotatorClient();
  return _client;
}

/**
 * Returns the GCS URI for a photo stored by FirebaseStorageEvidenceUploader.
 * Path convention: evidence/{taskId}/{photoId}.jpg
 */
export function photoGcsUri(taskId: string, photoId: string): string {
  return `gs://${STORAGE_BUCKET}/evidence/${taskId}/${photoId}.jpg`;
}

/**
 * Calls Cloud Vision API (SAFE_SEARCH + IMAGE_PROPERTIES) for a single photo.
 * Throws on network/API error — caller should catch and decide whether to proceed.
 */
export async function analyzePhotoWithVision(
  photoId: string,
  gcsUri: string,
): Promise<VisionPhotoResult> {
  const [response] = await getClient().annotateImage({
    image: { source: { imageUri: gcsUri } },
    features: [
      { type: "SAFE_SEARCH_DETECTION" },
      { type: "IMAGE_PROPERTIES" },
    ],
  });

  // SafeSearch: reject adult or violent content.
  const ss = response.safeSearchAnnotation;
  const appropriate =
    !ss ||
    (!isLikelihoodFlagged(ss.adult) && !isLikelihoodFlagged(ss.violence));

  // Colour variety: a single dominant colour > 85 % → blank/dark/washed-out image.
  const colors =
    response.imagePropertiesAnnotation?.dominantColors?.colors ?? [];
  const maxFraction = colors.reduce(
    (max, c) => Math.max(max, c.pixelFraction ?? 0),
    0,
  );
  const colorVariety = maxFraction > SOLID_COLOUR_THRESHOLD ? 0.3 : 1.0;

  return { photoId, appropriate, colorVariety };
}

/**
 * Aggregates per-photo Vision results into a single 0-1 quality score.
 * Returns 0.5 (neutral) when no results are provided so callers can gracefully
 * degrade when the Vision API is unavailable.
 */
export function visionScoreFromResults(results: VisionPhotoResult[]): number {
  if (results.length === 0) return 0.5;
  const sum = results.reduce(
    (acc, r) => acc + (r.appropriate ? r.colorVariety : 0),
    0,
  );
  return sum / results.length;
}
