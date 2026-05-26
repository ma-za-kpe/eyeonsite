import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";

const REGION = "africa-south1";
const INSPECTOR_SHARE = 0.8; // inspector receives 80 %, platform keeps 20 %

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
  const bounty = task.bountyUsd ?? 0;
  const escrowFee = Math.round(bounty * 0.02 * 100) / 100;

  return {
    taskUpdate: {
      status: "ESCROW_FUNDED",
      escrowFundedAtMs: nowMs,
      escrowAmountUsd: bounty,
    },
    ledgerEntry: {
      txType: "ESCROW_HOLD",
      taskId,
      clientUid: callerUid,
      amountUsd: bounty,
      escrowFeeUsd: escrowFee,
      platformFeeUsd: Math.round(bounty * 0.18 * 100) / 100,
      inspectorShareUsd: Math.round(bounty * INSPECTOR_SHARE * 100) / 100,
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

  const bounty = task.bountyUsd ?? 0;
  const inspectorPayoutUsd = Math.round(bounty * INSPECTOR_SHARE * 100) / 100;

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
      amountUsd: bounty,
      inspectorPayoutUsd,
      platformFeeUsd: Math.round(bounty * 0.2 * 100) / 100,
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
 * and notifies the inspector of their payout.
 * Demo MVP: no real Flutterwave payout (FR-1) — just the status change.
 */
export const releaseEscrow = onCall(
  { region: REGION },
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

    await notifyInspectorPayout(db, admin.messaging(), inspectorUid, taskId, inspectorPayoutUsd);

    console.log(`Task ${taskId} escrow released — inspector ${inspectorUid} payout $${inspectorPayoutUsd}`);
    return { success: true, inspectorPayoutUsd };
  },
);

async function notifyInspectorPayout(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  inspectorUid: string,
  taskId: string,
  payoutUsd: number,
): Promise<void> {
  const snap = await db.doc(`inspectors/${inspectorUid}`).get();
  const token = (snap.data() as { fcmToken?: string } | undefined)?.fcmToken;
  if (!token) return;

  await messaging.send({
    token,
    notification: {
      title: "Payment released",
      body: `$${payoutUsd.toFixed(2)} is on its way to you for task ${taskId}.`,
    },
    data: { taskId, type: "ESCROW_RELEASED", payoutUsd: String(payoutUsd) },
  });
}
