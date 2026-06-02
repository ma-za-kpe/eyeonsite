import * as fs from "fs";
import * as path from "path";

const repoRoot = path.resolve(process.cwd(), "..");

function readRepoFile(relativePath: string): string {
  return fs.readFileSync(path.join(repoRoot, relativePath), "utf8");
}

describe("Storage rules policy guard", () => {
  const rules = readRepoFile("storage.rules");

  it("registers storage.rules in firebase.json", () => {
    const firebaseConfig = JSON.parse(readRepoFile("firebase.json")) as {
      storage?: { rules?: string };
    };

    expect(firebaseConfig.storage?.rules).toBe("storage.rules");
  });

  it("locks evidence media to task participants and JPEG creates only", () => {
    expect(rules).toContain("match /evidence/{taskId}/{fileName}");
    expect(rules).toContain("taskDoc(taskId).data.clientUid == request.auth.uid");
    expect(rules).toContain("taskDoc(taskId).data.matchedInspectorId == request.auth.uid");
    expect(rules).toContain("allow write: if resource == null");
    expect(rules).toContain("request.resource.contentType == 'image/jpeg'");
    expect(rules).toContain("request.resource.size <= maxImageBytes()");
  });

  it("locks KYC media to owner-only access and approved filenames", () => {
    expect(rules).toContain("match /id_docs/{uid}/{fileName}");
    expect(rules).toContain("allow read: if signedIn() && request.auth.uid == uid;");
    expect(rules).toContain("allow create, update: if signedIn()");
    expect(rules).toContain("request.auth.uid == uid");
    expect(rules).toContain("fileName in ['govt_id.jpg', 'selfie.jpg']");
    expect(rules).toContain("allow delete: if false;");
  });

  it("denies all unrecognised storage paths", () => {
    expect(rules).toContain("match /{allPaths=**}");
    expect(rules).toContain("allow read, write: if false;");
  });

  it("keeps Android uploaders compatible with strict content-type rules", () => {
    const evidenceUploader = readRepoFile(
      "app/shared/src/androidMain/kotlin/com/maku/eyeonsite/ui/evidence/FirebaseStorageEvidenceUploader.android.kt",
    );
    const onboardingUploader = readRepoFile(
      "app/shared/src/androidMain/kotlin/com/maku/eyeonsite/onboarding/FirestoreInspectorOnboardingRepository.android.kt",
    );

    expect(evidenceUploader).toContain(".setContentType(\"image/jpeg\")");
    expect(onboardingUploader).toContain(".setContentType(\"image/jpeg\")");
  });
});
