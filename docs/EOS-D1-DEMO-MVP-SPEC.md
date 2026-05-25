# D1 — Demo MVP Feature Specification

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## What This Is (and Isn't)

This is **NOT** the full V1 product. This is a **demo MVP** — a functional prototype you can put in a stakeholder's hands and walk them through the complete EyeOnSite story in 5 minutes.

**Audience for the demo:**
- Potential co-founder → "This is real. It works. I want in."
- Angel investors → "There's a product here, not just a pitch deck."
- First 10 clients (diaspora) → "I can use this right now."
- First 10 inspectors → "I understand what I'd be doing."
- Enterprise prospects → "I see how this could scale to our use case."

**The demo must answer ONE question:** "Can I post a task and get GPS-verified evidence back?"

Everything else is secondary. If a feature doesn't help answer that question, it doesn't go in the demo.

---

## The Golden Path

The demo must nail one flawless end-to-end flow:

```
CLIENT                          INSPECTOR
  │                                │
  ├─ 1. Sign in (phone OTP)       │
  ├─ 2. Create task               │
  │    (pin location, pick         │
  │     template, set budget)      │
  ├─ 3. See "Finding inspector..." │
  │                                ├─ 4. Sign in (phone OTP)
  │                                ├─ 5. See task on map
  │                                ├─ 6. Accept task
  ├─ 7. See "Inspector matched"   │
  │    (name, photo, ETA)          │
  │                                ├─ 8. Navigate to site
  │                                ├─ 9. Open capture mode
  │                                ├─ 10. Take GPS-stamped photos
  │                                ├─ 11. Complete checklist
  │                                ├─ 12. Submit evidence
  ├─ 13. Review evidence           │
  │     (photos, GPS overlay,      │
  │      checklist, quality score)  │
  ├─ 14. Accept evidence           │
  │                                ├─ 15. See "Payment released"
  └─ DONE                         └─ DONE
```

That's it. That's the demo. 15 steps. One story. If this works beautifully, stakeholders will fill in the rest with their imagination.

---

## Feature Cut: What's IN vs. What's OUT

### IN (Demo MVP) — Build These

| # | Feature | Screen | Why It's In |
|---|---------|--------|-------------|
| **F1** | Phone OTP sign-in | Auth | Real authentication, no fake/demo login. First impression of "this is real." Firebase Auth, takes 1 day to implement. |
| **F2** | Role selection | Auth | "I'm a client" / "I'm an inspector" — one app, two experiences. Simple toggle. |
| **F3** | Client: Create task | Task creation | The core action. Map pin for location, construction template, free-text instructions, budget selector (GHS 30 / 60 / 120 presets). |
| **F4** | Client: Task dashboard | Home | List of your tasks with status chips (Posted, Matched, In Progress, Evidence Ready, Completed). Real-time Firestore updates. |
| **F5** | Client: Task detail | Task | Live status card, matched inspector profile (name, photo, trust score), evidence package when submitted. |
| **F6** | Client: Evidence review | Evidence | THE money screen. Photo gallery with GPS overlay on each photo. Checklist responses. Inspector notes. Quality score badge. Map showing inspector's GPS trail vs. task pin. Accept / Dispute buttons. |
| **F7** | Inspector: Task map | Home | Google Maps centered on inspector location. Nearby tasks as pins with budget + distance. Tap to see task detail. |
| **F8** | Inspector: Accept task | Task | Task detail with instructions, checklist preview, budget, distance. "Accept" button with auto-calculated ETA. |
| **F9** | Inspector: Capture mode | Camera | **This is the demo's showstopper.** Full-screen camera with: GPS status indicator (green/yellow/red), required photo counter ("3 of 8 captured"), current checklist item overlay, photo angle guide ("Take FRONT view"). All captured in-app. No gallery. |
| **F10** | Inspector: Checklist completion | Checklist | After photos, step through checklist: yes/no toggles, text inputs, number inputs. Must complete all required items. |
| **F11** | Inspector: Evidence submission | Submit | Review captured photos (thumbnail grid), checklist summary, add notes. Submit button. Show upload progress (important for low connectivity demo). |
| **F12** | Inspector: Earnings card | Home | Simple card showing "Pending: GHS X" → "Earned: GHS X" after acceptance. The payout story. |
| **F13** | Matching engine | Backend | Cloud Function: when task created, find nearest available inspector, send FCM push. Can be simple (nearest inspector) for demo. |
| **F14** | Evidence validation | Backend | Cloud Function: on evidence submission, verify GPS location matches task (within 200m), compute basic quality score, store in Firestore. |
| **F15** | Push notifications | System | FCM: "New task near you" (inspector), "Inspector matched" (client), "Evidence ready for review" (client), "Evidence accepted — payment released" (inspector). |
| **F16** | Construction template | Data | One pre-loaded template with 8-photo requirement (N/S/E/W + interior + materials + detail + overview) and 10-item checklist. Hardcoded for demo, Firestore-driven in V1. |

