# B5 — Landing Page Specification

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Purpose

The landing page (eyeonsite.com) has ONE job: convert a diaspora visitor who just heard about EyeOnSite (from WhatsApp, Facebook, LinkedIn, or a friend) into a user who downloads the app or creates their first task.

**Implementation:** Compose for Web (Kotlin/JS) on Firebase Hosting. See EOS-04 for technical rationale.

---

## Page Structure (Single Page, 8 Sections)

### Section 1: Hero

**Headline:** See your house before you fly home.
**Subheadline:** On-demand, GPS-verified inspections of your construction, property, or farm in Africa. Your money stays in escrow until you're satisfied.
**CTA:** "Start your first check — free" → App download or web task creation
**Visual:** Split — left side: a diaspora person looking at phone (illustration or real photo). Right side: app screenshot showing an evidence report with GPS-overlaid construction photo.
**Trust badge row:** "GPS-verified · Escrow-protected · 48-hour turnaround · Starting at $20"

### Section 2: The Problem (Emotional Hook)

**Headline:** You sent the money. Now what?
**Body:** (3 short paragraphs)
- "You're in London / Houston / Toronto. You wired £40,000 for a house in Accra. Your brother hired a contractor. The WhatsApp photos came monthly — at first."
- "By month four, the updates stop. 'Materials are expensive.' 'Rain delayed us.' 'Don't worry, it's coming.'"
- "This isn't an edge case. This is the default experience for diaspora families building back home."
**Data point:** "Africa's diaspora sends $100 billion home every year. Most of it builds on faith, not facts."

### Section 3: How It Works (3 Steps)

**Headline:** How EyeOnSite works

**Step 1: Post your task**
Icon: map pin
"Pin the location, tell us what to check, set your budget. Your money goes into escrow — safe until you're satisfied."

**Step 2: We send an inspector**
Icon: person walking with phone
"A verified, ID-checked local inspector heads to your site within hours. GPS confirms they're there."

**Step 3: See the proof**
Icon: eye with checkmark
"GPS-stamped photos, video, checklist — all captured in-app. Review and approve, or request changes. Your money only moves when you're happy."

### Section 4: Evidence Report Preview

**Headline:** This is what proof looks like.
**Visual:** Large, detailed screenshot/mockup of a real evidence report:
- Photo grid (8+ photos with GPS overlay)
- Map with pin showing exact inspection location
- Checklist with inspector responses
- Quality score badge: 0.87/1.0
- Timestamp: "Captured at 14:32 UTC, May 25, 2026"
- GPS coordinates displayed in JetBrains Mono

**Contrast callout:** "Your contractor sends this [blurry WhatsApp photo]. We send this [full evidence report]."

### Section 5: Verticals

**Headline:** One platform. Every verification you need.

**Cards (3 primary, 1 "coming soon"):**

| Card | Icon | Title | Description |
|------|------|-------|-------------|
| 🏗️ | Construction | Construction progress | Monthly checks: workers on site, materials, build stage, quality assessment |
| 🏠 | Property | Property verification | Land existence, boundary markers, condition, occupancy, address match |
| 🌾 | Agriculture | Farm monitoring | Crop health, irrigation, inputs, storage, seasonal progress |
| 📦 | More | Enterprise & more | Fintech collateral, insurance claims, NGO projects. Contact us. |

### Section 6: Trust and Safety

**Headline:** Your money is safe. Your inspector is real.

**Three trust pillars:**

1. **Escrow-protected**
"Your payment is held until you approve the evidence. Not satisfied? Get a refund."

2. **Inspector-verified**
"Every inspector is ID-checked, background-screened, trained, and tested before their first task."

3. **GPS-stamped**
"Every photo is captured in-app with GPS coordinates, timestamps, and device verification. No gallery uploads. No fakes."

### Section 7: Pricing

**Headline:** Starts at GHS 30 (~$20)

| Tier | Price | What You Get |
|------|-------|-------------|
| **Basic check** | From GHS 30 | 5+ photos, GPS verification, basic checklist, 48hr turnaround |
| **Detailed inspection** | From GHS 60 | 12+ photos, video, full checklist, inspector notes, same-day available |
| **Professional assessment** | From GHS 120 | Expert-tier inspector, certified assessment, detailed report |
| **Enterprise** | Custom | API access, bulk tasks, custom reports, SLA guarantees. Contact us. |

