import {
  H3_PARENT_RESOLUTION,
  H3_TASK_RESOLUTION,
  h3CandidateCellsForTask,
  h3CoverageForPoint,
} from "../h3";

const ACCRA_LAT = 5.6037;
const ACCRA_LNG = -0.187;

describe("h3CoverageForPoint", () => {
  test("returns parent, center, and neighbor H3 cells", () => {
    const coverage = h3CoverageForPoint(ACCRA_LAT, ACCRA_LNG);

    expect(coverage.h3Res7).toHaveLength(15);
    expect(coverage.h3Res9).toHaveLength(15);
    expect(coverage.h3NeighborRes9).toHaveLength(6);
    expect(coverage.h3NeighborRes9).not.toContain(coverage.h3Res9);
  });

  test("uses the configured parent and task resolutions", () => {
    expect(H3_PARENT_RESOLUTION).toBe(7);
    expect(H3_TASK_RESOLUTION).toBe(9);
  });
});

describe("h3CandidateCellsForTask", () => {
  test("prefers persisted task cells when they are present", () => {
    const cells = h3CandidateCellsForTask({
      h3Res9: "center",
      h3NeighborRes9: ["neighbor-b", "neighbor-a", "center"],
    });

    expect(cells).toEqual(["center", "neighbor-b", "neighbor-a"]);
  });

  test("computes cells from coordinates when task cells are missing", () => {
    const cells = h3CandidateCellsForTask({}, ACCRA_LAT, ACCRA_LNG);

    expect(cells).toHaveLength(7);
    expect(new Set(cells).size).toBe(7);
  });
});
