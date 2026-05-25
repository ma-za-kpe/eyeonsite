import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import * as nodemailer from "nodemailer";

admin.initializeApp();

// HTML-escape for email templates — prevents XSS in notification emails.
function escHtml(str: unknown): string {
  return String(str ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

// Sanitise stored Firestore data before use (truncates + strips leading whitespace).
function sanitiseWaitlistData(data: Record<string, unknown>): Record<string, string> {
  return {
    name:     String(data.name     ?? "").slice(0, 120).trim(),
    email:    String(data.email    ?? "").slice(0, 200).trim(),
    role:     String(data.role     ?? "").trim(),
    location: String(data.location ?? "").slice(0, 120).trim(),
    message:  String(data.message  ?? "").slice(0, 500).trim(),
  };
}

// Triggered every time a new document is added to the `waitlist` collection.
// Sends a notification email to the founder at makpalyy@gmail.com.
//
// Required secret (set once via CLI):
//   firebase functions:secrets:set GMAIL_APP_PASSWORD
export const notifyWaitlistSignup = onDocumentCreated(
  {
    document: "waitlist/{docId}",
    secrets: ["GMAIL_APP_PASSWORD"],
    region: "us-central1",
  },
  async (event) => {
    const rawData = event.data?.data();
    if (!rawData) return;
    const data = sanitiseWaitlistData(rawData as Record<string, unknown>);

    const appPassword = process.env.GMAIL_APP_PASSWORD;
    if (!appPassword) {
      console.error("GMAIL_APP_PASSWORD secret not set — skipping email.");
      return;
    }

    const transporter = nodemailer.createTransport({
      service: "gmail",
      auth: {
        user: "makpalyy@gmail.com",
        pass: appPassword,
      },
    });

    const roleLabel = data.role === "diaspora_client"
      ? "Diaspora Client (needs verification)"
      : "Field Agent (wants to inspect)";

    const html = `
      <div style="font-family:sans-serif;max-width:560px;margin:0 auto;padding:24px">
        <div style="background:#022B1E;padding:20px 24px;border-radius:8px 8px 0 0">
          <span style="color:#34D399;font-size:20px;font-weight:600">EyeOn</span><span style="color:#fff;font-size:20px;font-weight:600">Site</span>
          <span style="color:#D4A843;font-size:12px;letter-spacing:2px;display:block;margin-top:4px">NEW WAITLIST SIGNUP</span>
        </div>
        <div style="background:#f5f4f0;padding:24px;border-radius:0 0 8px 8px;border:1px solid #e8e6dd">
          <table style="width:100%;border-collapse:collapse">
            <tr><td style="padding:8px 0;color:#4a4a47;width:140px">Name</td><td style="padding:8px 0;font-weight:600;color:#1a1a19">${escHtml(data.name)}</td></tr>
            <tr><td style="padding:8px 0;color:#4a4a47">Email</td><td style="padding:8px 0"><a href="mailto:${escHtml(data.email)}" style="color:#0b6e4f">${escHtml(data.email)}</a></td></tr>
            <tr><td style="padding:8px 0;color:#4a4a47">Role</td><td style="padding:8px 0;font-weight:600;color:#1a1a19">${roleLabel}</td></tr>
            <tr><td style="padding:8px 0;color:#4a4a47">Location</td><td style="padding:8px 0;color:#1a1a19">${escHtml(data.location)}</td></tr>
            ${data.message ? `<tr><td style="padding:8px 0;color:#4a4a47;vertical-align:top">Message</td><td style="padding:8px 0;color:#1a1a19">${escHtml(data.message)}</td></tr>` : ""}
          </table>
          <hr style="border:none;border-top:1px solid #e8e6dd;margin:16px 0">
          <a href="https://console.firebase.google.com/project/eyeonsite-3b8e0/firestore/data/waitlist"
             style="background:#0b6e4f;color:#fff;padding:10px 20px;border-radius:6px;text-decoration:none;font-size:14px">
            View in Firestore →
          </a>
        </div>
      </div>
    `;

    await transporter.sendMail({
      from: '"EyeOnSite" <makpalyy@gmail.com>',
      to: "makpalyy@gmail.com",
      subject: `🔔 New waitlist signup — ${data.name} (${roleLabel})`,
      html,
    });

    console.log(`Waitlist email sent for: ${data.name} <${data.email}>`);
  }
);
