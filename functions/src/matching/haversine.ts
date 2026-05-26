const EARTH_RADIUS_KM = 6371;
const DEG_TO_RAD = Math.PI / 180;

/** Returns great-circle distance in km between two lat/lng points. */
export function haversine(
  lat1: number, lng1: number,
  lat2: number, lng2: number,
): number {
  const dLat = (lat2 - lat1) * DEG_TO_RAD;
  const dLng = (lng2 - lng1) * DEG_TO_RAD;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1 * DEG_TO_RAD) * Math.cos(lat2 * DEG_TO_RAD) *
    Math.sin(dLng / 2) ** 2;
  return EARTH_RADIUS_KM * 2 * Math.asin(Math.sqrt(a));
}
