import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { sendAndCleanup } from "../utils/fcm";

export const onInspectorApplied = onDocumentCreated(
  { document: "inspectors/{uid}", region: "africa-south1" },
  async (event) => {
    const data = event.data?.data();
    if (!data) return;

    const uid = event.params.uid;
    const status = String(data.onboardingStatus ?? "");
    if (status !== "PENDING_REVIEW") return;

    const db = admin.firestore();
    const messaging = admin.messaging();

    const fcmToken = data.fcmToken as string | undefined;
    if (fcmToken) {
      await sendAndCleanup(
        db,
        messaging,
        [{ docPath: `inspectors/${uid}`, token: fcmToken }],
        {
          title: "Application Received",
          body: "Your inspector application is under review. We'll notify you within 24–48 hours.",
        },
        { type: "ONBOARDING_SUBMITTED" },
      );
    }

    console.log(
      `Inspector application received: ${uid} from ${String(data.city ?? "unknown")}`,
    );
  },
);
