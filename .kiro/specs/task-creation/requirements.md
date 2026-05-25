# Task Creation — Requirements

**Spec ref:** EOS-D1 F3 | EOS-02 task lifecycle

## Functional Requirements

### FR-1: Map Pin Location
- Full-screen Google Maps with draggable crosshair
- Geocodes pin to human-readable address
- Stores GeoPoint (lat/lng) + address string

### FR-2: Vertical Template Selection
- Demo MVP: Construction only (hardcoded)
- V1: dropdown from Firestore `templates` collection

### FR-3: Instructions
- Free-text field, max 500 chars, required

### FR-4: Budget Selection
- Demo MVP: preset chips — GHS 30 / GHS 60 / GHS 120
- V1: custom amount input with minimum GHS 30

### FR-5: Post Task
- Writes to Firestore `tasks/{taskId}` with status `POSTED`
- Triggers `onTaskCreated` Cloud Function (matching)
- Shows "Finding inspector..." loading state

### FR-6: Escrow (V1, not demo)
- Demo: status change only, no real payment
- V1: Stripe (diaspora) or Flutterwave (local) payment before status → `ESCROW_FUNDED`

## Non-Functional
- Map must work offline (cached tiles)
- Task write must be idempotent (ULID as taskId)
