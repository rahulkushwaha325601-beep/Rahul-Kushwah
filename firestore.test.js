const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write students", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("students").doc("RSL-001").get());
  await assertFails(
    unauthDb.collection("students").doc("RSL-001").set({
      id: "RSL-001",
      username: "student001",
      passwordHash: "hash123",
      fullName: "Test Student",
      mobileNumber: "9876543210",
      seatNumber: "Seat A-1",
      course: "UPSC",
      status: "Active",
    })
  );
});

test("Authenticated user can read and create valid student with Aadhaar docs", async () => {
  const authDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    authDb.collection("students").doc("RSL-001").set({
      id: "RSL-001",
      username: "student001",
      passwordHash: "hash123",
      fullName: "Test Student",
      mobileNumber: "9876543210",
      seatNumber: "Seat A-1",
      course: "UPSC",
      status: "Active",
      dob: "2000-01-15",
      profileCompleted: true,
      aadhaarFrontBase64: "base64frontdata",
      aadhaarBackBase64: "base64backdata",
    })
  );
  await assertSucceeds(authDb.collection("students").doc("RSL-001").get());
});

test("Authenticated user can submit payment with screenshot", async () => {
  const authDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    authDb.collection("payments").doc("PAY-1001").set({
      id: "PAY-1001",
      studentId: "RSL-001",
      studentUsername: "student001",
      studentName: "Test Student",
      amount: 600,
      transactionRef: "UPI-998877",
      screenshotBase64: "base64screenshot",
      date: "2026-10-06",
      time: "11:30 AM",
      status: "Pending",
    })
  );
});

test("Unauthenticated user cannot submit payment", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("payments").doc("PAY-1002").set({
      id: "PAY-1002",
      studentId: "RSL-001",
      studentName: "Test Student",
      amount: 600,
      date: "2026-10-06",
      time: "11:30 AM",
      status: "Pending",
    })
  );
});
