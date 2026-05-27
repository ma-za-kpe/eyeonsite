import * as admin from "firebase-admin";
import { sendAndCleanup, FcmRecipient } from "../fcm";

jest.useFakeTimers();

// ── helpers ───────────────────────────────────────────────────────────────────

function makeDb() {
  return {
    doc: jest.fn().mockReturnValue({ update: jest.fn() }),
    batch: jest.fn().mockReturnValue({
      update: jest.fn(),
      commit: jest.fn().mockResolvedValue(undefined),
    }),
  } as unknown as admin.firestore.Firestore;
}

function makeMessaging(responseSets: Array<Array<{ success: boolean; code?: string }>>) {
  let call = 0;
  const spy = jest.fn().mockImplementation(() => {
    const set = responseSets[call] ?? responseSets[responseSets.length - 1];
    const responses = set.map((r) => ({
      success: r.success,
      ...(r.code ? { error: { code: r.code } } : {}),
    }));
    call++;
    return Promise.resolve({ responses });
  });
  return { messaging: { sendEachForMulticast: spy } as unknown as admin.messaging.Messaging, spy };
}

const RECIPIENTS: FcmRecipient[] = [
  { docPath: "inspectors/a", token: "tok-a" },
  { docPath: "inspectors/b", token: "tok-b" },
];

const NOTIFICATION: admin.messaging.Notification = { title: "Test", body: "body" };

// ── sendAndCleanup ────────────────────────────────────────────────────────────

describe("sendAndCleanup", () => {
  afterEach(() => jest.clearAllMocks());

  test("does nothing when recipients list is empty", async () => {
    const { messaging, spy } = makeMessaging([[{ success: true }]]);
    await sendAndCleanup(makeDb(), messaging, [], NOTIFICATION);
    expect(spy).not.toHaveBeenCalled();
  });

  test("sends single multicast for all recipients on first attempt", async () => {
    const { messaging, spy } = makeMessaging([[{ success: true }, { success: true }]]);
    await sendAndCleanup(makeDb(), messaging, RECIPIENTS, NOTIFICATION);
    expect(spy).toHaveBeenCalledTimes(1);
    expect(spy.mock.calls[0][0].tokens).toEqual(["tok-a", "tok-b"]);
  });

  test("cleans up stale token when unregistered", async () => {
    const { messaging, spy } = makeMessaging([
      [
        { success: true },
        { success: false, code: "messaging/registration-token-not-registered" },
      ],
    ]);
    const db = makeDb();
    await sendAndCleanup(db, messaging, RECIPIENTS, NOTIFICATION);
    expect(spy).toHaveBeenCalledTimes(1);
    expect(db.batch).toHaveBeenCalled();
  });

  test("cleans up stale token when invalid-registration-token", async () => {
    const { messaging } = makeMessaging([
      [{ success: false, code: "messaging/invalid-registration-token" }],
    ]);
    const db = makeDb();
    await sendAndCleanup(db, messaging, [RECIPIENTS[0]], NOTIFICATION);
    expect(db.batch).toHaveBeenCalled();
  });

  test("retries transient server-unavailable up to MAX_ATTEMPTS then succeeds", async () => {
    const { messaging, spy } = makeMessaging([
      [{ success: false, code: "messaging/server-unavailable" }],
      [{ success: false, code: "messaging/server-unavailable" }],
      [{ success: true }],
    ]);
    const db = makeDb();
    const p = sendAndCleanup(db, messaging, [RECIPIENTS[0]], NOTIFICATION);
    await jest.runAllTimersAsync();
    await p;
    expect(spy).toHaveBeenCalledTimes(3);
    expect(db.batch).not.toHaveBeenCalled();
  });

  test("retries transient internal-error and succeeds on second attempt", async () => {
    const { messaging, spy } = makeMessaging([
      [{ success: false, code: "messaging/internal-error" }],
      [{ success: true }],
    ]);
    const db = makeDb();
    const p = sendAndCleanup(db, messaging, [RECIPIENTS[0]], NOTIFICATION);
    await jest.runAllTimersAsync();
    await p;
    expect(spy).toHaveBeenCalledTimes(2);
  });

  test("stops after MAX_ATTEMPTS and does not clean up transient failures", async () => {
    const { messaging, spy } = makeMessaging([
      [{ success: false, code: "messaging/server-unavailable" }],
      [{ success: false, code: "messaging/server-unavailable" }],
      [{ success: false, code: "messaging/server-unavailable" }],
    ]);
    const db = makeDb();
    const p = sendAndCleanup(db, messaging, [RECIPIENTS[0]], NOTIFICATION);
    await jest.runAllTimersAsync();
    await p;
    expect(spy).toHaveBeenCalledTimes(3);
    expect(db.batch).not.toHaveBeenCalled();
  });

  test("does not retry permanent stale-token errors", async () => {
    const { messaging, spy } = makeMessaging([
      [{ success: false, code: "messaging/registration-token-not-registered" }],
    ]);
    await sendAndCleanup(makeDb(), messaging, [RECIPIENTS[0]], NOTIFICATION);
    expect(spy).toHaveBeenCalledTimes(1);
  });

  test("forwards data payload to sendEachForMulticast", async () => {
    const { messaging, spy } = makeMessaging([[{ success: true }]]);
    await sendAndCleanup(makeDb(), messaging, [RECIPIENTS[0]], NOTIFICATION, { taskId: "t1" });
    expect(spy.mock.calls[0][0].data).toEqual({ taskId: "t1" });
  });

  test("handles mixed success + stale + transient in one pass", async () => {
    // Attempt 1: a→success, b→stale, c→transient; Attempt 2: c→success
    const c: FcmRecipient = { docPath: "inspectors/c", token: "tok-c" };
    const { messaging, spy } = makeMessaging([
      [
        { success: true },
        { success: false, code: "messaging/registration-token-not-registered" },
        { success: false, code: "messaging/server-unavailable" },
      ],
      [{ success: true }],
    ]);
    const db = makeDb();
    const p = sendAndCleanup(db, messaging, [...RECIPIENTS, c], NOTIFICATION);
    await jest.runAllTimersAsync();
    await p;
    expect(spy).toHaveBeenCalledTimes(2);
    expect(spy.mock.calls[1][0].tokens).toEqual(["tok-c"]);
    expect(db.batch).toHaveBeenCalled(); // stale cleanup for b
  });
});
