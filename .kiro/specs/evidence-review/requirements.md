# Evidence Review — Requirements

**Spec ref:** EOS-D1 F6 | EOS-02 evidence model

## Functional Requirements

### FR-1: Evidence Package Display
- Photo gallery: grid of all captured photos, tap to expand
- Each photo shows GPS coordinates (JetBrains Mono) + capture timestamp
- Map view: task pin (red) + inspector GPS track (green dots) + distance label
- Checklist responses: all items with inspector answers
- Inspector notes
- Quality score badge (0.0–1.0, color-coded: green ≥ 0.8, amber ≥ 0.6, red < 0.6)

### FR-2: Accept
- Client taps "Accept" → task status → `ACCEPTED`
- Triggers escrow release (demo: status change; V1: real payout)
- Both parties prompted to rate each other

### FR-3: Request Revision
- Client taps "Request Revision" + writes specific feedback
- Task status → `REVISION_REQUESTED`
- Inspector notified with feedback text

### FR-4: Dispute
- Client taps "Dispute" + selects category + writes notes
- Task status → `DISPUTED`, escrow frozen
- Platform mediation flow (V1)

### FR-5: 48-Hour Auto-Accept
- If client takes no action within 48 hours, evidence auto-accepts
- Warning notification at 24 hours and 4 hours remaining
- Countdown timer visible on review screen

## Non-Functional
- Photos load progressively (thumbnail first, full-res on tap)
- Map renders even if offline (cached tiles)
