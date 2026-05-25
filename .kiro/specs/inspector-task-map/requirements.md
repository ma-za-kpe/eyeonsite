# Inspector Task Map — Requirements

**Spec ref:** EOS-D1 F7/F8 | EOS-02 inspector flows

## Functional Requirements

### FR-1: Map View of Available Tasks
- Google Maps centered on inspector's current GPS location
- Available tasks shown as annotated pins: budget + distance
- Bottom sheet: nearest task preview card (title, distance, budget, deadline)
- Real-time updates via Firestore listener on `tasks` where `status == POSTED`

### FR-2: Task Detail
- Full task detail: instructions, checklist preview, budget, distance, client rating
- "Accept" button with auto-calculated ETA (distance / avg speed)
- ETA committed to Firestore on accept

### FR-3: Accept Task
- `tasks/{id}.status` → `INSPECTOR_MATCHED`
- `tasks/{id}.matchedInspectorId` → inspector UID
- Inspector's `activeTaskId` set
- Client receives FCM push: "Inspector matched"

### FR-4: Earnings Card
- Home screen card: "Pending: GHS X" → "Earned: GHS X" after acceptance
- Tap → earnings history list

### FR-5: Inspector Availability
- Toggle: available / unavailable
- Unavailable inspectors excluded from matching geo-query

## Non-Functional
- Map must show tasks within configurable radius (default 10km)
- Task pins update in real-time without full page refresh
