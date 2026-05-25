# Evidence Capture — Requirements

**Spec ref:** EOS-D1 F9/F10/F11 | EOS-02 evidence model | DEC-010

## Functional Requirements

### FR-1: In-App Camera Only
- ALL photos captured through EyeOnSite camera module (CameraX on Android, AVFoundation on iOS)
- Gallery/camera roll uploads are PERMANENTLY PROHIBITED — this is non-negotiable
- Photos captured outside the app are rejected server-side

### FR-2: GPS Overlay
- GPS coordinates captured at every photo (multi-signal: GPS + network + WiFi)
- GPS status indicator: green (< 20m accuracy), yellow (20–50m), red (> 50m)
- Coordinates displayed in JetBrains Mono on capture screen
- Inspector cannot submit if GPS is red

### FR-3: Photo Progress
- Progress bar: "X of N photos" where N = template requirement (default 8)
- Photo angle guide: "Take FRONT view", "Take LEFT SIDE", etc.
- Thumbnail strip of captured photos

### FR-4: Checklist Completion
- After all photos: step through checklist items
- Item types: yes/no toggle, number input, text input, multi-select
- Required items must be answered before submit

### FR-5: Evidence Submission
- Upload photos to Cloud Storage `evidence/{taskId}/photos/`
- Write evidence doc to Firestore `evidence/{evidenceId}`
- Show upload progress (critical for low-connectivity)
- Offline queue: capture locally → upload when online (SQLDelight pending_sync)

### FR-6: Inspector Notes
- Optional free-text field, max 1000 chars

## Non-Functional
- Must work offline: capture to local storage, queue for upload
- Photo metadata (GPS, timestamp, device) embedded and preserved
- Perceptual hash computed per photo (duplicate detection, V1)
