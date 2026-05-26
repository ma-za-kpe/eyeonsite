import { HttpsError } from "firebase-functions/v2/https";
import { buildTaskAcceptanceUpdate } from "../acceptTask";

function expectHttpsErrorCode(action: () => unknown, code: string): void {
  try {
    action();
    throw new Error("Expected HttpsError");
  } catch (err) {
    expect(err).toBeInstanceOf(HttpsError);
    expect((err as HttpsError).code).toBe(code);
  }
}

describe("buildTaskAcceptanceUpdate", () => {
  test("throws not-found when task is undefined", () => {
    expectHttpsErrorCode(
      () => buildTaskAcceptanceUpdate(undefined, "uid-1", 0),
      "not-found",
    );
  });

  test("throws failed-precondition when task is not POSTED", () => {
    expectHttpsErrorCode(
      () => buildTaskAcceptanceUpdate(
        { status: "INSPECTOR_MATCHED", pinnedInspectorIds: ["uid-1"] },
        "uid-1",
        0,
      ),
      "failed-precondition",
    );
  });

  test("throws failed-precondition for EXPIRED task", () => {
    expectHttpsErrorCode(
      () => buildTaskAcceptanceUpdate(
        { status: "EXPIRED", pinnedInspectorIds: ["uid-1"] },
        "uid-1",
        0,
      ),
      "failed-precondition",
    );
  });

  test("throws permission-denied when inspector not in pinned list", () => {
    expectHttpsErrorCode(
      () => buildTaskAcceptanceUpdate(
        { status: "POSTED", pinnedInspectorIds: ["a", "b"] },
        "c",
        0,
      ),
      "permission-denied",
    );
  });

  test("throws permission-denied when pinned list is missing", () => {
    expectHttpsErrorCode(
      () => buildTaskAcceptanceUpdate({ status: "POSTED" }, "uid-1", 0),
      "permission-denied",
    );
  });

  test("returns INSPECTOR_MATCHED update for valid acceptance", () => {
    const { update } = buildTaskAcceptanceUpdate(
      { status: "POSTED", pinnedInspectorIds: ["a", "b", "c"] },
      "b",
      1_234_567_890,
    );

    expect(update["status"]).toBe("INSPECTOR_MATCHED");
    expect(update["matchedInspectorId"]).toBe("b");
    expect(update["matchedAtMs"]).toBe(1_234_567_890);
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
});
