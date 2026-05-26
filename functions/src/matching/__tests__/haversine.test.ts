import { haversine } from "../haversine";

describe("haversine", () => {
  test("same point returns 0", () => {
    expect(haversine(5.6037, -0.1870, 5.6037, -0.1870)).toBe(0);
  });

  test("Accra CBD to Airport Hills is ~6 km", () => {
    // Accra CBD ~= 5.5502, -0.2174 | Airport Hills ~= 5.6034, -0.1730
    const km = haversine(5.5502, -0.2174, 5.6034, -0.1730);
    expect(km).toBeGreaterThan(5);
    expect(km).toBeLessThan(8);
  });

  test("Accra to London is ~5200 km", () => {
    const km = haversine(5.6037, -0.1870, 51.5074, -0.1278);
    expect(km).toBeGreaterThan(5100);
    expect(km).toBeLessThan(5400);
  });

  test("result is symmetric", () => {
    const a = haversine(5.5502, -0.2174, 5.6034, -0.1730);
    const b = haversine(5.6034, -0.1730, 5.5502, -0.2174);
    expect(a).toBeCloseTo(b, 6);
  });

  test("1 degree latitude is about 111 km", () => {
    const km = haversine(0, 0, 1, 0);
    expect(km).toBeGreaterThan(110);
    expect(km).toBeLessThan(112);
  });
});
