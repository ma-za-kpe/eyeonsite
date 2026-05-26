import { HttpsError } from "firebase-functions/v2/https";
import { buildFundUpdate, buildReleaseUpdate } from "../escrow";

function expectCode(fn: () => unknown, code: string): void {
  try {
    fn();
    throw new Error("Expected HttpsError but none thrown");
  } catch (err) {
    expect(err).toBeInstanceOf(HttpsError);
    expect((err as HttpsError).code).toBe(code);
  }
}

const NOW = 1_748_000_000_000;
const CLIENT = "client-001";
const INSPECTOR = "inspector-001";
const TASK_ID = "task-001";

const POSTED_TASK = {
  id: TASK_ID,
  status: "POSTED",
  clientUid: CLIENT,
  bountyUsd: 100,
};

const EVIDENCE_SUBMITTED_TASK = {
  id: TASK_ID,
  status: "EVIDENCE_SUBMITTED",
  clientUid: CLIENT,
  bountyUsd: 100,
  matchedInspectorId: INSPECTOR,
};

// ── buildFundUpdate ───────────────────────────────────────────────────────────

describe("buildFundUpdate", () => {
  test("throws not-found when task is undefined", () => {
    expectCode(() => buildFundUpdate(undefined, TASK_ID, CLIENT, NOW), "not-found");
  });

  test("throws permission-denied when caller is not the client", () => {
    expectCode(
      () => buildFundUpdate(POSTED_TASK, TASK_ID, "other-uid", NOW),
      "permission-denied",
    );
  });

  test("throws failed-precondition when task is not POSTED", () => {
    expectCode(
      () => buildFundUpdate({ ...POSTED_TASK, status: "ESCROW_FUNDED" }, TASK_ID, CLIENT, NOW),
      "failed-precondition",
    );
  });

  test("returns ESCROW_FUNDED task update for valid call", () => {
    const { taskUpdate } = buildFundUpdate(POSTED_TASK, TASK_ID, CLIENT, NOW);
    expect(taskUpdate["status"]).toBe("ESCROW_FUNDED");
    expect(taskUpdate["escrowAmountUsd"]).toBe(100);
    expect(taskUpdate["escrowFundedAtMs"]).toBe(NOW);
  });

  test("ledger entry has correct fee breakdown for $100 bounty", () => {
    const { ledgerEntry } = buildFundUpdate(POSTED_TASK, TASK_ID, CLIENT, NOW);
    expect(ledgerEntry["txType"]).toBe("ESCROW_HOLD");
    expect(ledgerEntry["status"]).toBe("HELD");
    expect(ledgerEntry["amountUsd"]).toBe(100);
    expect(ledgerEntry["escrowFeeUsd"]).toBe(2);       // 2 %
    expect(ledgerEntry["platformFeeUsd"]).toBe(18);    // 18 %
    expect(ledgerEntry["inspectorShareUsd"]).toBe(80); // 80 %
  });
});

// ── buildReleaseUpdate ────────────────────────────────────────────────────────

describe("buildReleaseUpdate", () => {
  test("throws not-found when task is undefined", () => {
    expectCode(
      () => buildReleaseUpdate(undefined, TASK_ID, CLIENT, NOW),
      "not-found",
    );
  });

  test("throws permission-denied when caller is not the client", () => {
    expectCode(
      () => buildReleaseUpdate(EVIDENCE_SUBMITTED_TASK, TASK_ID, "other-uid", NOW),
      "permission-denied",
    );
  });

  test("throws failed-precondition for non-releasable status", () => {
    expectCode(
      () => buildReleaseUpdate({ ...EVIDENCE_SUBMITTED_TASK, status: "POSTED" }, TASK_ID, CLIENT, NOW),
      "failed-precondition",
    );
  });

  test("throws failed-precondition when no matched inspector", () => {
    expectCode(
      () => buildReleaseUpdate(
        { ...EVIDENCE_SUBMITTED_TASK, matchedInspectorId: undefined },
        TASK_ID, CLIENT, NOW,
      ),
      "failed-precondition",
    );
  });

  test("returns ACCEPTED task update for valid call", () => {
    const { taskUpdate } = buildReleaseUpdate(EVIDENCE_SUBMITTED_TASK, TASK_ID, CLIENT, NOW);
    expect(taskUpdate["status"]).toBe("ACCEPTED");
    expect(taskUpdate["escrowReleasedAtMs"]).toBe(NOW);
    expect(taskUpdate["inspectorPayoutUsd"]).toBe(80);
  });

  test("inspector receives 80 % of bounty", () => {
    const { inspectorPayoutUsd, inspectorUid } = buildReleaseUpdate(
      EVIDENCE_SUBMITTED_TASK, TASK_ID, CLIENT, NOW,
    );
    expect(inspectorPayoutUsd).toBe(80);
    expect(inspectorUid).toBe(INSPECTOR);
  });

  test("payout rounds to 2 decimal places for fractional bounty", () => {
    const { inspectorPayoutUsd } = buildReleaseUpdate(
      { ...EVIDENCE_SUBMITTED_TASK, bountyUsd: 33.33 },
      TASK_ID, CLIENT, NOW,
    );
    expect(inspectorPayoutUsd).toBe(26.66); // 33.33 × 0.80 = 26.664 → 26.66
  });

  test("release works from UNDER_REVIEW and REVISION_REQUESTED states", () => {
    for (const status of ["UNDER_REVIEW", "REVISION_REQUESTED"]) {
      const { taskUpdate } = buildReleaseUpdate(
        { ...EVIDENCE_SUBMITTED_TASK, status },
        TASK_ID, CLIENT, NOW,
      );
      expect(taskUpdate["status"]).toBe("ACCEPTED");
    }
  });
});