### OUT (Deferred to V1) — Do NOT Build These for Demo

| Feature | Why It's Out |
|---------|-------------|
| **Escrow / real payments** | Use fake/simulated payment for demo. "Payment released" is a status change, not a real Stripe charge. Real escrow adds weeks of Stripe + Flutterwave integration. Show the concept, implement the plumbing later. |
| **Inspector onboarding flow** | For demo, inspectors are pre-registered by you (Maku). No ID upload, no background check, no training module. Just phone OTP → inspector role → ready to go. |
| **Dispute flow** | The demo doesn't fail. Client accepts evidence. No dispute UI needed. |
| **Revision requests** | Same — demo shows the happy path. |
| **Rating system** | Nice to have, not needed for demo impact. |
| **Offline sync / SQLDelight** | Demo will be in Accra with connectivity. Offline-first is critical for V1 but not for a 5-minute demo. |
| **Cloud Vision AI quality** | Compute a fake quality score based on photo count + GPS match for demo. Real CV API integration is V1. |
| **Inspector tiers** | All inspectors are "Basic" in demo. Tier system is V1. |
| **Project / recurring tasks** | Demo shows single tasks only. |
| **Video capture** | Photos only for demo. Video is V2. |
| **Admin dashboard** | You (Maku) are the admin. Check Firestore directly. |
| **Landing page** | Separate effort. Not needed for the demo itself. Build it in parallel if you want, but it doesn't block the demo. |
| **iOS** | Android only for demo. CMP iOS is V1. |
| **Web dashboard** | Not needed for demo. |
| **Multi-vertical** | Construction only. One template. |
| **Multi-city** | Accra only. Hardcoded. |

---

## Screen-by-Screen Specification

### Screen 1: Splash / Auth

```
┌─────────────────────────┐
│                         │
│     [Pin+Eye Logo]      │
│     EyeOnSite           │
│     Trust, verified.    │
│                         │
│  ┌───────────────────┐  │
│  │ +233 XX XXX XXXX  │  │
│  └───────────────────┘  │
│                         │
│  [ Send OTP ]           │
│                         │
│  ─── or ───             │
│                         │
│  [ Continue with Google]│
│                         │
└─────────────────────────┘
```

- Firebase Auth phone OTP (primary) + Google Sign-In (fallback for demo convenience)
- After auth: role selection → "I'm here to verify my project" / "I'm here to inspect"
- First-time users only. Role persists in Firestore user doc.

### Screen 2: Client Home (Task Dashboard)

