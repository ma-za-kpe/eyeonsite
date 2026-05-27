import * as admin from "firebase-admin";

export interface FcmRecipient {
  /** Firestore document path where the `fcmToken` field lives, e.g. "inspectors/{uid}" */
  docPath: string;
  token: string;
}

const STALE_TOKEN_CODES = new Set([
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
]);

// Transient errors worth retrying with backoff
const TRANSIENT_CODES = new Set([
  "messaging/internal-error",
  "messaging/server-unavailable",
  "messaging/too-many-requests",
]);

const MAX_ATTEMPTS = 3;
const BASE_DELAY_MS = 200;

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Sends an FCM multicast with up to MAX_ATTEMPTS tries (exponential backoff)
 * for transient failures. Permanently stale/unregistered tokens are removed
 * from Firestore in a single batch after all send attempts complete.
 */
export async function sendAndCleanup(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  recipients: FcmRecipient[],
  notification: admin.messaging.Notification,
  data?: Record<string, string>,
): Promise<void> {
  if (recipients.length === 0) return;

  let pending = [...recipients];
  const stalePaths: string[] = [];

  for (let attempt = 0; attempt < MAX_ATTEMPTS && pending.length > 0; attempt++) {
    if (attempt > 0) {
      await sleep(BASE_DELAY_MS * 2 ** (attempt - 1)); // 200ms, 400ms
    }

    const { responses } = await messaging.sendEachForMulticast({
      tokens: pending.map((r) => r.token),
      notification,
      ...(data ? { data } : {}),
    });

    const nextPending: FcmRecipient[] = [];
    for (let i = 0; i < responses.length; i++) {
      const resp = responses[i];
      const recipient = pending[i];
      if (resp.success) continue;
      const code = resp.error?.code ?? "";
      if (STALE_TOKEN_CODES.has(code)) {
        stalePaths.push(recipient.docPath);
      } else if (TRANSIENT_CODES.has(code)) {
        nextPending.push(recipient);
      } else {
        console.warn(`FCM send failed (${recipient.docPath}): ${code}`);
      }
    }
    pending = nextPending;
  }

  if (pending.length > 0) {
    console.warn(`FCM: ${pending.length} recipient(s) unreachable after ${MAX_ATTEMPTS} attempts`);
  }

  if (stalePaths.length === 0) return;

  const batch = db.batch();
  stalePaths.forEach((path) =>
    batch.update(db.doc(path), { fcmToken: admin.firestore.FieldValue.delete() }),
  );
  await batch.commit();
  console.log(`Cleaned up ${stalePaths.length} stale FCM token(s)`);
}
