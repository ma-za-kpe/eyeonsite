import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { FcmRecipient, sendAndCleanup } from "../utils/fcm";
import { TaskDocument } from "./types";

/** Sweeps POSTED tasks whose expiresAtMs is in the past and marks them EXPIRED. */
// Cloud Scheduler is not available in africa-south1; europe-west1 is nearest supported region.
export const expireStaleTasks = onSchedule(
  { schedule: "every 5 minutes", region: "europe-west1" },
  async () => {
    const db = admin.firestore();
    const now = Date.now();

    const snap = await db
      .collection("tasks")
      .where("status", "==", "POSTED")
      .where("expiresAtMs", "<=", now)
      .get();

    if (snap.empty) return;

    // TODO(production): chunk into groups of 500 — Firestore batches hard-fail above 500 writes.
    const batch = db.batch();
    snap.docs.forEach((doc) => batch.update(doc.ref, { status: "EXPIRED" }));
    await batch.commit();

    await notifyExpiredClients(db, admin.messaging(), snap.docs);

    console.log(`Expired ${snap.size} stale task(s)`);
  },
);

async function notifyExpiredClients(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging,
  taskDocs: admin.firestore.QueryDocumentSnapshot[],
): Promise<void> {
  // Resolve unique client UIDs from expired tasks
  const clientUids = [
    ...new Set(
      taskDocs
        .map((d) => (d.data() as TaskDocument).clientUid)
        .filter((uid): uid is string => !!uid),
    ),
  ];
  if (clientUids.length === 0) return;

  // Look up FCM tokens from users/{uid} (written by EyeOnSiteMessagingService on token refresh)
  const userSnaps = await Promise.all(clientUids.map((uid) => db.doc(`users/${uid}`).get()));
  const uidToToken = new Map<string, string>(
    userSnaps
      .map((s, i) => {
        const token = (s.data() as { fcmToken?: string } | undefined)?.fcmToken;
        return token ? ([clientUids[i], token] as [string, string]) : null;
      })
      .filter((e): e is [string, string] => e !== null),
  );

  const recipients: FcmRecipient[] = [...uidToToken.entries()].map(([uid, token]) => ({
    docPath: `users/${uid}`,
    token,
  }));

  await sendAndCleanup(
    db,
    messaging,
    recipients,
    {
      title: "No inspector available",
      body: "Try posting your task again — more inspectors are on the way",
    },
    { type: "TASK_EXPIRED" },
  );
}
