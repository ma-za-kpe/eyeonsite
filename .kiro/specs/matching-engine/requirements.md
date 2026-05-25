# Matching Engine — Requirements

**Spec ref:** EOS-D1 F13 | EOS-A5 matching algorithm | EOS-03 event model

## Functional Requirements

### FR-1: Trigger
- Cloud Function `onTaskCreated` fires when `tasks/{id}.status` → `POSTED`

### FR-2: Geo-Query
- Find inspectors where: `available == true` AND within `task.locationRadius` (default 10km)
- Demo MVP: simple nearest-inspector (sort by distance)
- V1: scored matching (trust score × distance weight)

### FR-3: Ping
- Send FCM push to matched inspector(s): "New task near you — GHS X, Y km away"
- Ping up to 3 nearest inspectors simultaneously
- First to accept wins

### FR-4: Timeout
- If no inspector accepts within 30 minutes → status → `EXPIRED`
- Client notified: "No inspector available, try again"

### FR-5: Acceptance Lock
- When inspector accepts: atomic Firestore transaction to prevent double-acceptance
- Other pinged inspectors receive "Task taken" notification

## Non-Functional
- Function must complete within 10s (FCM delivery SLA)
- Geo-query uses Firestore GeoPoint + geohash for efficiency (V1)
