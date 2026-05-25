# Landing Page — Requirements

**Feature:** EyeOnSite public landing page  
**Spec:** EOS-B5  
**Surface:** Compose for Web (Kotlin/JS) → Firebase Hosting  
**URL:** https://eyeonsite-3b8e0.web.app

## Goal
Convert a diaspora visitor (from WhatsApp/Facebook/LinkedIn) into a user who downloads the app or creates their first task.

## Functional Requirements

### FR-1: 8-Section Single Page
The page MUST contain all 8 sections in order:
1. Hero — headline, subheadline, CTA, trust badges
2. Problem — emotional hook, $100B data point
3. How It Works — 3 steps with icons
4. Evidence Preview — mock report with GPS overlay
5. Verticals — construction, property, agriculture, enterprise
6. Trust & Safety — escrow, verified inspectors, GPS-stamped
7. Pricing — GHS 30 / 60 / 120 / Enterprise tiers
8. Footer — links, social, legal, newsletter

### FR-2: CTAs
- Primary CTA: "Start your first check — free" (Hero)
- Secondary CTA: "For Inspectors" (Hero + Footer)
- Enterprise CTA: "Talk to us" (Pricing + Footer)
- All CTAs fire a Firebase Analytics event on click

### FR-3: Analytics
Every section scroll-into-view fires `section_viewed` event.  
Every CTA click fires `cta_clicked` event with `{cta_id, section}`.  
Page load fires `landing_page_viewed`.

### FR-4: Brand Compliance
- Colors: Forest (#0B6E4F), DeepForest (#022B1E), SavannaGold (#D4A843)
- Fonts: DM Sans (UI), JetBrains Mono (data/GPS coordinates)
- Logo: EyeOnSite wordmark from `docs/eyeonsite-wordmark-color.svg`
- Tagline: "Trust, verified."

### FR-5: Responsive
Must render correctly on mobile (360px+) and desktop (1280px+).

### FR-6: No Private Data
No API keys, Firebase config, user data, or internal URLs on the page.

## Non-Functional Requirements
- NFR-1: Page loads in < 3s on 4G (Compose for Web bundle is large — use loading state)
- NFR-2: All CTAs keyboard-accessible
- NFR-3: Animations respect `prefers-reduced-motion`
- NFR-4: Security headers set in firebase.json (X-Frame-Options, X-Content-Type-Options)
