# 11 — Execution Plan

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Week 1 — Immediate Actions

### Day 1–2: Foundation
- [ ] Register domain: eyeonsite.com or eyeonsite.co (check availability)
- [ ] Create Google Workspace account (eyeonsite domain email)
- [ ] Create Firebase project (eyeonsite-prod)
- [ ] Create GitHub organization + private monorepo (eyeonsite/eyeonsite)
- [ ] Create WhatsApp Business account
- [ ] Create social media: X (@eyeonsite), LinkedIn company page
- [ ] Set up Google Sheets tracker: tasks, clients, feedback, revenue, inspectors

### Day 3–4: Demand Seeding
- [ ] Identify and join 10+ diaspora Facebook groups (UK-Ghana, US-Ghana, Canada-Ghana)
- [ ] Identify and join 5+ diaspora WhatsApp groups
- [ ] Draft and post introductory message (not salesy — personal story about the trust problem)
- [ ] Reach out to 5 personal contacts in the diaspora who are building/buying property in Ghana
- [ ] Post on LinkedIn: personal story about the construction trust gap (content marketing)
- [ ] Post on X: thread about diaspora investment challenges

### Day 5–7: First Manual Task
- [ ] Secure first client (personal network or group response)
- [ ] Create Google Form for task intake (location, what to verify, budget, deadline)
- [ ] Travel to first task site in Accra
- [ ] Take GPS-stamped photos (use GPS Camera app — timestamps + coordinates in photo)
- [ ] Complete construction checklist manually
- [ ] Create first evidence report (Google Docs template → PDF)
- [ ] Deliver to client via WhatsApp/email
- [ ] Invoice via Stripe (Manti Labs LLC) or manual MoMo
- [ ] Collect feedback: structured 5-question survey

## Month 1 — V0 Execution

### Week 2–3: Iterate
- [ ] Complete 5–8 more manual tasks
- [ ] Refine checklist based on client feedback (what questions matter most?)
- [ ] Refine photo requirements (which angles are most valuable?)
- [ ] Refine report format (what do clients actually look at?)
- [ ] Test pricing: $20, $30, $50 per task — find ceiling
- [ ] Document every client conversation in Google Sheets (the real product spec is here)

### Week 3–4: Validate and Decide
- [ ] 10–15 total completed tasks
- [ ] Review kill criteria: < 5 willing after 20 outreach → STOP
- [ ] If alive: compile V0 learnings document
- [ ] Client interviews: 30-min calls with 5+ clients. What would make this worth 2x the price? Would you use it monthly? Would you recommend to a friend?
- [ ] Inspector recruitment: identify 3–5 potential inspectors (university students, field agents, surveyors in Accra)
- [ ] Start V1 KMP project scaffolding (if demand validated)

### V0 Deliverables
1. V0 learnings document (demand validated yes/no, pricing data, feature requests)
2. Client interview summaries (5+ interviews)
3. Refined construction checklist (v2, based on real usage)
4. Evidence report template (v2, based on real delivery)
5. Go/no-go decision for V1

## Month 2–3 — V1 Development

### Assumption: V0 validated demand. Kill criteria not met.

### Sprint 1 (Weeks 5–6): Foundation
- [ ] Scaffold KMP monorepo (shared/, androidApp/, iosApp/, webApp/, functions/)
- [ ] Set up Gradle version catalog (libs.versions.toml)
- [ ] Implement domain models in shared/commonMain (Task, Evidence, Inspector, Escrow)
- [ ] Set up Firebase project: Auth, Firestore, Storage, Functions
- [ ] Write Firestore Security Rules (basic version)
- [ ] Write Cloud Storage Security Rules
- [ ] Create CLAUDE.md for the repo
- [ ] Set up GitHub Actions CI (build shared module, run tests)

### Sprint 2 (Weeks 7–8): Inspector App Core
- [ ] Phone OTP registration (Firebase Auth)
- [ ] Inspector onboarding flow (profile, ID upload, training video placeholder)
- [ ] Map view: available tasks (Google Maps SDK)
- [ ] Task acceptance flow
- [ ] CameraX integration: in-app photo capture with GPS overlay
- [ ] Checklist completion UI
- [ ] Evidence submission (SQLDelight queue → Firestore + Cloud Storage)

### Sprint 3 (Weeks 9–10): Client App Core
- [ ] Phone OTP registration
- [ ] Task creation flow (location picker, instructions, checklist, budget)
- [ ] Stripe integration: fund escrow
- [ ] Task status tracking (real-time Firestore listener)
- [ ] Evidence review screen (photo gallery, checklist responses, GPS overlay)
- [ ] Accept / Dispute buttons

### Sprint 4 (Weeks 11–12): Backend + Glue
- [ ] Cloud Functions: task matching (geo-query → FCM ping → acceptance)
- [ ] Cloud Functions: evidence validation pipeline (GPS check, EXIF check, basic quality)
- [ ] Cloud Functions: escrow management (hold, release, refund)
- [ ] Cloud Functions: notification dispatch (FCM)
- [ ] Flutterwave integration: inspector payout to MoMo
- [ ] Rating system: post-task mutual rating
- [ ] Basic admin dashboard (Compose for Web): task list, dispute queue

