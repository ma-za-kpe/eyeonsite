import * as admin from "firebase-admin";
import * as crypto from "crypto";
import { onRequest } from "firebase-functions/v2/https";

const ALLOWED_ORIGINS = new Set([
  "https://geteyeonsite.com",
  "https://www.geteyeonsite.com",
  "https://eyeonsite-3b8e0.web.app",
  "http://localhost:8080",
  "http://127.0.0.1:8080",
]);
const COOLDOWN_MS = 10 * 60 * 1000;

function corsHeaders(origin: string | null): Record<string, string> {
  const allowOrigin = origin && ALLOWED_ORIGINS.has(origin) ? origin : "https://geteyeonsite.com";
  return {
    "Access-Control-Allow-Origin": allowOrigin,
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type",
    "Access-Control-Max-Age": "3600",
    "Vary": "Origin",
  };
}

function normalise(text: unknown, maxLen: number): string {
  return String(text ?? "").trim().slice(0, maxLen);
}

function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function hashValue(value: string): string {
  return crypto.createHash("sha256").update(value).digest("hex");
}

function getClientIp(req: { get(header: string): string | undefined; ip?: string }): string {
  const forwarded = String(req.get("x-forwarded-for") ?? "");
  const first = forwarded.split(",")[0]?.trim();
  return first || req.ip || "unknown";
}

export const submitWaitlist = onRequest(
  {
    region: "africa-south1",
    invoker: "public",
  },
  async (req, res) => {
    const origin = req.get("origin") ?? null;
    Object.entries(corsHeaders(origin)).forEach(([key, value]) => res.setHeader(key, value));

    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }

    if (req.method !== "POST") {
      res.status(405).json({ error: "Method not allowed" });
      return;
    }

    if (origin && !ALLOWED_ORIGINS.has(origin)) {
      res.status(403).json({ error: "Origin not allowed" });
      return;
    }

    const body = typeof req.body === "string" ? JSON.parse(req.body) : req.body ?? {};
    const name = normalise(body.name, 120);
    const email = normalise(body.email, 200);
    const role = normalise(body.role, 32);
    const location = normalise(body.location, 120);
    const message = normalise(body.message, 500);
    const source = normalise(body.source, 64) || "web_landing";
    const companyTrap = normalise(body.company, 64);

    if (companyTrap) {
      res.status(200).json({ ok: true });
      return;
    }

    if (!name || !email || !role || !location) {
      res.status(400).json({ error: "Missing required fields" });
      return;
    }

    if (!isValidEmail(email)) {
      res.status(400).json({ error: "Invalid email" });
      return;
    }

    if (!["diaspora_client", "field_agent"].includes(role)) {
      res.status(400).json({ error: "Invalid role" });
      return;
    }

    const db = admin.firestore();
    const now = Date.now();
    const emailHash = hashValue(email.toLowerCase());
    const ipHash = hashValue(getClientIp(req));
    const emailGuardRef = db.collection("waitlist_throttle").doc(`email_${emailHash}`);
    const ipGuardRef = db.collection("waitlist_throttle").doc(`ip_${ipHash}`);

    const duplicateEmail = await db.collection("waitlist")
      .where("email", "==", email)
      .limit(1)
      .get();

    if (!duplicateEmail.empty) {
      res.status(409).json({ error: "Email already registered" });
      return;
    }

    try {
      await db.runTransaction(async (tx) => {
        const [emailGuardSnap, ipGuardSnap] = await Promise.all([
          tx.get(emailGuardRef),
          tx.get(ipGuardRef),
        ]);

        const emailLast = Number(emailGuardSnap.get("lastSubmittedAtMs") ?? 0);
        const ipLast = Number(ipGuardSnap.get("lastSubmittedAtMs") ?? 0);

        if (now - emailLast < COOLDOWN_MS || now - ipLast < COOLDOWN_MS) {
          throw new Error("Rate limit exceeded");
        }

        const waitlistRef = db.collection("waitlist").doc();
        tx.set(waitlistRef, {
          name,
          email,
          role,
          location,
          message,
          source,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        tx.set(emailGuardRef, {
          lastSubmittedAtMs: now,
          email,
        }, { merge: true });

        tx.set(ipGuardRef, {
          lastSubmittedAtMs: now,
          ipHash,
        }, { merge: true });
      });
    } catch (error) {
      const messageText = String((error as Error)?.message ?? error);
      if (messageText.includes("Rate limit exceeded")) {
        res.status(429).json({ error: "Too many requests" });
        return;
      }
      throw error;
    }

    res.status(200).json({ ok: true });
  }
);
