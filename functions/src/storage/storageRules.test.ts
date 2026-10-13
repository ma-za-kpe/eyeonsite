import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
  RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import { serverTimestamp } from "firebase/firestore";
import * as fs from "fs";
import * as path from "path";

const PROJECT_ID = "demo-eyeonsite";
const TASK_ID = "task-alpha";
const CLIENT_UID = "client-1";
const INSPECTOR_UID = "inspector-1";
const OTHER_UID = "other-1";
const JPEG = "fake-jpeg-bytes";

let testEnv: RulesTestEnvironment;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync(path.resolve(process.cwd(), "../firestore.rules"), "utf8"),
    },
    storage: {
      rules: fs.readFileSync(path.resolve(process.cwd(), "../storage.rules"), "utf8"),
    },
  });
});

afterAll(async () => {
  await testEnv.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
  await testEnv.clearStorage();
  await seedTask();
});

async function seedTask(): Promise<void> {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().doc(`tasks/${TASK_ID}`).set({
      clientUid: CLIENT_UID,
      matchedInspectorId: INSPECTOR_UID,
      status: "EVIDENCE_SUBMITTED",
    });
  });
}

async function seedEvidenceObject(fileName = "photo-seed.jpg"): Promise<void> {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .storage()
      .ref(`evidence/${TASK_ID}/${fileName}`)
      .putString(JPEG, "raw", { contentType: "image/jpeg" });
  });
}

describe("firestore.rules evidence documents", () => {
  it("allows a matched inspector to create bounded intake-only evidence", async () => {
    const db = testEnv.authenticatedContext(INSPECTOR_UID).firestore();
    await assertSucceeds(db.collection("evidence").doc("evidence-valid").set({
      taskId: TASK_ID,
      inspectorUid: INSPECTOR_UID,
      photos: [{ id: "photo-1", storagePath: `evidence/${TASK_ID}/photo-1.jpg` }],
      notes: "",
      submittedAtMs: serverTimestamp(),
      status: "PENDING",
    }));
  });

  it("refuses client-authored assessment fields and evidence for another task", async () => {
    const db = testEnv.authenticatedContext(INSPECTOR_UID).firestore();
    const common = {
      taskId: TASK_ID,
      inspectorUid: INSPECTOR_UID,
      photos: [{ id: "photo-1", storagePath: `evidence/${TASK_ID}/photo-1.jpg` }],
      notes: "",
      status: "PENDING",
    };
    await assertFails(db.collection("evidence").doc("forged-score").set({
      ...common,
      submittedAtMs: serverTimestamp(),
      qualityScore: 100,
    }));
    await assertFails(db.collection("evidence").doc("forged-attestation").set({
      ...common,
      submittedAtMs: serverTimestamp(),
      attestationVerdict: "PASSED",
    }));
    await assertFails(db.collection("evidence").doc("wrong-task").set({
      ...common,
      taskId: "other-task",
      submittedAtMs: serverTimestamp(),
    }));
  });
});