### Sprint 5 (Weeks 13–14): Polish + Launch Prep
- [ ] Landing page (Compose for Web → Firebase Hosting)
- [ ] Recruit and onboard 10–15 inspectors in Accra
- [ ] Inspector training: 3 test tasks each
- [ ] End-to-end testing: full task lifecycle (create → fund → match → capture → submit → review → release)
- [ ] Bug fixes, performance optimization
- [ ] Play Store listing preparation (screenshots, description)
- [ ] Soft launch: first 10 real tasks through the app

## Month 4–5 — V1 Live Operations

- [ ] Monitor task completion daily
- [ ] Manual dispute resolution (Maku reviews every dispute)
- [ ] Manual quality review of first 50 evidence packages
- [ ] Inspector feedback sessions (weekly WhatsApp group check-in)
- [ ] Client feedback collection (post-task survey, monthly call)
- [ ] Iterate on checklist templates based on real usage
- [ ] Track metrics: completion rate, dispute rate, quality score, revenue

## Hiring Plan

Hires are triggered by revenue/traction, not by timeline.

| Role | Trigger | Estimated Timeline | Where |
|------|---------|-------------------|-------|
| **Operations Lead** | 200+ tasks/month, dispute volume requires dedicated person | V2 (Month 6–8) | Accra (full-time) |
| **Mobile Developer** | V2 scope requires parallel development | V2 (Month 8–10) | Remote (KMP experience) |
| **Growth / Community** | Diaspora marketing needs dedicated person | V2 (Month 8–10) | Remote (diaspora network) |
| **Backend Engineer** | API + enterprise features | V3 (Month 12–14) | Remote |
| **City Launcher** | City #3 expansion | V3 (Month 14–16) | Lagos or Nairobi |

## Budget Allocation

### V0 (Month 1): ~$100

| Item | Cost | Notes |
|------|------|-------|
| Transport (site visits) | $50 | Bolt/trotro in Accra |
| SIM cards / data | $20 | WhatsApp Business, client calls |
| Domain registration | $12 | eyeonsite.com |
| Google Workspace | $7/mo | Email, Drive, Forms |
| Miscellaneous | $11 | Printing, materials |

### V1 (Months 2–5): ~$500/month

| Item | Cost/Month | Notes |
|------|------------|-------|
| Firebase Blaze | $25 | Pay-as-you-go |
| Google Maps API | $50 | Free tier covers initial volume |
| Stripe fees | Variable | Per transaction |
| Flutterwave fees | Variable | Per transaction |
| Inspector test task payments | $100 | Onboarding test tasks |
| Transport | $50 | Site visits, inspector meetings |
| Google Workspace | $7 | — |
| Miscellaneous | $50 | — |
| Marketing (content) | $100 | LinkedIn ads, boosted posts |
| **Total** | **~$430–$500** | Covered by Farmerline salary runway |

### V2 (Months 6–10): ~$1,500/month

Additional: Marketing budget ($500), ops lead stipend ($500), cloud costs increase ($200).

## Personal Transition Planning

### Current Situation
- Full-time at Farmerline (Senior Mobile Developer)
- 30-day notice period required
- EyeOnSite is a side project until V1 traction proves viability

### Transition Triggers
- **Stay at Farmerline + side project:** V0–V1. EyeOnSite runs on evenings/weekends + V0 manual work during off-hours.
- **Give notice at Farmerline:** Only when TWO conditions are met:
  1. V1 is live with 50+ completed tasks and growing
  2. Either: (a) revenue covers basic living expenses ($800+/month), OR (b) fundraise committed ($50K+ in hand)
- **Full-time EyeOnSite:** After notice period. All-in.

### IP Considerations
- Farmerline employment contract has IP assignment clause
- EyeOnSite (and all Manti Labs ventures) should be formally listed as pre-existing IP at any new employment
- All EyeOnSite work done on personal devices, personal time, personal accounts
- No Farmerline resources, infrastructure, or proprietary knowledge used

## Decision Log Starting Entries

| Date | Decision | Reasoning | Owner |
|------|----------|-----------|-------|
| May 2026 | KMP over Flutter | You write Kotlin daily. Max code sharing. Native feel. | Maku |
| May 2026 | Accra before Lagos | You're physically there. MEST network. Less competition. Strong UK-Ghana corridor. | Maku |
| May 2026 | Construction as V1 vertical | Highest emotional stakes (life savings). Recurring need (monthly checks). Clear evidence criteria. | Maku |
| May 2026 | Firebase over Supabase | Offline persistence built-in. Phone OTP works in Africa. KMP SDK exists. | Maku |
| May 2026 | Compose for Web over Next.js | One language everywhere. Acceptable SEO trade-off for admin/portal. | Maku |
| May 2026 | 18% take rate | Below Bolt/Uber (20-25%). Above payment-only platforms (3-5%). Balanced for inspector retention. | Maku |
| May 2026 | Stripe + Flutterwave dual rails | Stripe for diaspora USD/GBP/EUR. Flutterwave for local MoMo/M-Pesa. Best coverage. | Maku |

---

*See [EOS-09-ROADMAP.md](./EOS-09-ROADMAP.md) for version milestone details.*
*See [EOS-A7-DECISION-LOG.md](./EOS-A7-DECISION-LOG.md) for the living decision log.*
