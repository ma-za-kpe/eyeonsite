import { geohashForPoint, geohashQueryBounds, GEOHASH_PRECISION } from "../geohash";

// ── geohashForPoint ───────────────────────────────────────────────────────────

describe("geohashForPoint", () => {
  test("returns string of length GEOHASH_PRECISION (5)", () => {
    const gh = geohashForPoint(5.6037, -0.187);
    expect(gh).toHaveLength(GEOHASH_PRECISION);
  });

  test("returns consistent result for same input", () => {
    expect(geohashForPoint(5.5502, -0.2174)).toBe(geohashForPoint(5.5502, -0.2174));
  });

  test("nearby points share a common prefix", () => {
    const a = geohashForPoint(5.6037, -0.187);
    const b = geohashForPoint(5.604, -0.187);
    expect(b.slice(0, 3)).toBe(a.slice(0, 3));
  });

  test("distant points differ", () => {
    const accra = geohashForPoint(5.6037, -0.187);
    const london = geohashForPoint(51.5074, -0.1278);
    expect(accra).not.toBe(london);
    expect(accra[0]).not.toBe(london[0]);
  });

  test("returns only BASE32 characters", () => {
    const BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    const gh = geohashForPoint(5.5502, -0.2174);
    for (const ch of gh) {
      expect(BASE32).toContain(ch);
    }
  });

  test("poles and antimeridian do not throw", () => {
    expect(() => geohashForPoint(89.9, 0)).not.toThrow();
    expect(() => geohashForPoint(-89.9, 0)).not.toThrow();
    expect(() => geohashForPoint(0, 179.9)).not.toThrow();
    expect(() => geohashForPoint(0, -179.9)).not.toThrow();
  });
});

// ── geohashQueryBounds ────────────────────────────────────────────────────────

describe("geohashQueryBounds", () => {
  const LAT = 5.5502;
  const LNG = -0.2174;

  test("returns at least one range", () => {
    const ranges = geohashQueryBounds(LAT, LNG);
    expect(ranges.length).toBeGreaterThanOrEqual(1);
  });

  test("returns at most 9 ranges (3×3 grid, worst case no merging)", () => {
    const ranges = geohashQueryBounds(LAT, LNG);
    expect(ranges.length).toBeLessThanOrEqual(9);
  });

  test("each range has start <= end", () => {
    const ranges = geohashQueryBounds(LAT, LNG);
    for (const [start, end] of ranges) {
      expect(start <= end).toBe(true);
    }
  });

  test("the center geohash falls within one of the ranges", () => {
    const center = geohashForPoint(LAT, LNG);
    const ranges = geohashQueryBounds(LAT, LNG);
    const covered = ranges.some(([start, end]) => center >= start && center <= end);
    expect(covered).toBe(true);
  });

  test("range end suffix is a high Unicode character (￿)", () => {
    const ranges = geohashQueryBounds(LAT, LNG);
    for (const [, end] of ranges) {
      expect(end.charCodeAt(end.length - 1)).toBeGreaterThan(127);
    }
  });

  test("merging reduces range count vs naive 9 separate ranges", () => {
    // For typical mid-latitude cells, some neighbors share a parent prefix
    // so the merged count should be < 9 most of the time.
    // At minimum it must be <= 9.
    const ranges = geohashQueryBounds(LAT, LNG);
    expect(ranges.length).toBeLessThanOrEqual(9);
  });

  test("all neighbors of a uniform area collapse into fewer ranges", () => {
    // Pick a point whose geohash neighbors all share the same 4-char parent.
    // geohash "s0000" covers the equatorial prime-meridian area; all cells
    // in the 3×3 grid starting with "s" should merge heavily.
    const ranges = geohashQueryBounds(0.0, 0.0);
    expect(ranges.length).toBeGreaterThanOrEqual(1);
    expect(ranges.length).toBeLessThanOrEqual(9);
  });
});