describe("storage.rules evidence media", () => {
  it("allows only the matched inspector to upload task evidence JPEGs", async () => {
    const inspectorStorage = testEnv.authenticatedContext(INSPECTOR_UID).storage();
    const clientStorage = testEnv.authenticatedContext(CLIENT_UID).storage();
    const anonymousStorage = testEnv.unauthenticatedContext().storage();

    await assertSucceeds(
      Promise.resolve(inspectorStorage
        .ref(`evidence/${TASK_ID}/photo-1.jpg`)
        .putString(JPEG, "raw", { contentType: "image/jpeg" })),
    );
    await assertFails(
      Promise.resolve(clientStorage
        .ref(`evidence/${TASK_ID}/photo-2.jpg`)
        .putString(JPEG, "raw", { contentType: "image/jpeg" })),
    );
    await assertFails(
      Promise.resolve(anonymousStorage
        .ref(`evidence/${TASK_ID}/photo-3.jpg`)
        .putString(JPEG, "raw", { contentType: "image/jpeg" })),
    );
  });

  it("rejects non-JPEG evidence uploads and unknown evidence paths", async () => {
    const inspectorStorage = testEnv.authenticatedContext(INSPECTOR_UID).storage();

    await assertFails(
      Promise.resolve(inspectorStorage
        .ref(`evidence/${TASK_ID}/photo-1.png`)
        .putString(JPEG, "raw", { contentType: "image/png" })),
    );
    await assertFails(
      Promise.resolve(inspectorStorage
        .ref("evidence-unscoped/photo-1.jpg")
        .putString(JPEG, "raw", { contentType: "image/jpeg" })),
    );
  });

  it("allows only the task client and matched inspector to read evidence", async () => {
    await seedEvidenceObject();

    await assertSucceeds(
      testEnv
        .authenticatedContext(CLIENT_UID)
        .storage()
        .ref(`evidence/${TASK_ID}/photo-seed.jpg`)
        .getMetadata(),
    );
    await assertSucceeds(
      testEnv
        .authenticatedContext(INSPECTOR_UID)
        .storage()
        .ref(`evidence/${TASK_ID}/photo-seed.jpg`)
        .getMetadata(),
    );
    await assertFails(
      testEnv
        .authenticatedContext(OTHER_UID)
        .storage()
        .ref(`evidence/${TASK_ID}/photo-seed.jpg`)
        .getMetadata(),
    );
  });

  it("denies evidence updates and deletes after upload", async () => {
    await seedEvidenceObject();
    const inspectorRef = testEnv
      .authenticatedContext(INSPECTOR_UID)
      .storage()
      .ref(`evidence/${TASK_ID}/photo-seed.jpg`);

    await assertFails(Promise.resolve(inspectorRef.putString(JPEG, "raw", { contentType: "image/jpeg" })));
    await assertFails(inspectorRef.delete());
  });
});

describe("storage.rules KYC media", () => {
  it("allows inspectors to upload only their own ID and selfie JPEGs", async () => {
    const inspectorStorage = testEnv.authenticatedContext(INSPECTOR_UID).storage();
    const otherStorage = testEnv.authenticatedContext(OTHER_UID).storage();

    await assertSucceeds(
      Promise.resolve(inspectorStorage
        .ref(`id_docs/${INSPECTOR_UID}/govt_id.jpg`)
        .putString(JPEG, "raw", { contentType: "image/jpeg" })),
    );
    await assertSucceeds(
      Promise.resolve(inspectorStorage
        .ref(`id_docs/${INSPECTOR_UID}/selfie.jpg`)
        .putString(JPEG, "raw", { contentType: "image/jpeg" })),
    );
    await assertFails(
      Promise.resolve(otherStorage
        .ref(`id_docs/${INSPECTOR_UID}/govt_id.jpg`)
        .putString(JPEG, "raw", { contentType: "image/jpeg" })),
    );
  });

  it("rejects unexpected KYC filenames and content types", async () => {
    const inspectorStorage = testEnv.authenticatedContext(INSPECTOR_UID).storage();

    await assertFails(
      Promise.resolve(inspectorStorage
        .ref(`id_docs/${INSPECTOR_UID}/passport.jpg`)
        .putString(JPEG, "raw", { contentType: "image/jpeg" })),
    );
    await assertFails(
      Promise.resolve(inspectorStorage
        .ref(`id_docs/${INSPECTOR_UID}/selfie.jpg`)
        .putString("not-an-image", "raw", { contentType: "text/plain" })),
    );
  });

  it("allows only the owner to read KYC media and blocks deletes", async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context
        .storage()
        .ref(`id_docs/${INSPECTOR_UID}/selfie.jpg`)
        .putString(JPEG, "raw", { contentType: "image/jpeg" });
    });

    const ownerRef = testEnv
      .authenticatedContext(INSPECTOR_UID)
      .storage()
      .ref(`id_docs/${INSPECTOR_UID}/selfie.jpg`);
    const otherRef = testEnv
      .authenticatedContext(OTHER_UID)
      .storage()
      .ref(`id_docs/${INSPECTOR_UID}/selfie.jpg`);

    await assertSucceeds(ownerRef.getMetadata());
    await assertFails(otherRef.getMetadata());
    await assertFails(ownerRef.delete());
  });
});
