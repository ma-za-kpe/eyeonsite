import * as admin from "firebase-admin";
import { onCall, onRequest, HttpsError } from "firebase-functions/v2/https";
import Stripe from "stripe";
import { buildFundUpdate } from "../escrow/escrow";
import type { FundableTask } from "../escrow/escrow";

const REGION = "africa-south1";

/**
 * createPaymentIntent — client callable.
 * Creates a Stripe PaymentIntent for the task bounty and returns the clientSecret
 * so the mobile app can present the Stripe payment sheet.
 * Requires: STRIPE_SECRET_KEY Firebase secret.
 */
export const createPaymentIntent = onCall(
  { region: REGION, enforceAppCheck: true, secrets: ["STRIPE_SECRET_KEY"] },
  async (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "Must be signed in");

    const rawId = (request.data as { taskId?: unknown }).taskId;
    const taskId = typeof rawId === "string" ? rawId.trim() : "";
    if (!taskId) throw new HttpsError("invalid-argument", "taskId is required");

    const db = admin.firestore();
    const snap = await db.doc(`tasks/${taskId}`).get();
    const task = snap.data() as FundableTask | undefined;

    if (!task) throw new HttpsError("not-found", "Task not found");
    if (task.clientUid !== request.auth.uid) {
      throw new HttpsError("permission-denied", "Only the task owner can fund escrow");
    }
    if (task.status !== "POSTED") {
      throw new HttpsError(
        "failed-precondition",
        `Task must be POSTED to fund escrow, current: ${task.status}`,
      );
    }

    const stripe = new Stripe(process.env.STRIPE_SECRET_KEY!, {
      apiVersion: "2024-06-20",
    });

    const amountCents = Math.round((task.bountyUsd ?? 0) * 100);
    if (amountCents < 50) {
      throw new HttpsError("invalid-argument", "Bounty must be at least $0.50");
    }

    const paymentIntent = await stripe.paymentIntents.create({
      amount: amountCents,
      currency: "usd",
      metadata: { taskId, clientUid: request.auth.uid },
      automatic_payment_methods: { enabled: true },
    });

    console.log(`PaymentIntent ${paymentIntent.id} created for task ${taskId} ($${task.bountyUsd})`);
    return {
      clientSecret: paymentIntent.client_secret,
      paymentIntentId: paymentIntent.id,
    };
  },
);

/**
 * stripeWebhook — HTTP trigger.
 * Handles payment_intent.succeeded from Stripe. Transitions task POSTED → ESCROW_FUNDED
 * and writes the double-entry ledger entry. All other event types are ignored.
 * Requires: STRIPE_SECRET_KEY, STRIPE_WEBHOOK_SECRET Firebase secrets.
 */
export const stripeWebhook = onRequest(
  { region: REGION, secrets: ["STRIPE_SECRET_KEY", "STRIPE_WEBHOOK_SECRET"] },
  async (req, res) => {
    const sig = req.headers["stripe-signature"];
    if (!sig) {
      res.status(400).send("Missing stripe-signature header");
      return;
    }

    // Firebase Functions v2 exposes the pre-parsed raw body buffer on req.rawBody.
    const rawBody = (req as unknown as { rawBody?: Buffer }).rawBody;
    if (!rawBody) {
      res.status(400).send("rawBody unavailable — ensure functions.https.rawBody is enabled");
      return;
    }

    const stripe = new Stripe(process.env.STRIPE_SECRET_KEY!, {
      apiVersion: "2024-06-20",
    });

    let event: Stripe.Event;
    try {
      event = stripe.webhooks.constructEvent(
        rawBody,
        sig,
        process.env.STRIPE_WEBHOOK_SECRET!,
      );
    } catch (err) {
      console.error("Stripe webhook signature verification failed:", err);
      res.status(400).send(`Webhook error: ${String(err)}`);
      return;
    }

    if (event.type !== "payment_intent.succeeded") {
      res.status(200).send("Event type ignored");
      return;
    }

    const paymentIntent = event.data.object as Stripe.PaymentIntent;
    const { taskId, clientUid } = paymentIntent.metadata ?? {};
    if (!taskId || !clientUid) {
      res.status(400).send("Missing taskId or clientUid in PaymentIntent metadata");
      return;
    }

    const db = admin.firestore();
    const taskRef = db.doc(`tasks/${taskId}`);

    try {
      await db.runTransaction(async (tx) => {
        const snap = await tx.get(taskRef);
        const { taskUpdate, ledgerEntry } = buildFundUpdate(
          snap.data() as FundableTask | undefined,
          taskId,
          clientUid,
          Date.now(),
        );
        tx.update(taskRef, { ...taskUpdate, stripePaymentIntentId: paymentIntent.id });
        tx.set(db.collection("escrow_transactions").doc(), ledgerEntry);
      });
    } catch (err) {
      console.error(`Failed to fund task ${taskId} from Stripe webhook:`, err);
      res.status(500).send("Failed to update task");
      return;
    }

    console.log(`Stripe webhook: task ${taskId} funded via PaymentIntent ${paymentIntent.id}`);
    res.status(200).send("OK");
  },
);
