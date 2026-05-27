import { ImageAnnotatorClient } from "@google-cloud/vision";
import {
  analyzePhotoWithVision,
  photoGcsUri,
  setVisionClient,
  visionScoreFromResults,
  VisionPhotoResult,
} from "../visionQuality";

// Build a mock ImageAnnotatorClient that returns a pre-configured response.
function makeMockClient(response: object): ImageAnnotatorClient {
  return {
    annotateImage: jest.fn().mockResolvedValue([response]),
  } as unknown as ImageAnnotatorClient;
}

beforeEach(() => {
  jest.clearAllMocks();
});

describe("photoGcsUri", () => {
  test("produces correct GCS URI", () => {
    expect(photoGcsUri("task-001", "photo-abc")).toBe(
      "gs://eyeonsite-3b8e0.appspot.com/evidence/task-001/photo-abc.jpg",
    );
  });
});

describe("analyzePhotoWithVision", () => {
  test("returns appropriate=true and colorVariety=1 for a clean photo", async () => {
    const client = makeMockClient({
      safeSearchAnnotation: { adult: "UNLIKELY", violence: "VERY_UNLIKELY" },
      imagePropertiesAnnotation: {
        dominantColors: {
          colors: [
            { pixelFraction: 0.3 },
            { pixelFraction: 0.25 },
            { pixelFraction: 0.2 },
          ],
        },
      },
    });
    setVisionClient(client);

    const result = await analyzePhotoWithVision("photo-1", "gs://bucket/img.jpg");
    expect(result.appropriate).toBe(true);
    expect(result.colorVariety).toBe(1.0);
  });

  test("returns appropriate=false when adult=LIKELY", async () => {
    const client = makeMockClient({
      safeSearchAnnotation: { adult: "LIKELY", violence: "UNLIKELY" },
      imagePropertiesAnnotation: { dominantColors: { colors: [] } },
    });
    setVisionClient(client);

    const result = await analyzePhotoWithVision("photo-2", "gs://bucket/img.jpg");
    expect(result.appropriate).toBe(false);
  });

  test("returns appropriate=false when violence=VERY_LIKELY", async () => {
    const client = makeMockClient({
      safeSearchAnnotation: { adult: "UNLIKELY", violence: "VERY_LIKELY" },
      imagePropertiesAnnotation: { dominantColors: { colors: [] } },
    });
    setVisionClient(client);

    const result = await analyzePhotoWithVision("photo-3", "gs://bucket/img.jpg");
    expect(result.appropriate).toBe(false);
  });

  test("returns colorVariety=0.3 when one colour covers > 85 % of pixels", async () => {
    const client = makeMockClient({
      safeSearchAnnotation: { adult: "UNLIKELY", violence: "UNLIKELY" },
      imagePropertiesAnnotation: {
        dominantColors: {
          colors: [{ pixelFraction: 0.9 }, { pixelFraction: 0.05 }],
        },
      },
    });
    setVisionClient(client);

    const result = await analyzePhotoWithVision("photo-4", "gs://bucket/img.jpg");
    expect(result.colorVariety).toBe(0.3);
  });

  test("handles missing imagePropertiesAnnotation gracefully", async () => {
    const client = makeMockClient({
      safeSearchAnnotation: { adult: "UNLIKELY", violence: "UNLIKELY" },
    });
    setVisionClient(client);

    const result = await analyzePhotoWithVision("photo-5", "gs://bucket/img.jpg");
    expect(result.appropriate).toBe(true);
    expect(result.colorVariety).toBe(1.0);
  });

  test("includes photoId in result", async () => {
    const client = makeMockClient({
      safeSearchAnnotation: {},
      imagePropertiesAnnotation: { dominantColors: { colors: [] } },
    });
    setVisionClient(client);

    const result = await analyzePhotoWithVision("my-photo-id", "gs://bucket/img.jpg");
    expect(result.photoId).toBe("my-photo-id");
  });
});

describe("visionScoreFromResults", () => {
  test("returns 0.5 for empty array (neutral/degraded fallback)", () => {
    expect(visionScoreFromResults([])).toBe(0.5);
  });

  test("returns 1.0 when all photos are appropriate with full colour variety", () => {
    const results: VisionPhotoResult[] = [
      { photoId: "a", appropriate: true, colorVariety: 1.0 },
      { photoId: "b", appropriate: true, colorVariety: 1.0 },
    ];
    expect(visionScoreFromResults(results)).toBe(1.0);
  });

  test("returns 0 when all photos are inappropriate", () => {
    const results: VisionPhotoResult[] = [
      { photoId: "a", appropriate: false, colorVariety: 1.0 },
      { photoId: "b", appropriate: false, colorVariety: 1.0 },
    ];
    expect(visionScoreFromResults(results)).toBe(0);
  });

  test("averages scores across photos", () => {
    const results: VisionPhotoResult[] = [
      { photoId: "a", appropriate: true, colorVariety: 1.0 },   // 1.0
      { photoId: "b", appropriate: true, colorVariety: 0.3 },   // 0.3
      { photoId: "c", appropriate: false, colorVariety: 1.0 },  // 0.0
    ];
    // (1.0 + 0.3 + 0) / 3 ≈ 0.433
    expect(visionScoreFromResults(results)).toBeCloseTo(0.433, 2);
  });
});
