# Evidence Review — Tasks

- [x] `EvidenceReviewState` + `EvidenceReviewViewModel`
- [x] Photo grid with tap-to-expand + GPS coordinate overlay (JetBrains Mono)
- [x] Map card: task pin + inspector GPS track + distance label
- [x] Checklist responses list
- [x] Quality score badge (green ≥ 0.8, amber ≥ 0.6, red < 0.6)
- [x] Auto-accept countdown timer (ticks every second via Flow)
- [x] Accept → Firestore `tasks/{id}.status = ACCEPTED`
- [x] Request Revision → feedback dialog → `REVISION_REQUESTED`
- [x] Dispute → category picker + notes → `DISPUTED`
- [x] Unit tests: timer logic, action state transitions
- [x] `@Preview` for EvidenceReviewScreen
- [x] Analytics: `evidence_review_opened`, `evidence_accepted`, `evidence_disputed`
