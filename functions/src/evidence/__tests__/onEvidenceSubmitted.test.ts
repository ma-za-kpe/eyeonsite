import {
  analyzeEvidenceTrust,
  attestationVerdictForEvidence,
  computeQualityScore,
  EvidencePhotoInput,
} from "../onEvidenceSubmitted";

const TASK_LAT = 5.6037;
const TASK_LNG = -0.1870;

// East Legon cluster — all within 200 m of task, spread ~150 m apart
const ON_SITE = [
  { latitude: 5.6037, longitude: -0.1870 },
  { latitude: 5.6038, longitude: -0.1871 },
  { latitude: 5.6039, longitude: -0.1872 },
];

// Tema — ~25 km away from task
const OFF_SITE = [
  { latitude: 5.6690, longitude: -0.0166 },
  { latitude: 5.6691, longitude: -0.0167 },
  { latitude: 5.6692, longitude: -0.0168 },
];

// Same exact point — no movement
const STATIONARY = [
  { latitude: TASK_LAT, longitude: TASK_LNG },
  { latitude: TASK_LAT, longitude: TASK_LNG },
  { latitude: TASK_LAT, longitude: TASK_LNG },
];

describe("server attestation boundary", () => {
  test("does not trust client-persisted PASSED or assessment fields", () => {
    expect(attestationVerdictForEvidence({
      attestationVerdict: "PASSED",
      attestation: { provider: "PLAY_INTEGRITY", verdict: "PASSED" },
      qualityScore: 100,
    })).toBe("UNKNOWN");
  });
});

describe("computeQualityScore", () => {
  test("empty array → score 0 and gpsValid false", () => {
    const { score, gpsValid } = computeQualityScore([], TASK_LAT, TASK_LNG);
    expect(score).toBe(0);
    expect(gpsValid).toBe(false);
  });

  test("three on-site photos with spread → score ≥ 90 and gpsValid true", () => {
    const { score, gpsValid } = computeQualityScore(ON_SITE, TASK_LAT, TASK_LNG);
    expect(score).toBeGreaterThanOrEqual(90);
    expect(gpsValid).toBe(true);
  });

  test("three off-site photos → gpsValid false and low score", () => {
    const { score, gpsValid } = computeQualityScore(OFF_SITE, TASK_LAT, TASK_LNG);
    expect(gpsValid).toBe(false);
    expect(score).toBeLessThan(60);
  });

  test("single on-site photo → lower count score than three photos", () => {
    const { score: oneScore } = computeQualityScore(ON_SITE.slice(0, 1), TASK_LAT, TASK_LNG);
    const { score: threeScore } = computeQualityScore(ON_SITE, TASK_LAT, TASK_LNG);
    expect(threeScore).toBeGreaterThan(oneScore);
  });

  test("no task location → gpsValid true with partial GPS credit", () => {
    const { score, gpsValid } = computeQualityScore(ON_SITE);
    expect(gpsValid).toBe(true);
    expect(score).toBeGreaterThan(0);
    // GPS section caps at 25 when no task location
    expect(score).toBeLessThan(100);
  });

  test("stationary photos penalise spread score vs moving", () => {
    const { score: moving } = computeQualityScore(ON_SITE, TASK_LAT, TASK_LNG);
    const { score: still } = computeQualityScore(STATIONARY, TASK_LAT, TASK_LNG);
    expect(moving).toBeGreaterThan(still);
  });

  test("≥ 80 % on-site passes gpsValid even with one off-site photo", () => {
    const mixed = [...ON_SITE, OFF_SITE[0]]; // 3/4 on-site = 75% → false
    const { gpsValid: fail } = computeQualityScore(mixed, TASK_LAT, TASK_LNG);
    expect(fail).toBe(false);

    const mostly = [...ON_SITE, ON_SITE[0], OFF_SITE[0]]; // 4/5 = 80% → true
    const { gpsValid: pass } = computeQualityScore(mostly, TASK_LAT, TASK_LNG);
    expect(pass).toBe(true);
  });

  test("extra photos beyond minimum do not increase count score beyond 30", () => {
    const six = [...ON_SITE, ...ON_SITE];
    const { score: sixScore } = computeQualityScore(six, TASK_LAT, TASK_LNG);
    const { score: threeScore } = computeQualityScore(ON_SITE, TASK_LAT, TASK_LNG);
    // Count portion is capped; spread may differ but overall not dramatically higher
    expect(sixScore - threeScore).toBeLessThan(15);
  });
});

