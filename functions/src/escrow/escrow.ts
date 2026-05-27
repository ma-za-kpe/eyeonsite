import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { initiateInspectorPayout } from "../payments/flutterwavePayout";

const REGION = "africa-south1";
const INSPECTOR_SHARE_RATE = 0.8;
const PLATFORM_FEE_RATE = 0.18;

// Statuses from which escrow can be funded (client posts a task)
const FUNDABLE_STATUS = "POSTED";
// Statuses from which the client can release payment after reviewing evidence
const RELEASABLE_STATUSES = new Set([
  "EVIDENCE_SUBMITTED",
  "UNDER_REVIEW",
  "REVISION_REQUESTED",
]);

// ── Shared interfaces ────────────────────────────────────────────────────────

export interface FundableTask {
  id?: string;
  status?: string;
  clientUid?: string;
  bountyUsd?: number;
}

export interface ReleasableTask {
  id?: string;
  status?: string;
  clientUid?: string;
  bountyUsd?: number;
  matchedInspectorId?: string;
}

export interface EscrowFeeBreakdown {
  amountUsd: number;
  escrowFeeUsd: number;
  platformFeeUsd: number;
  inspectorShareUsd: number;
}

function centsToUsd(cents: number): number {
  return Math.round(cents) / 100;
}

function usdToCents(amountUsd: number): number {
  return Math.round(amountUsd * 100);
}

export function calculateEscrowBreakdown(amountUsd: number): EscrowFeeBreakdown {
  const amountCents = usdToCents(amountUsd);
  const inspectorShareCents = Math.round(amountCents * INSPECTOR_SHARE_RATE);
  const platformFeeCents = Math.round(amountCents * PLATFORM_FEE_RATE);
  const escrowFeeCents = amountCents - inspectorShareCents - platformFeeCents;

  return {
    amountUsd: centsToUsd(amountCents),
    escrowFeeUsd: centsToUsd(escrowFeeCents),
    platformFeeUsd: centsToUsd(platformFeeCents),
    inspectorShareUsd: centsToUsd(inspectorShareCents),
  };
}

// ── Pure helpers (exported for unit testing) ─────────────────────────────────

export function buildFundUpdate(
  task: FundableTask | undefined,
  taskId: string,
  callerUid: string,
  nowMs: number,
): {
  taskUpdate: Record<string, unknown>;
  ledgerEntry: Record<string, unknown>;
} {
  if (!task) throw new HttpsError("not-found", "Task does not exist");
  if (task.clientUid !== callerUid) {
    throw new HttpsError("permission-denied", "Only the task owner can fund escrow");
  }
  if (task.status !== FUNDABLE_STATUS) {
    throw new HttpsError("failed-precondition", `Task must be POSTED to fund escrow, current: ${task.status}`);
  }
  const fees = calculateEscrowBreakdown(task.bountyUsd ?? 0);

  return {
    taskUpdate: {
      status: "ESCROW_FUNDED",
      escrowFundedAtMs: nowMs,
      escrowAmountUsd: fees.amountUsd,
    },
    ledgerEntry: {
      txType: "ESCROW_HOLD",
      taskId,
      clientUid: callerUid,
      amountUsd: fees.amountUsd,
      escrowFeeUsd: fees.escrowFeeUsd,
      platformFeeUsd: fees.platformFeeUsd,
      inspectorShareUsd: fees.inspectorShareUsd,
      status: "HELD",
      createdAtMs: nowMs,
    },
  };
}

