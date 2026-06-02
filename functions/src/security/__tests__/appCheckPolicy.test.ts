import { readFileSync } from "fs";
import { join } from "path";

const root = join(__dirname, "../..");

const protectedCallables = [
  { exportName: "acceptTask", file: "matching/acceptTask.ts" },
  { exportName: "fundTask", file: "escrow/escrow.ts" },
  { exportName: "releaseEscrow", file: "escrow/escrow.ts" },
  { exportName: "createPaymentIntent", file: "payments/stripePayment.ts" },
  { exportName: "seedFirestore", file: "seed/seedFirestore.ts" },
];

function callableOptionsFor(file: string, exportName: string): string {
  const source = readFileSync(join(root, file), "utf8");
  const start = source.indexOf(`export const ${exportName} = onCall(`);
  expect(start).toBeGreaterThanOrEqual(0);
  const handlerStart = source.indexOf("async", start);
  expect(handlerStart).toBeGreaterThan(start);
  return source.slice(start, handlerStart);
}

describe("App Check policy", () => {
  test.each(protectedCallables)("$exportName enforces App Check", ({ file, exportName }) => {
    expect(callableOptionsFor(file, exportName)).toContain("enforceAppCheck: true");
  });
});