```
┌─────────────────────────┐
│ EyeOnSite    [Profile]  │
├─────────────────────────┤
│                         │
│  Your tasks             │
│                         │
│  ┌─────────────────────┐│
│  │ 🏗️ Tema House Check ││
│  │ Tema, Accra          ││
│  │ [Evidence Ready] ●   ││
│  │ GHS 60 · May 25      ││
│  └─────────────────────┘│
│                         │
│  ┌─────────────────────┐│
│  │ 🏗️ Plot Verification││
│  │ East Legon, Accra    ││
│  │ [In Progress] ●      ││
│  │ GHS 45 · May 24      ││
│  └─────────────────────┘│
│                         │
│                         │
│  [ + New Task ]  (FAB)  │
│                         │
└─────────────────────────┘
```

- Real-time Firestore listener on `tasks` where `clientId == currentUser.uid`
- Status chips: color-coded per TaskStatus enum
- Tap card → Task Detail (Screen 3)
- FAB → Create Task (Screen 4)

### Screen 3: Client Task Detail

```
┌─────────────────────────┐
│ ← Task Detail           │
├─────────────────────────┤
│                         │
│  Tema House Check       │
│  📍 Tema, Greater Accra │
│                         │
│ ┌──── Status ──────────┐│
│ │ ● Inspector matched   ││
│ │                       ││
│ │ [Inspector Photo]     ││
│ │ Kwame Asante          ││
│ │ ★ 4.7 · 23 tasks     ││
│ │ ETA: 12 min           ││
│ └───────────────────────┘│
│                         │
│ ┌──── Map ─────────────┐│
│ │                       ││
│ │   [Google Map with    ││
│ │    task pin + inspector││
│ │    location dot]      ││
│ │                       ││
│ └───────────────────────┘│
│                         │
│ Instructions:           │
│ "Check construction     │
│  progress. Focus on     │
│  the roof structure."   │
│                         │
│ Budget: GHS 60          │
│                         │
│  [Review Evidence]      │
│  (appears when status = │
│   EVIDENCE_SUBMITTED)   │
│                         │
└─────────────────────────┘
```

- Real-time status updates
- Inspector profile card appears after matching
- Map shows both task pin and inspector's last known location
- "Review Evidence" button appears when evidence is submitted

### Screen 4: Client Create Task

```
┌─────────────────────────┐
│ ← New Task              │
├─────────────────────────┤
│                         │
│  Where?                 │
│  ┌─────────────────────┐│
│  │ [Google Map]         ││
│  │  ╋ (crosshair)      ││
│  │  Drag to pin your   ││
│  │  site location       ││
│  └─────────────────────┘│
│  📍 Tema, Community 25  │
│                         │
│  What type?             │
│  [🏗️ Construction ✓]   │
│  (only option in demo)  │
│                         │
│  What to check?         │
│  ┌─────────────────────┐│
│  │ Check roof progress  ││
│  │ and materials on     ││
│  │ site.                ││
│  └─────────────────────┘│
│                         │
│  Budget                 │
│  [GHS 30] [GHS 60✓]    │
│  [GHS 120]              │
│                         │
│  [ Post Task — GHS 60 ] │
│                         │
└─────────────────────────┘
```

- Google Maps with draggable pin (geocode to address)
- Free-text instructions
- Budget presets (not custom amount in demo)
- "Post Task" writes to Firestore → triggers matching Cloud Function

### Screen 5: Client Evidence Review (THE MONEY SCREEN)

```
┌─────────────────────────┐
│ ← Evidence Review       │
├─────────────────────────┤
│                         │
│ Quality: [■■■■■■■■░░]  │
│          0.87 / 1.0     │
│                         │
│ ┌─── Photos ───────────┐│
│ │ [Photo grid — 8 imgs] ││
│ │  Each with GPS icon   ││
│ │  Tap to expand        ││
│ └───────────────────────┘│
│                         │
│ ┌─── GPS Verification ─┐│
│ │ [Map showing:         ││
│ │  - Task pin (red)     ││
│ │  - Inspector GPS      ││
│ │    track (green dots) ││
│ │  - Distance: 45m ✓]  ││
│ └───────────────────────┘│
│                         │
│ ┌─── Checklist ────────┐│
│ │ ✅ Workers on site: Yes││
│ │ ✅ Materials visible: Yes│
│ │ ✅ Stage: Roofing     ││
│ │ ✅ Est. complete: 65% ││
│ │ ... (scrollable)      ││
│ └───────────────────────┘│
│                         │
│ Inspector notes:        │
│ "Roof trusses installed.│
│  Iron sheets partially  │
│  laid. 3 workers on     │
│  site. Materials include│
│  20 bags cement, rebar."│
│                         │
│ ┌───────┐ ┌───────────┐│
│ │Dispute│ │  Accept ✓  ││
│ └───────┘ └───────────┘│
│                         │
└─────────────────────────┘
```

