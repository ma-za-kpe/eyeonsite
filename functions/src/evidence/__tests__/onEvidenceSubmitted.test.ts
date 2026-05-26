import { computeQualityScore } from "../onEvidenceSubmitted";

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
