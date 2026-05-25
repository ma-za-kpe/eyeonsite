# Matching Engine — Design

## Cloud Function (Node.js)
```
functions/
└── matching/
    └── onTaskCreated.js
```

## Algorithm (Demo MVP — simple)
```javascript
// onTaskCreated trigger
exports.onTaskCreated = onDocumentCreated("tasks/{taskId}", async (event) => {
  const task = event.data.data();
  if (task.status !== "POSTED") return;

  // 1. Query available inspectors within radius
  const inspectors = await queryInspectorsNearby(task.location, task.locationRadius);
  if (inspectors.length === 0) {
    await event.data.ref.update({ status: "EXPIRED" });
    return;
  }

  // 2. Sort by distance, take top 3
  const candidates = inspectors
    .map(i => ({ ...i, distance: haversine(task.location, i.location) }))
    .sort((a, b) => a.distance - b.distance)
    .slice(0, 3);

  // 3. Send FCM pings
  await sendTaskPings(candidates, task);

  // 4. Set 30-min expiry timer
  await scheduleExpiry(task.id, 30 * 60 * 1000);
});
```

## Acceptance (atomic transaction)
```javascript
exports.acceptTask = onCall(async (request) => {
  const { taskId } = request.data;
  await firestore.runTransaction(async (tx) => {
    const taskRef = firestore.doc(`tasks/${taskId}`);
    const task = (await tx.get(taskRef)).data();
    if (task.status !== "POSTED") throw new Error("Task no longer available");
    tx.update(taskRef, { status: "INSPECTOR_MATCHED", matchedInspectorId: request.auth.uid });
  });
});
```

## Tasks
- [ ] `onTaskCreated` Cloud Function
- [ ] `haversine` distance utility
- [ ] FCM ping helper (sends to inspector FCM tokens)
- [ ] `acceptTask` callable function with atomic transaction
- [ ] `scheduleExpiry` using Cloud Tasks or scheduled function
- [ ] Integration test with Firebase Emulator