This is the screen you linger on during demos. It's the proof. Every element says "this is real":
- Photo grid with GPS icons (tap to see full photo with coordinate overlay)
- Map with task pin + inspector GPS track (proves they were there)
- Distance calculation (45m from pin ✓)
- Checklist responses (structured data, not just photos)
- Quality score (computed, not hand-wavy)
- Inspector notes (human observation)

### Screen 6: Inspector Home (Task Map)

```
┌─────────────────────────┐
│ EyeOnSite    [Earnings] │
├─────────────────────────┤
│                         │
│ ┌─────────────────────┐ │
│ │                     │ │
│ │  [Full-screen map]  │ │
│ │                     │ │
│ │  📍 GHS 60 · 2.1km │ │
│ │                     │ │
│ │  📍 GHS 45 · 4.5km │ │
│ │                     │ │
│ │       ● (you)       │ │
│ │                     │ │
│ └─────────────────────┘ │
│                         │
│  ┌─ Nearest task ──────┐│
│  │ 🏗️ Construction     ││
│  │ Tema · 2.1 km       ││
│  │ GHS 60 · Due today  ││
│  │ [ View Details ]    ││
│  └─────────────────────┘│
│                         │
└─────────────────────────┘
```

- Google Maps centered on inspector's current GPS
- Available tasks as annotated pins
- Bottom sheet shows nearest task preview

### Screen 7: Inspector Capture Mode (THE SHOWSTOPPER)

```
┌─────────────────────────┐
│ [━━━━━━━━░░] 5/8 photos │
├─────────────────────────┤
│                         │
│                         │
│    ┌─────────────────┐  │
│    │                 │  │
│    │  [CAMERA        │  │
│    │   VIEWFINDER]   │  │
│    │                 │  │
│    │                 │  │
│    └─────────────────┘  │
│                         │
│  ┌──────────────────┐   │
│  │ 📍 GPS: ✓ 12m    │   │
│  │ 5.6037°N 0.1870°W│   │
│  └──────────────────┘   │
│                         │
│  Next: Take FRONT view  │
│                         │
│  ┌────────────────────┐ │
│  │ [Thumbnails of     │ │
│  │  captured photos]  │ │
│  └────────────────────┘ │
│                         │
│      [ 📸 CAPTURE ]     │
│                         │
└─────────────────────────┘
```

This screen does all the magic:
- **CameraX** viewfinder (full-screen camera preview)
- **GPS indicator**: green checkmark + accuracy in meters + coordinates in JetBrains Mono
- **Progress bar**: "5 of 8 photos" with photo angle guide ("Take FRONT view", "Take LEFT SIDE", etc.)
- **Thumbnail strip**: previously captured photos
- **Capture button**: takes photo, embeds GPS + timestamp + device info in metadata, stores locally
- After all 8 photos: auto-transitions to checklist screen

### Screen 8: Inspector Checklist

