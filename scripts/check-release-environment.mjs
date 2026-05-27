#!/usr/bin/env node
import { spawnSync } from "node:child_process";
import { existsSync, readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";

const expectedProjectId = process.env.EYEONSITE_FIREBASE_PROJECT_ID || "eyeonsite-3b8e0";
const strictRuntime = process.argv.includes("--strict-runtime");
const failures = [];
const warnings = [];

function readJson(path) {
  return JSON.parse(readFileSync(path, "utf8"));
}

function readText(path) {
  return readFileSync(path, "utf8");
}

function assert(condition, message) {
  if (!condition) failures.push(message);
}

function warn(condition, message) {
  if (!condition) warnings.push(message);
}

function walkTsFiles(dir) {
  const files = [];
  for (const entry of readdirSync(dir)) {
    const fullPath = join(dir, entry);
    const stat = statSync(fullPath);
    if (stat.isDirectory()) {
      files.push(...walkTsFiles(fullPath));
    } else if (entry.endsWith(".ts")) {
      files.push(fullPath);
    }
  }
  return files;
}

function detectedJavaMajor() {
  const result = spawnSync("java", ["-version"], { encoding: "utf8" });
  return parseJavaMajor(`${result.stdout || ""}\n${result.stderr || ""}`);
}

function parseJavaMajor(output) {
  const match = output.match(/version "([^"]+)"/);
  if (!match) return null;
  const version = match[1];
  if (version.startsWith("1.")) {
    return Number(version.split(".")[1]);
  }
  return Number(version.split(".")[0]);
}

const firebaseRc = readJson(".firebaserc");
const firebaseJson = readJson("firebase.json");
const functionsPackage = readJson("functions/package.json");
const firestoreRules = readText("firestore.rules");
const firestoreIndexes = readJson("firestore.indexes.json");
const storageRules = readText("storage.rules");
const ciWorkflow = readText(".github/workflows/ci.yml");

assert(firebaseRc.projects?.default === expectedProjectId, `.firebaserc default project must be ${expectedProjectId}`);
assert(firebaseJson.firestore?.rules === "firestore.rules", "firebase.json must register firestore.rules");
assert(firebaseJson.firestore?.indexes === "firestore.indexes.json", "firebase.json must register firestore.indexes.json");
assert(firebaseJson.storage?.rules === "storage.rules", "firebase.json must register storage.rules");
assert(firebaseJson.functions?.source === "functions", "firebase.json must point Functions source at functions/");
assert(firebaseJson.hosting?.public === "public", "firebase.json must deploy hosting from public/");
assert(firebaseJson.emulators?.firestore?.port === 8085, "firebase.json must pin Firestore emulator port 8085");
assert(firebaseJson.emulators?.storage?.port === 9199, "firebase.json must pin Storage emulator port 9199");

assert(firestoreRules.includes("rules_version = '2';"), "firestore.rules must use rules_version 2");
assert(storageRules.includes("rules_version = '2';"), "storage.rules must use rules_version 2");
assert(storageRules.includes("match /{allPaths=**}"), "storage.rules must include a deny-all fallback");
assert(storageRules.includes("allow read, write: if false;"), "storage.rules fallback must deny reads and writes");
assert(Array.isArray(firestoreIndexes.indexes), "firestore.indexes.json must declare indexes array");
for (const collectionGroup of ["inspectors", "tasks", "evidence", "disputes", "pending_payouts", "escrow_transactions", "waitlist"]) {
  assert(
    firestoreIndexes.indexes.some((index) => index.collectionGroup === collectionGroup),
    `firestore.indexes.json must include collection group ${collectionGroup}`,
  );
}

assert(functionsPackage.scripts?.build === "tsc", "functions/package.json must expose npm run build");
assert(functionsPackage.scripts?.test === "jest", "functions/package.json must expose npm test");
assert(Boolean(functionsPackage.scripts?.["test:rules"]), "functions/package.json must expose npm run test:rules");

const functionsSource = walkTsFiles("functions/src").map(readText).join("\n");
for (const secret of ["STRIPE_SECRET_KEY", "STRIPE_WEBHOOK_SECRET", "FLW_SECRET_KEY", "GMAIL_APP_PASSWORD"]) {
  assert(functionsSource.includes(secret), `Functions source must reference required Secret Manager secret ${secret}`);
}

assert(ciWorkflow.includes('java-version: "21"'), "CI must use JDK 21 for Firebase emulator compatibility");
assert(ciWorkflow.includes("npm test"), "CI must run Functions Jest tests");
assert(ciWorkflow.includes("npm run build"), "CI must run Functions TypeScript build");
assert(ciWorkflow.includes("npm run test:rules"), "CI must run Firebase rules emulator tests");

const javaMajor = detectedJavaMajor();
const runtimeOk = javaMajor !== null && javaMajor >= 21;
const runtimeMessage = `Firebase emulator rules tests require JDK 21+; detected ${javaMajor ?? "unknown"}`;
if (strictRuntime) {
  assert(runtimeOk, runtimeMessage);
} else {
  warn(runtimeOk, runtimeMessage);
}

for (const file of [".firebaserc", "firebase.json", "firestore.rules", "firestore.indexes.json", "storage.rules", "functions/package.json"]) {
  assert(existsSync(file), `Required release file missing: ${file}`);
}

if (warnings.length > 0) {
  console.warn("Release environment warnings:");
  for (const message of warnings) console.warn(`- ${message}`);
}

if (failures.length > 0) {
  console.error("Release environment check failed:");
  for (const message of failures) console.error(`- ${message}`);
  process.exit(1);
}

console.log(`Release environment check passed for Firebase project ${expectedProjectId}.`);
