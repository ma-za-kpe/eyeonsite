import * as admin from "firebase-admin";

const FLW_TRANSFERS_URL = "https://api.flutterwave.com/v3/transfers";

interface InspectorPayoutDoc {
  fcmToken?: string;
  payoutAccountBank?: string;
  payoutAccountNumber?: string;
  payoutCurrency?: string;
}

/**
 * Initiates a Flutterwave Transfer to the inspector's mobile money / bank account,
 * then sends an FCM push notification confirming the payout.
 *
 * Inspector payout details must be stored on their Firestore doc:
 *   inspectors/{uid}.payoutAccountBank  — e.g. "MPS" (MTN MoMo), "MPESA" (M-Pesa)
 *   inspectors/{uid}.payoutAccountNumber — phone or account number
 *   inspectors/{uid}.payoutCurrency      — e.g. "GHS", "KES", "NGN", "UGX"
 *
 * If payout details are missing, writes to pending_payouts for manual processing.
 * Requires: FLW_SECRET_KEY Firebase secret.
 */
export async function initiateInspectorPayout(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  inspectorUid: string,
  amountUsd: number,
  taskId: string,
): Promise<void> {
  const snap = await db.doc(`inspectors/${inspectorUid}`).get();
  const data = snap.data() as InspectorPayoutDoc | undefined;

  const hasPayoutDetails =
    data?.payoutAccountBank && data?.payoutAccountNumber && data?.payoutCurrency;

  if (!hasPayoutDetails) {
    console.warn(
      `Inspector ${inspectorUid} has no payout details — queuing task ${taskId} for manual processing`,
    );
    await db.collection("pending_payouts").add({
      inspectorUid,
      amountUsd,
      taskId,
      reason: "NO_PAYOUT_DETAILS",
      createdAtMs: Date.now(),
    });
    await notifyPayout(messaging, data?.fcmToken, taskId, amountUsd, /* isPending */ true);
    return;
  }

  const flwKey = process.env.FLW_SECRET_KEY;
  if (!flwKey) {
    console.error("FLW_SECRET_KEY not configured — skipping payout for task", taskId);
    return;
  }

  const reference = `EOS-${taskId}-${Date.now()}`;

  const response = await fetch(FLW_TRANSFERS_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${flwKey}`,
    },
    body: JSON.stringify({
      account_bank: data!.payoutAccountBank,
      account_number: data!.payoutAccountNumber,
      amount: amountUsd,
      currency: data!.payoutCurrency,
      debit_currency: "USD",
      reference,
      narration: `EyeOnSite payout — task ${taskId}`,
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error(`Flutterwave payout failed for task ${taskId} (${response.status}): ${errorText}`);
    await db.collection("pending_payouts").add({
      inspectorUid,
      amountUsd,
      taskId,
      reason: "FLW_API_ERROR",
      flwStatus: response.status,
      flwError: errorText,
      createdAtMs: Date.now(),
    });
    await notifyPayout(messaging, data?.fcmToken, taskId, amountUsd, /* isPending */ true);
    return;
  }

  const result = (await response.json()) as { data?: { id?: number } };
  const transferId = String(result.data?.id ?? "");

  await db.doc(`tasks/${taskId}`).update({
    flutterwaveTransferId: transferId,
    payoutInitiatedAtMs: Date.now(),
  });

  console.log(
    `Flutterwave transfer ${transferId} initiated — inspector ${inspectorUid} payout $${amountUsd} for task ${taskId}`,
  );
  await notifyPayout(messaging, data?.fcmToken, taskId, amountUsd, /* isPending */ false);
}

async function notifyPayout(
  messaging: admin.messaging.Messaging,
  fcmToken: string | undefined,
  taskId: string,
  amountUsd: number,
  isPending: boolean,
): Promise<void> {
  if (!fcmToken) return;

  const body = isPending
    ? `$${amountUsd.toFixed(2)} payout for task ${taskId} is being processed — we'll update you shortly.`
    : `$${amountUsd.toFixed(2)} is on its way to your account for task ${taskId}.`;

  try {
    await messaging.send({
      token: fcmToken,
      notification: { title: "Payment released", body },
      data: {
        taskId,
        type: "ESCROW_RELEASED",
        payoutUsd: String(amountUsd),
        isPending: String(isPending),
      },
    });
  } catch (err) {
    console.warn(`FCM payout notification failed for task ${taskId}:`, err);
  }
}
