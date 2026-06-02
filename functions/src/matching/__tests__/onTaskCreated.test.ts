import { GeoPoint } from "firebase-admin/firestore";
import { queryAvailableInspectors, rankCandidates, sendTaskPings } from "../onTaskCreated";
import { InspectorDocument, TaskDocument } from "../types";
import { h3CoverageForPoint } from "../../utils/h3";

// ── helpers ───────────────────────────────────────────────────────────────────

function makeInspector(lat: number, lng: number, fcmToken?: string): { uid: string; doc: InspectorDocument } {
  return {
    uid: `inspector-${lat}-${lng}`,
    doc: { available: true, location: new GeoPoint(lat, lng), fcmToken },
  };
}

const TASK_LAT = 5.5502;
const TASK_LNG = -0.2174;
const RADIUS_KM = 10;

// ── rankCandidates ────────────────────────────────────────────────────────────

describe("rankCandidates", () => {
  test("returns empty array when no inspectors", () => {
    expect(rankCandidates(TASK_LAT, TASK_LNG, RADIUS_KM, [])).toEqual([]);
  });

  test("excludes inspectors outside radius", () => {
    // ~56 km away (Kumasi direction)
    const farInspector = makeInspector(6.0, -0.2174);
    const result = rankCandidates(TASK_LAT, TASK_LNG, RADIUS_KM, [farInspector]);
    expect(result).toHaveLength(0);
  });

  test("includes inspectors within radius", () => {
    // ~5 km away
    const nearInspector = makeInspector(5.595, -0.217);
    const result = rankCandidates(TASK_LAT, TASK_LNG, RADIUS_KM, [nearInspector]);
    expect(result).toHaveLength(1);
  });

  test("sorts by distance ascending", () => {
    const near = makeInspector(5.555, -0.217);  // ~0.6 km
    const mid = makeInspector(5.580, -0.217);   // ~3.3 km
    const result = rankCandidates(TASK_LAT, TASK_LNG, RADIUS_KM, [mid, near]);
    expect(result[0].distanceKm).toBeLessThan(result[1].distanceKm);
  });

  test("caps candidates at 3", () => {
    const inspectors = [
      makeInspector(5.553, -0.217),
      makeInspector(5.556, -0.217),
      makeInspector(5.559, -0.217),
      makeInspector(5.562, -0.217),
      makeInspector(5.565, -0.217),
    ];
    const result = rankCandidates(TASK_LAT, TASK_LNG, RADIUS_KM, inspectors);
    expect(result).toHaveLength(3);
  });

  test("preserves fcmToken on candidates", () => {
    const inspector = makeInspector(5.555, -0.217, "fcm-token-123");
    const result = rankCandidates(TASK_LAT, TASK_LNG, RADIUS_KM, [inspector]);
    expect(result[0].fcmToken).toBe("fcm-token-123");
  });

  test("skips inspectors with no location", () => {
    const noLocation = {
      uid: "no-loc",
      doc: { available: true, location: null as unknown as GeoPoint },
    };
    const result = rankCandidates(TASK_LAT, TASK_LNG, RADIUS_KM, [noLocation]);
    expect(result).toHaveLength(0);
  });
});

// ── sendTaskPings ─────────────────────────────────────────────────────────────

function makeInspectorDoc(id: string, doc: InspectorDocument) {
  return { id, data: () => doc };
}

function makeQueryRecorder(docsByField: Record<string, unknown[]> = {}) {
  type FakeQuery = {
    where: (field: string, op: string, value: unknown) => FakeQuery;
    get: () => Promise<{ docs: unknown[] }>;
  };

  const calls: Array<{ field: string; op: string; value: unknown }> = [];
  const query: FakeQuery = {
    where: jest.fn((field: string, op: string, value: unknown): FakeQuery => {
      calls.push({ field, op, value });
      return query;
    }),
    get: jest.fn(async (): Promise<{ docs: unknown[] }> => {
      const lastField = calls[calls.length - 1]?.field ?? "";
      return { docs: docsByField[lastField] ?? [] };
    }),
  };
  const db = {
    collection: jest.fn(() => query),
  };
  return { db: db as any, calls };
}

