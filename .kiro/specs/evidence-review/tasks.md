# Evidence Review — Tasks

- [ ] `EvidenceReviewState` + `EvidenceReviewViewModel`
- [ ] Photo grid with tap-to-expand + GPS coordinate overlay (JetBrains Mono)
- [ ] Map card: task pin + inspector GPS track + distance label
- [ ] Checklist responses list
- [ ] Quality score badge (green ≥ 0.8, amber ≥ 0.6, red < 0.6)
- [ ] Auto-accept countdown timer (ticks every second via Flow)
- [ ] Accept → Firestore `tasks/{id}.status = ACCEPTED`
- [ ] Request Revision → feedback dialog → `REVISION_REQUESTED`
- [ ] Dispute → category picker + notes → `DISPUTED`
- [ ] Unit tests: timer logic, action state transitions
- [ ] `@Preview` for EvidenceReviewScreen
- [ ] Analytics: `evidence_review_opened`, `evidence_accepted`, `evidence_disputed`
