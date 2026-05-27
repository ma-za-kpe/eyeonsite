# Evidence Capture — Tasks

- [x] `InAppCamera` expect/actual (CameraX Android + AVFoundation iOS)
- [x] GPS provider expect/actual (FusedLocation Android + CoreLocation iOS)
- [x] `CaptureState` + `CaptureViewModel`
- [x] `CaptureScreen` — viewfinder, GPS indicator, progress bar, angle guide, thumbnail strip
- [x] `ChecklistScreen` — yes/no + text item types (number + multi-select deferred to V1)
- [x] `EvidenceSummaryScreen` — photo grid review + notes + submit
- [ ] SQLDelight `pending_upload` table + sync worker
- [x] Cloud Storage upload with progress reporting
- [x] Firestore evidence doc write
- [x] `computeQualityScore()` in commonMain
- [x] Unit tests: CaptureViewModel, ChecklistViewModel
- [x] `@Preview` for CaptureScreen, ChecklistScreen
- [x] Analytics: `capture_started`, `photo_captured`, `evidence_submitted`, `checklist_opened`, `checklist_completed`
