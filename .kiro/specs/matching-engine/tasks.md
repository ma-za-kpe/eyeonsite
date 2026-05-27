# Matching Engine - Tasks

## Status Legend

- [ ] Not started
- [x] Complete

## Demo MVP Scope

- [x] `onTaskCreated` Cloud Function for `tasks/{taskId}` creation.
- [x] Query available inspectors from Firestore.
- [x] Rank inspectors by Haversine distance.
- [x] Limit simultaneous pings to the three nearest inspectors.
- [x] Send FCM task ping payloads.
- [x] Persist `pinnedInspectorIds` and `expiresAtMs`.
- [x] Mark tasks `EXPIRED` when no inspectors are available.
- [x] `acceptTask` callable function with authenticated inspector guard.
- [x] Enforce that accepting inspector is in the pinned match list.
- [x] Mark accepting inspector unavailable in the same transaction.
- [x] Notify other pinned inspectors when a task is taken.
- [x] Scheduled expiry sweep for stale posted tasks.
- [x] Unit tests for Haversine distance.
- [x] Unit tests for candidate ranking and ping payloads.
- [x] Unit tests for accept-task transaction guard.

## Remaining

- [x] Firebase Emulator integration test for concurrent `acceptTask` calls.
- [x] Firestore security rules for task/inspector matching documents.
- [x] FCM token cleanup for invalid or unregistered tokens.
- [x] Geo-query optimization with geohash bounds for V1.
- [x] Inspector tier/trust scoring in ranking for V1.
- [x] Backoff or retry strategy for failed FCM sends.
- [x] Client notification when a task expires after the scheduled sweep.