```
┌─────────────────────────┐
│ ← Checklist  [Submit →] │
├─────────────────────────┤
│                         │
│  1. Workers on site?    │
│     [Yes ✓] [No]        │
│                         │
│  2. How many workers?   │
│     [  3  ]             │
│                         │
│  3. Materials visible?  │
│     [Yes ✓] [No]        │
│                         │
│  4. Materials observed: │
│     ┌──────────────────┐│
│     │ Cement, rebar,   ││
│     │ iron sheets      ││
│     └──────────────────┘│
│                         │
│  5. Construction stage? │
│     ○ Foundation        │
│     ○ Walls/Blockwork   │
│     ● Roofing ✓         │
│     ○ Finishing         │
│                         │
│  ... (scrollable)       │
│                         │
│  10. Additional notes:  │
│     ┌──────────────────┐│
│     │ Roof trusses     ││
│     │ installed. Iron   ││
│     │ sheets partially ││
│     │ laid...          ││
│     └──────────────────┘│
│                         │
│  [ Submit Evidence ]    │
│                         │
└─────────────────────────┘
```

- All 10 checklist items from the construction template
- Mix of yes/no, number, text, and multi-select inputs
- Required items marked
- Submit uploads photos to Cloud Storage + writes evidence doc to Firestore

---

## Technical Scope (Demo Build)

### What to Build

| Component | Technology | Effort |
|-----------|-----------|--------|
| **Android app** | KMP + Jetpack Compose | 3-4 weeks |
| **Shared module** | KMP commonMain (domain models, repository interfaces) | 1 week |
| **Firebase setup** | Auth (phone OTP), Firestore (5 collections), Cloud Storage (evidence bucket) | 1 day |
| **Cloud Functions** | 3 functions: `onTaskCreated` (match), `onEvidenceSubmitted` (validate), `sendNotification` | 3-4 days |
| **Firestore rules** | Basic security rules (from EOS-07) | 1 day |
| **Google Maps** | Maps Compose integration, geocoding | 2-3 days |
| **CameraX** | In-app capture with GPS overlay | 3-4 days |
| **FCM** | Push notifications (4 notification types) | 1-2 days |

### What to Skip

| Component | V1 Timing |
|-----------|----------|
| SQLDelight / offline sync | V1 Sprint 2 |
| Stripe / Flutterwave integration | V1 Sprint 3 |
| Cloud Vision API | V1 Sprint 4 |
| Fraud detection pipeline | V1 Sprint 4 |
| iOS (CMP) | V1 Sprint 5 |
| Web dashboard | V1 Sprint 4 |
| Inspector onboarding flow | V1 Sprint 2 |
| Rating system | V1 Sprint 3 |
| Dispute flow | V1 Sprint 3 |

### Demo Data Strategy

Pre-seed Firestore with:
- 2-3 sample completed tasks (with real photos from your V0 manual inspections)
- 1 active template (construction progress)
- 3-5 inspector profiles (you + friends/MEST contacts)
- Sample evidence packages with GPS data

This way the demo isn't empty on first launch.

---

## Firestore Collections (Demo Minimal)

```
users/
  {uid}/
    displayName, phone, role ("CLIENT"|"INSPECTOR"), createdAt

tasks/
  {taskId}/
    clientId, title, instructions, location (GeoPoint), address,
    budget (amount, currency), templateId,
    status ("POSTED"|"MATCHED"|"IN_PROGRESS"|"EVIDENCE_SUBMITTED"|"ACCEPTED"),
    matchedInspectorId, evidenceId,
    createdAt, updatedAt

inspectors/
  {uid}/
    displayName, phone, photoUrl, trustScore, completedTasks,
    location (GeoPoint), available (boolean)

evidence/
  {evidenceId}/
    taskId, inspectorId,
    photos: [{ url, gpsLat, gpsLng, gpsAccuracy, angle, capturedAt }],
    checklistResponses: [{ itemId, type, value }],
    inspectorNotes, qualityScore,
    captureLocation (GeoPoint), distanceFromTask,
    submittedAt

templates/
  {templateId}/
    name, verticalType, requiredPhotos, photoAngles[],
    checklist: [{ id, text, type, required }]
```

