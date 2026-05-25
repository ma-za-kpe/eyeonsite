# Client Dashboard — Requirements

**Spec ref:** EOS-D1 F4/F5 | EOS-02 client flows

## Functional Requirements

### FR-1: Task List
- All tasks for current client, ordered by `updatedAt` desc
- Status chips: POSTED, MATCHED, IN_PROGRESS, EVIDENCE_READY, COMPLETED, DISPUTED
- Real-time updates via Firestore listener

### FR-2: Task Detail
- Live status card with inspector profile (name, photo, trust score, ETA) after matching
- Map showing task pin + inspector last known location
- Instructions recap
- "Review Evidence" button appears when `status == EVIDENCE_SUBMITTED`

### FR-3: New Task FAB
- Floating action button → CreateTaskScreen

### FR-4: Notifications
- FCM push for: inspector matched, evidence ready, auto-accept warning (24h + 4h)
- In-app notification center (badge count on home)

### FR-5: Empty State
- First-time client: "Post your first verification task" with illustration + CTA

## Non-Functional
- Task list loads from SQLDelight cache first, then syncs from Firestore
- Status chips animate on status change