export function buildReleaseUpdate(
  task: ReleasableTask | undefined,
  taskId: string,
  callerUid: string,
  nowMs: number,
): {
  taskUpdate: Record<string, unknown>;
  ledgerEntry: Record<string, unknown>;
  inspectorUid: string;
  inspectorPayoutUsd: number;
} {
  if (!task) throw new HttpsError("not-found", "Task does not exist");
  if (task.clientUid !== callerUid) {
    throw new HttpsError("permission-denied", "Only the task owner can release escrow");
  }
  if (!RELEASABLE_STATUSES.has(task.status ?? "")) {
    throw new HttpsError(
      "failed-precondition",
      `Cannot release escrow from status ${task.status}`,
    );
  }
  if (!task.matchedInspectorId) {
    throw new HttpsError("failed-precondition", "No matched inspector on this task");
  }

  const fees = calculateEscrowBreakdown(task.bountyUsd ?? 0);
  const inspectorPayoutUsd = fees.inspectorShareUsd;

  return {
    taskUpdate: {
      status: "ACCEPTED",
      escrowReleasedAtMs: nowMs,
      inspectorPayoutUsd,
    },
    ledgerEntry: {
      txType: "ESCROW_RELEASE",
      taskId,
      clientUid: callerUid,
      inspectorUid: task.matchedInspectorId,
      amountUsd: fees.amountUsd,
      escrowFeeUsd: fees.escrowFeeUsd,
      inspectorPayoutUsd,
      platformFeeUsd: fees.platformFeeUsd,
      status: "RELEASED",
      createdAtMs: nowMs,
    },
    inspectorUid: task.matchedInspectorId,
    inspectorPayoutUsd,
  };
}

// ── Cloud Functions ─────────────────────────────────────────────────────────

/**
 * fundTask — client callable.
 * Transitions a POSTED task to ESCROW_FUNDED and writes the escrow ledger entry.
 * Demo MVP: status change only — no real payment processing (FR-1).
 */
export const fundTask = onCall(
  { region: REGION },
  async (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "Must be signed in");

    const rawId = (request.data as { taskId?: unknown }).taskId;
    const taskId = typeof rawId === "string" ? rawId.trim() : "";
    if (!taskId) throw new HttpsError("invalid-argument", "taskId is required");

    const db = admin.firestore();
    const taskRef = db.doc(`tasks/${taskId}`);

    await db.runTransaction(async (tx) => {
      const snap = await tx.get(taskRef);
      const { taskUpdate, ledgerEntry } = buildFundUpdate(
        snap.data() as FundableTask | undefined,
        taskId,
        request.auth!.uid,
        Date.now(),
      );
      tx.update(taskRef, taskUpdate);
      tx.set(db.collection("escrow_transactions").doc(), ledgerEntry);
    });

    console.log(`Task ${taskId} funded by ${request.auth.uid}`);
    return { success: true };
  },
);

/**
 * releaseEscrow — client callable.
 * Transitions task → ACCEPTED, writes a RELEASED ledger entry,
 * and triggers the Flutterwave payout to the inspector.
 */
export const releaseEscrow = onCall(
  { region: REGION, secrets: ["FLW_SECRET_KEY"] },
  async (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "Must be signed in");

    const rawId = (request.data as { taskId?: unknown }).taskId;
    const taskId = typeof rawId === "string" ? rawId.trim() : "";
    if (!taskId) throw new HttpsError("invalid-argument", "taskId is required");

    const db = admin.firestore();
    const taskRef = db.doc(`tasks/${taskId}`);

    let inspectorUid = "";
    let inspectorPayoutUsd = 0;

    await db.runTransaction(async (tx) => {
      const snap = await tx.get(taskRef);
      const result = buildReleaseUpdate(
        snap.data() as ReleasableTask | undefined,
        taskId,
        request.auth!.uid,
        Date.now(),
      );
      tx.update(taskRef, result.taskUpdate);
      tx.set(db.collection("escrow_transactions").doc(), result.ledgerEntry);
      inspectorUid = result.inspectorUid;
      inspectorPayoutUsd = result.inspectorPayoutUsd;
    });

    await initiateInspectorPayout(db, admin.messaging(), inspectorUid, inspectorPayoutUsd, taskId);

    console.log(`Task ${taskId} escrow released — inspector ${inspectorUid} payout $${inspectorPayoutUsd}`);
    return { success: true, inspectorPayoutUsd };
  },
);