Five collections. That's it for the demo.

---

## Build Timeline

**Assuming 2-3 hours/day evenings + weekends (alongside Farmerline):**

| Week | Focus | Deliverable |
|------|-------|------------|
| **Week 1** | Foundation | KMP project scaffolded. Firebase configured. Auth working. Firestore rules. Domain models in shared module. |
| **Week 2** | Inspector capture flow | CameraX integration. GPS overlay. Photo capture with metadata. Checklist UI. Evidence submission to Firestore + Cloud Storage. |
| **Week 3** | Client flow | Task creation with map pin. Task dashboard. Evidence review screen with photo gallery + GPS map overlay. Accept button. |
| **Week 4** | Matching + polish | Cloud Function for matching (simple nearest-inspector). FCM notifications. Pre-seed demo data. Bug fixes. End-to-end test. |
| **Week 5** | Demo prep | Record demo video. Prepare talking points. Do 3 real demo runs with friends. Fix issues found. |

**Total: ~5 weeks to demo-ready.**

If using Claude Code for acceleration, potentially 3-4 weeks.

---

## Demo Script (5-Minute Walkthrough)

**Setup:** Two phones. One is "Ama in London" (client). One is "Kwame in Accra" (inspector). Run the demo live.

**[0:00]** "Ama is a nurse in London. She's building a house in Tema. Her contractor stopped sending updates 6 weeks ago. She opens EyeOnSite."

**[0:30]** *On client phone:* Create task. Pin the Tema location. "Check construction progress, focus on the roof." Budget: GHS 60. Post.

**[1:00]** *On inspector phone:* Kwame gets a push notification. Sees the task on his map — 2.1km away. Taps "Accept."

**[1:30]** *On client phone:* "Inspector matched — Kwame Asante, 4.7 stars, ETA 12 min."

**[2:00]** *On inspector phone:* Kwame arrives at the site. Opens capture mode. GPS indicator: green, 12m accuracy. Takes 8 photos — front, back, left, right, interior, materials, detail, overview. Each photo stamped with GPS.

**[3:00]** *On inspector phone:* Completes the checklist. "Workers on site: Yes. Stage: Roofing. Estimated complete: 65%." Adds notes. Submits.

**[3:30]** *On client phone:* Push notification: "Evidence ready for review."

**[3:45]** *On client phone:* Opens evidence review. 8 GPS-verified photos. Map shows inspector was 45m from the task pin. Quality score: 0.87. Checklist confirms roof is in progress. Inspector notes describe materials on site.

**[4:15]** *Pause here.* "This is what Ama sees from London. Not a WhatsApp photo from her brother. GPS-verified proof, captured through our app, with a structured assessment. She knows her house is real. She knows the roof is going up. She didn't have to fly home."

**[4:30]** *On client phone:* Tap "Accept." 

*On inspector phone:* "Payment released — GHS 49.20."

**[4:45]** "That's EyeOnSite. Trust, verified."

---

## What Makes the Demo Memorable

1. **Two phones, live** — nothing is pre-recorded or simulated
2. **The GPS overlay on photos** — the moment a stakeholder sees coordinates on a construction photo, they get it
3. **The map with inspector track** — visual proof of "someone was there"
4. **The checklist** — structured data, not just random photos
5. **The quality score** — the app is computing trust, not just collecting media
6. **The notification flow** — it feels like Bolt/Uber, which everyone understands
7. **Real Accra construction site** — not a stock photo, not a simulation

---

*See [EOS-09-ROADMAP.md](../EOS-09-ROADMAP.md) for how demo MVP maps to V1.*
*See [EOS-03-ARCHITECTURE.md](../EOS-03-ARCHITECTURE.md) for full architecture (demo is a subset).*
*See [EOS-A6-CLAUDE-MD.md](../EOS-A6-CLAUDE-MD.md) for repo CLAUDE.md (demo follows same patterns).*