describe("queryAvailableInspectors", () => {
  test("queries H3 cells before geohash fallback", async () => {
    const coverage = h3CoverageForPoint(TASK_LAT, TASK_LNG);
    const inspector = makeInspectorDoc("inspector-h3", makeInspector(TASK_LAT, TASK_LNG).doc);
    const { db, calls } = makeQueryRecorder({ h3Res9: [inspector] });

    const result = await queryAvailableInspectors(db, TASK_LAT, TASK_LNG, {
      h3Res9: coverage.h3Res9,
      h3NeighborRes9: coverage.h3NeighborRes9,
    });

    expect(result.map((item) => item.uid)).toEqual(["inspector-h3"]);
    expect(calls).toContainEqual({ field: "available", op: "==", value: true });
    expect(calls).toContainEqual({
      field: "h3Res9",
      op: "in",
      value: [coverage.h3Res9, ...coverage.h3NeighborRes9],
    });
    expect(calls.some((call) => call.field === "geohash")).toBe(false);
  });

  test("falls back to geohash during migration when H3 has no matches", async () => {
    const inspector = makeInspectorDoc("inspector-geohash", makeInspector(TASK_LAT, TASK_LNG).doc);
    const { db, calls } = makeQueryRecorder({ geohash: [inspector] });

    const result = await queryAvailableInspectors(db, TASK_LAT, TASK_LNG, {});

    expect(result.map((item) => item.uid)).toEqual(["inspector-geohash"]);
    expect(calls.some((call) => call.field === "h3Res9")).toBe(true);
    expect(calls.some((call) => call.field === "geohash")).toBe(true);
  });
});

function makeDb() {
  return { doc: jest.fn().mockReturnValue({ update: jest.fn().mockResolvedValue(undefined) }), batch: jest.fn() } as any;
}

function makeMessaging(allSucceed = true) {
  return {
    sendEachForMulticast: jest.fn().mockResolvedValue({
      responses: allSucceed
        ? [{ success: true }, { success: true }]
        : [{ success: false, error: { code: "messaging/registration-token-not-registered" } }],
    }),
  } as any;
}

describe("sendTaskPings", () => {
  const mockTask: TaskDocument = {
    status: "POSTED",
    location: new GeoPoint(TASK_LAT, TASK_LNG),
    locationRadiusKm: RADIUS_KM,
    bountyUsd: 45,
    clientUid: "client-001",
  };

  test("does nothing when no candidates have fcmToken", async () => {
    const mockMessaging = makeMessaging();
    const candidates = [{ uid: "a", distanceKm: 1 }]; // no fcmToken
    await sendTaskPings(makeDb(), mockMessaging, "task-1", mockTask, candidates);
    expect(mockMessaging.sendEachForMulticast).not.toHaveBeenCalled();
  });

  test("sends to all candidates with tokens", async () => {
    const mockMessaging = makeMessaging();
    const candidates = [
      { uid: "a", fcmToken: "tok-1", distanceKm: 1.2 },
      { uid: "b", fcmToken: "tok-2", distanceKm: 3.4 },
    ];
    await sendTaskPings(makeDb(), mockMessaging, "task-1", mockTask, candidates);
    const call = mockMessaging.sendEachForMulticast.mock.calls[0][0];
    expect(call.tokens).toEqual(["tok-1", "tok-2"]);
  });

  test("notification body includes bounty and distance", async () => {
    const mockMessaging = makeMessaging();
    const candidates = [{ uid: "a", fcmToken: "tok", distanceKm: 2.567 }];
    await sendTaskPings(makeDb(), mockMessaging, "task-1", mockTask, candidates);
    const notification = mockMessaging.sendEachForMulticast.mock.calls[0][0].notification;
    expect(notification.body).toContain("45");
    expect(notification.body).toContain("2.6");
  });

  test("data payload contains taskId and type", async () => {
    const mockMessaging = makeMessaging();
    const candidates = [{ uid: "a", fcmToken: "tok", distanceKm: 1 }];
    await sendTaskPings(makeDb(), mockMessaging, "task-xyz", mockTask, candidates);
    const data = mockMessaging.sendEachForMulticast.mock.calls[0][0].data;
    expect(data.taskId).toBe("task-xyz");
    expect(data.type).toBe("TASK_PING");
  });
});
