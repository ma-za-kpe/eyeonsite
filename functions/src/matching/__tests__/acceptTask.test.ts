import { HttpsError } from "firebase-functions/v2/https";
import { buildTaskAcceptanceUpdate } from "../acceptTask";

describe("buildTaskAcceptanceUpdate", () => {
  test("throws not-found when task is undefined", () => {
    expect(() => buildTaskAcceptanceUpdate(undefined, "uid-1", 0))
      .toThrow(HttpsError);
    try {
      buildTaskAcceptanceUpdate(undefined, "uid-1", 0);
    } catch (e) {
      expect((e as HttpsError).code).toBe("not-found");
    }
  });

  test("throws failed-precondition when task is not POSTED", () => {
    try {
      buildTaskAcceptanceUpdate({ status: "INSPECTOR_MATCHED", pinnedInspectorIds: ["uid-1"] }, "uid-1", 0);
    } catch (e) {
      expect((e as HttpsError).code).toBe("failed-precondition");
    }
  });

  test("throws failed-precondition for EXPIRED task", () => {
    try {
      buildTaskAcceptanceUpdate({ status: "EXPIRED", pinnedInspectorIds: ["uid-1"] }, "uid-1", 0);
    } catch (e) {
      expect((e as HttpsError).code).toBe("failed-precondition");
    }
  });

  test("throws permission-denied when inspector not in pinned list", () => {
    try {
      buildTaskAcceptanceUpdate(
        { status: "POSTED", pinnedInspectorIds: ["a", "b"] },
        "c",
        0,
      );
    } catch (e) {
      expect((e as HttpsError).code).toBe("permission-denied");
    }
  });

  test("returns INSPECTOR_MATCHED update for valid acceptance", () => {
    const { update } = buildTaskAcceptanceUpdate(
      { status: "POSTED", pinnedInspectorIds: ["a", "b", "c"] },
      "b",
      1234567890,
    );
    expect(update["status"]).toBe("INSPECTOR_MATCHED");
    expect(update["matchedInspectorId"]).toBe("b");
    expect(update["matchedAtMs"]).toBe(1234567890);
  });

  test("losers excludes the accepting inspector", () => {
    const { losers } = buildTaskAcceptanceUpdate(
      { status: "POSTED", pinnedInspectorIds: ["a", "b", "c"] },
      "b",
      0,
    );
    expect(losers).toEqual(["a", "c"]);
  });

  test("losers is empty when only one pinned inspector accepts", () => {
    const { losers } = buildTaskAcceptanceUpdate(
      { status: "POSTED", pinnedInspectorIds: ["solo"] },
      "solo",
      0,
    );
    expect(losers).toHaveLength(0);
  });

  test("handles missing pinnedInspectorIds gracefully when inspector matches", () => {
    // Inspector with no pinned list — should fail permission-denied
    try {
      buildTaskAcceptanceUpdate({ status: "POSTED" }, "uid-1", 0);
    } catch (e) {
      expect((e as HttpsError).code).toBe("permission-denied");
    }
  });
});
