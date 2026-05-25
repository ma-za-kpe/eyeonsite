# Landing Page — Design

## File Structure
```
app/webApp/src/webMain/kotlin/com/maku/eyeonsite/
├── main.kt                    # entry point — routes to LandingPage or App
├── landing/
│   ├── LandingPage.kt         # root composable, section orchestration
│   ├── sections/
│   │   ├── HeroSection.kt
│   │   ├── ProblemSection.kt
│   │   ├── HowItWorksSection.kt
│   │   ├── EvidencePreviewSection.kt
│   │   ├── VerticalsSection.kt
│   │   ├── TrustSection.kt
│   │   ├── PricingSection.kt
│   │   └── FooterSection.kt
│   └── components/
│       ├── NavBar.kt
│       ├── CtaButton.kt       # reusable CTA with analytics
│       └── TrustBadge.kt
```

## Component Design

### LandingPage
- Vertical scroll container
- Tracks scroll position to fire `section_viewed` analytics
- Passes `onCtaClick: (ctaId: String, section: String) -> Unit` down to sections

### HeroSection
- Dark background (DeepForest)
- Left: headline "See your house before you fly home." in DM Sans Medium 48sp
- Subheadline in Stone color
- Primary CTA button (Forest background)
- Trust badge row: "GPS-verified · Escrow-protected · 48-hour turnaround · Starting at $20"
- Fade-in animation on load (300ms delay, 600ms duration)

### ProblemSection
- Light background (Ivory)
- 3 paragraphs telling Ama's story
- Data callout: "$100B" in SavannaGold, large display text
- Slide-up animation on scroll-into-view

### HowItWorksSection
- 3 step cards: Post → Inspector → Proof
- Each card: icon + title + description
- Step connector line between cards (desktop only)
- Staggered fade-in (100ms between each card)

### EvidencePreviewSection
- Dark background (DeepForest)
- Mock evidence report card:
  - Photo grid (placeholder boxes with GPS icon overlay)
  - Map placeholder with pin
  - Checklist items (✅ Workers on site: Yes, etc.)
  - Quality score badge: 0.87/1.0 in SignalGreen
  - GPS coordinates in JetBrains Mono
- "Before vs After" contrast: blurry WhatsApp photo vs structured report

### VerticalsSection
- 4 cards: Construction 🏗️, Property 🏠, Agriculture 🌾, Enterprise 📦
- Hover effect: Forest border + slight elevation
- "Coming soon" badge on Enterprise card

### TrustSection
- 3 pillars: Escrow-protected, Inspector-verified, GPS-stamped
- Icon + title + description per pillar
- Forest accent color on icons

### PricingSection
- 4 tier cards: Basic (GHS 30), Detailed (GHS 60), Professional (GHS 120), Enterprise
- Highlighted card: Detailed (most popular)
- "First check free" callout in SavannaGold

### FooterSection
- Logo + tagline
- 4 link columns: Product, Inspectors, Enterprise, Company
- Social icons: X, LinkedIn, Instagram
- Legal: Terms, Privacy
- "Built in Accra 🇬🇭"

## Animation Strategy
- Use `androidx.compose.animation` APIs
- `AnimatedVisibility` with `fadeIn + slideInVertically` for scroll-triggered sections
- `animateFloatAsState` for hover effects
- All animations gated on `LocalReduceMotion` (respect system preference)

## Analytics Integration
```kotlin
// CtaButton fires this on every click:
analyticsService.logEvent("cta_clicked", mapOf("cta_id" to ctaId, "section" to section))

// LandingPage fires on section visibility:
analyticsService.logEvent("section_viewed", mapOf("section" to sectionName))

// On page load:
analyticsService.logEvent("landing_page_viewed", emptyMap())
```
