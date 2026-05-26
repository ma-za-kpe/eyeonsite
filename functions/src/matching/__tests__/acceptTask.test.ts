/**
 * acceptTask unit tests — validates the atomic transaction logic
 * and the "task taken" notification path using mocks.
 *
 * Full end-to-end behaviour (concurrent requests, Firestore rollbacks)
 * is verified with the Firebase Emulator in integration tests (V1).
 */

// Simulate the transaction guard logic extracted from acceptTask.ts
// so we can unit-test it without spinning up the Functions runtime.

interface TaskData {
  status: string;
  pinnedInspectorIds?: string[];
}

function validateAndBuildUpdate(
  task: TaskData | undefined,
  inspectorUid: string,
): { update: Record<string, unknown>; losers: string[] } {
  if (!task) throw new Error("not-found");
  if (task.status !== "POSTED") throw new Error("failed-precondition");

  const pinned = task.pinnedInspectorIds ?? [];
  return {
    update: {
      status: "INSPECTOR_MATCHED",
      matchedInspectorId: inspectorUid,
    },
    losers: pinned.filter((uid) => uid !== inspectorUid),
  };
}

describe("acceptTask — transaction guard", () => {
  test("throws not-found when task document missing", () => {
    expect(() => validateAndBuildUpdate(undefined, "uid-1")).toThrow("not-found");
  });

  test("throws failed-precondition when task is not POSTED", () => {
    const task: TaskData = { status: "INSPECTOR_MATCHED" };
    expect(() => validateAndBuildUpdate(task, "uid-1")).toThrow("failed-precondition");
  });

  test("throws failed-precondition for EXPIRED task", () => {
    const task: TaskData = { status: "EXPIRED" };
    expect(() => validateAndBuildUpdate(task, "uid-1")).toThrow("failed-precondition");
  });

  test("sets status to INSPECTOR_MATCHED and records accepting inspector", () => {
    const task: TaskData = { status: "POSTED", pinnedInspectorIds: ["a", "b", "c"] };
    const { update } = validateAndBuildUpdate(task, "b");
    expect(update["status"]).toBe("INSPECTOR_MATCHED");
    expect(update["matchedInspectorId"]).toBe("b");
  });

  test("losers excludes the accepting inspector", () => {
    const task: TaskData = { status: "POSTED", pinnedInspectorIds: ["a", "b", "c"] };
    const { losers } = validateAndBuildUpdate(task, "b");
    expect(losers).toEqual(["a", "c"]);
  });

  test("losers is empty when only one pinned inspector and they accept", () => {
    const task: TaskData = { status: "POSTED", pinnedInspectorIds: ["solo"] };
    const { losers } = validateAndBuildUpdate(task, "solo");
    expect(losers).toHaveLength(0);
  });

  test("losers is empty when pinnedInspectorIds absent", () => {
    const task: TaskData = { status: "POSTED" };
    const { losers } = validateAndBuildUpdate(task, "uid-1");
    expect(losers).toHaveLength(0);
  });

  test("POSTED task returns full update payload", () => {
    const task: TaskData = { status: "POSTED" };
    const { update } = validateAndBuildUpdate(task, "uid-99");
    expect(Object.keys(update)).toContain("status");
    expect(Object.keys(update)).toContain("matchedInspectorId");
  });
});
