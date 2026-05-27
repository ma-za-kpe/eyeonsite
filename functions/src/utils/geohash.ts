/**
 * Compact geohash implementation (no external dependencies).
 *
 * Precision 5 cells ≈ 4.9 km × 4.9 km — suitable for a 10 km match radius.
 * geohashQueryBounds returns [start, end] pairs for Firestore range queries
 * covering the 3×3 grid of cells surrounding the given point.
 */

const BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

/** Default precision used for all inspector / task geohashes. */
export const GEOHASH_PRECISION = 5;

function encode(lat: number, lng: number, precision: number): string {
  let geohash = "";
  let minLat = -90, maxLat = 90, minLng = -180, maxLng = 180;
  let idx = 0, bit = 0, evenBit = true;

  while (geohash.length < precision) {
    if (evenBit) {
      const mid = (minLng + maxLng) / 2;
      if (lng >= mid) { idx = (idx << 1) + 1; minLng = mid; }
      else { idx = idx << 1; maxLng = mid; }
    } else {
      const mid = (minLat + maxLat) / 2;
      if (lat >= mid) { idx = (idx << 1) + 1; minLat = mid; }
      else { idx = idx << 1; maxLat = mid; }
    }
    evenBit = !evenBit;
    if (++bit === 5) { geohash += BASE32[idx]; bit = 0; idx = 0; }
  }
  return geohash;
}

function bounds(geohash: string): { minLat: number; maxLat: number; minLng: number; maxLng: number } {
  let evenBit = true, minLat = -90, maxLat = 90, minLng = -180, maxLng = 180;
  for (const char of geohash) {
    const idx = BASE32.indexOf(char);
    for (let bits = 4; bits >= 0; bits--) {
      const b = (idx >> bits) & 1;
      if (evenBit) { const mid = (minLng + maxLng) / 2; if (b) minLng = mid; else maxLng = mid; }
      else { const mid = (minLat + maxLat) / 2; if (b) minLat = mid; else maxLat = mid; }
      evenBit = !evenBit;
    }
  }
  return { minLat, maxLat, minLng, maxLng };
}

function adjacent(gh: string, dir: "n" | "s" | "e" | "w"): string {
  const { minLat, maxLat, minLng, maxLng } = bounds(gh);
  const dLat = maxLat - minLat;
  const dLng = maxLng - minLng;
  const lat = (minLat + maxLat) / 2;
  const lng = (minLng + maxLng) / 2;
  switch (dir) {
    case "n": return encode(Math.min(lat + dLat, 90 - 1e-9), lng, gh.length);
    case "s": return encode(Math.max(lat - dLat, -90 + 1e-9), lng, gh.length);
    case "e": {
      const newLng = lng + dLng;
      return encode(lat, newLng > 180 ? newLng - 360 : newLng, gh.length);
    }
    case "w": {
      const newLng = lng - dLng;
      return encode(lat, newLng < -180 ? newLng + 360 : newLng, gh.length);
    }
  }
}

/** Encodes a lat/lng point to a geohash string at GEOHASH_PRECISION (5). */
export function geohashForPoint(lat: number, lng: number): string {
  return encode(lat, lng, GEOHASH_PRECISION);
}

/**
 * Returns an array of `[start, end]` Firestore range query bounds that cover
 * the 3×3 grid of geohash cells surrounding (lat, lng).
 *
 * Query pattern:
 *   for (const [start, end] of geohashQueryBounds(lat, lng)) {
 *     db.collection("inspectors")
 *       .where("geohash", ">=", start)
 *       .where("geohash", "<=", end)
 *       .get()
 *   }
 */
export function geohashQueryBounds(lat: number, lng: number): [string, string][] {
  const c = encode(lat, lng, GEOHASH_PRECISION);
  const n = adjacent(c, "n");
  const s = adjacent(c, "s");
  const e = adjacent(c, "e");
  const w = adjacent(c, "w");
  const allCells = Array.from(
    new Set([c, n, s, e, w, adjacent(n, "e"), adjacent(n, "w"), adjacent(s, "e"), adjacent(s, "w")]),
  ).sort();

  // Merge alphabetically-adjacent cells (same parent prefix, consecutive last char)
  // into single [start, end] ranges to minimise Firestore query count.
  const ranges: [string, string][] = [];
  let rangeStart = allCells[0];
  let prev = allCells[0];

  for (let i = 1; i < allCells.length; i++) {
    const cur = allCells[i];
    const sameParent = cur.slice(0, -1) === prev.slice(0, -1);
    const consecutive = BASE32.indexOf(cur[cur.length - 1]) === BASE32.indexOf(prev[prev.length - 1]) + 1;
    if (sameParent && consecutive) {
      prev = cur; // extend current range
    } else {
      ranges.push([rangeStart, prev + "￿"]);
      rangeStart = cur;
      prev = cur;
    }
  }
  ranges.push([rangeStart, prev + "￿"]);
  return ranges;
}