describe("analyzeEvidenceTrust", () => {
  const trustedPhotos: EvidencePhotoInput[] = ON_SITE.map((photo, index) => ({
    ...photo,
    id: `photo-${index + 1}`,
    accuracyMeters: 4,
    capturedAtDeviceMs: 1_748_000_000_000 + index * 60_000,
    receivedAtServerMs: 1_748_000_300_000,
    exif: {
      latitude: photo.latitude,
      longitude: photo.longitude,
      capturedAtMs: 1_748_000_000_000 + index * 60_000,
    },
    perceptualHash: `hash-${index + 1}`,
  }));

  test("returns separate quality fraud content and provenance outputs", () => {
    const analysis = analyzeEvidenceTrust(trustedPhotos, TASK_LAT, TASK_LNG, {
      visionResults: trustedPhotos.map((photo) => ({
        photoId: photo.id ?? "",
        appropriate: true,
        colorVariety: 1,
      })),
      attestationVerdict: "PASSED",
    });

    expect(analysis.qualityScore).toBeGreaterThanOrEqual(90);
    expect(analysis.gpsValid).toBe(true);
    expect(analysis.fraudRisk).toBe("LOW");
    expect(analysis.contentRisk).toBe("LOW");
    expect(analysis.provenanceStatus).toBe("PASSED");
    expect(analysis.fraudSignals).toEqual([]);
  });

  test("flags mock location low accuracy gps exif mismatch and duplicate phash", () => {
    const riskyPhotos: EvidencePhotoInput[] = trustedPhotos.map((photo, index) => ({
      ...photo,
      isMockLocation: index === 0,
      accuracyMeters: index === 1 ? 120 : photo.accuracyMeters,
      exif: index === 2
        ? { latitude: 5.6690, longitude: -0.0166, capturedAtMs: photo.capturedAtDeviceMs }
        : photo.exif,
      perceptualHash: "same-hash",
    }));

    const analysis = analyzeEvidenceTrust(riskyPhotos, TASK_LAT, TASK_LNG, {
      visionResults: [],
      attestationVerdict: "PASSED",
    });

    expect(analysis.fraudRisk).toBe("HIGH");
    expect(analysis.provenanceStatus).toBe("FAILED");
    expect(analysis.fraudSignals.map((signal) => signal.type)).toEqual(
      expect.arrayContaining(["MOCK_LOCATION", "LOW_ACCURACY", "GPS_EXIF_MISMATCH", "PHASH_DUPLICATE"]),
    );
  });

  test("flags timestamp drift and impossible travel", () => {
    const fastTravel: EvidencePhotoInput[] = [
      {
        ...trustedPhotos[0],
        capturedAtDeviceMs: 1_748_000_000_000,
        exif: { ...trustedPhotos[0].exif, capturedAtMs: 1_747_999_000_000 },
      },
      {
        ...OFF_SITE[0],
        id: "far-photo",
        capturedAtDeviceMs: 1_748_000_060_000,
        receivedAtServerMs: 1_748_000_300_000,
        accuracyMeters: 5,
        perceptualHash: "far-photo-hash",
      },
    ];

    const analysis = analyzeEvidenceTrust(fastTravel, TASK_LAT, TASK_LNG, {
      visionResults: [],
      attestationVerdict: "PASSED",
    });

    expect(analysis.fraudSignals.map((signal) => signal.type)).toEqual(
      expect.arrayContaining(["TIMESTAMP_DRIFT", "IMPOSSIBLE_TRAVEL", "GPS_OFF_SITE"]),
    );
    expect(analysis.fraudRisk).toBe("HIGH");
  });

  test("flags unsafe content and failed attestation", () => {
    const analysis = analyzeEvidenceTrust(trustedPhotos, TASK_LAT, TASK_LNG, {
      visionResults: [{ photoId: "photo-1", appropriate: false, colorVariety: 1 }],
      attestationVerdict: "FAILED",
    });

    expect(analysis.contentRisk).toBe("HIGH");
    expect(analysis.provenanceStatus).toBe("FAILED");
    expect(analysis.fraudSignals.map((signal) => signal.type)).toEqual(
      expect.arrayContaining(["CONTENT_UNSAFE", "ATTESTATION_FAILED"]),
    );
  });
});
