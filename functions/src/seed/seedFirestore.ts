import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { GeoPoint } from "firebase-admin/firestore";
import { geohashForPoint } from "../utils/geohash";
import { h3CoverageForPoint } from "../utils/h3";

const REGION = "africa-south1";
const ALLOWED_EMAIL = "makpalyy@gmail.com";

// ── Sample data ────────────────────────────────────────────────────────────────

const INSPECTORS = [
  {
    name: "Kwame Asante",
    location: new GeoPoint(5.6037, -0.187),
    city: "East Legon, Accra",
    available: true,
    tierLevel: 1,
    completedTasks: 14,
    rating: 4.8,
    fcmToken: "",
  },
  {
    name: "Ama Darko",
    location: new GeoPoint(5.565, -0.1735),
    city: "Labone, Accra",
    available: true,
    tierLevel: 1,
    completedTasks: 9,
    rating: 4.6,
    fcmToken: "",
  },
  {
    name: "Kofi Mensah",
    location: new GeoPoint(5.6081, -0.174),
    city: "Airport Residential, Accra",
    available: false,
    tierLevel: 2,
    completedTasks: 31,
    rating: 4.9,
    fcmToken: "",
  },
  {
    name: "Adwoa Boateng",
    location: new GeoPoint(5.5584, -0.1863),
    city: "Osu, Accra",
    available: true,
    tierLevel: 1,
    completedTasks: 5,
    rating: 4.4,
    fcmToken: "",
  },
  {
    name: "Yaw Osei",
    location: new GeoPoint(5.568, -0.1778),
    city: "Cantonments, Accra",
    available: true,
    tierLevel: 1,
    completedTasks: 20,
    rating: 4.7,
    fcmToken: "",
  },
];

const TASKS = [
  {
    type: "CONSTRUCTION",
    location: new GeoPoint(5.6037, -0.187),
    locationLabel: "East Legon, Accra",
    description: "Verify floor slab and column placement on floors 3–5",
    status: "POSTED",
    bountyUsd: 350,
    clientUid: "demo-client-001",
    createdAtMs: Date.now() - 2 * 60 * 60 * 1000, // 2 h ago
    locationRadiusKm: 0.3,
  },
  {
    type: "PROPERTY",
    location: new GeoPoint(5.565, -0.1735),
    locationLabel: "Labone, Accra",
    description: "Inspect 4-bedroom detached house — confirm occupancy and structural condition",
    status: "INSPECTOR_MATCHED",
    bountyUsd: 120,
    clientUid: "demo-client-001",
    createdAtMs: Date.now() - 5 * 60 * 60 * 1000, // 5 h ago
    locationRadiusKm: 0.2,
  },
  {
    type: "FARM",
    location: new GeoPoint(5.45, -0.21),
    locationLabel: "Accra South",
    description: "Verify 5-acre maize farm — growth stage, pest signs, irrigation channels",
    status: "EVIDENCE_SUBMITTED",
    bountyUsd: 80,
    clientUid: "demo-client-001",
    createdAtMs: Date.now() - 26 * 60 * 60 * 1000, // 26 h ago
    locationRadiusKm: 1.0,
  },
];

// ── Callable function ──────────────────────────────────────────────────────────

export const seedFirestore = onCall(
  { region: REGION, enforceAppCheck: true },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Must be signed in");
    }
    if (request.auth.token.email !== ALLOWED_EMAIL) {
      throw new HttpsError("permission-denied", "Seed is restricted to the founder account");
    }

    const db = admin.firestore();
    const results: { inspectors: string[]; tasks: string[] } = {
      inspectors: [],
      tasks: [],
    };

    // Create inspectors
    for (const inspector of INSPECTORS) {
      const { latitude, longitude } = inspector.location;
      const h3Coverage = h3CoverageForPoint(latitude, longitude);
      const ref = await db.collection("inspectors").add({
        ...inspector,
        geohash: geohashForPoint(latitude, longitude),
        ...h3Coverage,
        createdAtMs: Date.now(),
      });
      results.inspectors.push(ref.id);
    }

    // Create tasks
    for (const task of TASKS) {
      const { latitude, longitude } = task.location;
      const ref = await db.collection("tasks").add({
        ...task,
        ...h3CoverageForPoint(latitude, longitude),
      });
      results.tasks.push(ref.id);
    }

    console.log(`Seed complete: ${results.inspectors.length} inspectors, ${results.tasks.length} tasks`);
    return results;
  },
);