"Your first check is free. No credit card required."

### Section 8: Footer

- Logo + tagline
- Links: How it works, Pricing, For Inspectors, For Enterprise, About, Contact
- Social: X, LinkedIn, Instagram
- Legal: Terms, Privacy, Cookie Policy
- "EyeOnSite is a Manti Labs product. Built in Accra. 🇬🇭"
- Newsletter signup: "Get diaspora investment tips and EyeOnSite updates."

---

## Inspector Recruitment Page (eyeonsite.com/inspectors)

### Hero

**Headline:** Get paid to be someone's eyes on the ground.
**Subheadline:** Join EyeOnSite as a verified inspector. Earn GHS 50–200 per task. Work when you want.
**CTA:** "Apply now" → Inspector onboarding flow

### How It Works (Inspector Side)

1. **Apply and get verified** — ID check, background screen, training module, test task
2. **Accept tasks near you** — Map shows available tasks. Pick the ones that fit your schedule.
3. **Go, capture, get paid** — Travel to site, take photos with our app, submit. Money hits your MoMo instantly.

### Earnings Calculator

Interactive widget: slider for "tasks per week" → shows estimated weekly/monthly earnings

```
Tasks per week: [3] ──────●──── [10]

Estimated weekly earnings: GHS 240 – GHS 600
Estimated monthly earnings: GHS 960 – GHS 2,400

* Based on average task value of GHS 60–80
```

### Requirements
- Smartphone with camera (Android or iOS)
- National ID or passport
- Based in Accra (more cities coming soon)
- Ability to travel to task locations
- Pass background check and training

---

## Enterprise Page (eyeonsite.com/enterprise)

### Hero

**Headline:** Field truth at API speed.
**Subheadline:** Physical verification for fintech lenders, insurers, and organizations. On-demand. At scale.

### Use Cases

- **Fintech:** Verify MSME collateral before disbursing. Reduce NPL rates.
- **Insurance:** Independent claims verification. Faster than sending an adjuster.
- **NGO:** Monitor project implementation. Independent of self-reported data.

### API Preview

```json
POST /v1/tasks
{
  "vertical": "FINTECH",
  "location": { "lat": 5.614, "lng": -0.186 },
  "template": "business_verification",
  "budget": { "amount": 1500, "currency": "GHS" }
}

→ Evidence webhook delivered within 24 hours
```

### CTA

"Start a pilot — 50 verifications free. Talk to us."
→ Contact form or Calendly link

---

## Technical Implementation Notes (Compose for Web)

### Page Routing

```kotlin
// webApp/src/jsMain/pages/Router.kt
fun main() {
    renderComposable("root") {
        val currentPath = window.location.pathname
        when {
            currentPath == "/" -> LandingPage()
            currentPath == "/inspectors" -> InspectorPage()
            currentPath == "/enterprise" -> EnterprisePage()
            currentPath.startsWith("/dashboard") -> DashboardApp()
            currentPath.startsWith("/portal") -> ClientPortalApp()
            else -> NotFoundPage()
        }
    }
}
```

### SEO Mitigation

Compose for Web is client-side rendered — poor for SEO. Mitigations:
1. Use `prerender.io` (or Firebase Hosting's built-in prerender) to generate static HTML snapshots for crawlers
2. `<meta>` tags and `<title>` set in the HTML template (not in Kotlin/JS — search engines may not execute JS)
3. Open Graph tags for social sharing previews
4. Sitemap.xml generated statically

### Firebase Hosting Config

```json
{
  "hosting": {
    "public": "webApp/build/dist/js/productionExecutable",
    "rewrites": [
      { "source": "**", "destination": "/index.html" }
    ],
    "headers": [
      {
        "source": "**",
        "headers": [
          { "key": "X-Frame-Options", "value": "DENY" },
          { "key": "X-Content-Type-Options", "value": "nosniff" }
        ]
      }
    ]
  }
}
```

---

*See [EOS-B1-BRAND-IDENTITY.md](./EOS-B1-BRAND-IDENTITY.md) for colors, typography, and logo usage.*
*See [EOS-04-TECHNICAL-STACK.md](../EOS-04-TECHNICAL-STACK.md) for Compose for Web technical details.*
