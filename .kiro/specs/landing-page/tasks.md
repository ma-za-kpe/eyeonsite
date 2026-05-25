# Landing Page — Tasks

## Status Legend
- [ ] Not started
- [x] Complete

## Tasks

### T1: AnalyticsService interface + implementations
- [x] `shared/commonMain` — `AnalyticsService` interface
- [x] `shared/androidMain` — `FirebaseAnalyticsService` impl
- [x] `shared/jsMain` — `JsAnalyticsService` impl (console.log stub → real gtag later)
- [x] `shared/commonTest` — `MockAnalyticsService` for tests

### T2: LandingPage root + NavBar
- [ ] `LandingPage.kt` — scroll container, section orchestration, analytics on load
- [ ] `NavBar.kt` — logo + nav links + "Get Started" CTA

### T3: HeroSection
- [ ] Headline, subheadline, primary CTA
- [ ] Trust badge row
- [ ] Fade-in animation

### T4: ProblemSection
- [ ] 3-paragraph story
- [ ] $100B data callout
- [ ] Slide-up on scroll

### T5: HowItWorksSection
- [ ] 3 step cards with staggered animation

### T6: EvidencePreviewSection
- [ ] Mock report card (photo grid, checklist, quality score, GPS coords in JetBrains Mono)
- [ ] Before/after contrast callout

### T7: VerticalsSection
- [ ] 4 vertical cards with hover effects

### T8: TrustSection
- [ ] 3 trust pillars

### T9: PricingSection
- [ ] 4 tier cards, highlighted "most popular"

### T10: FooterSection
- [ ] Links, social, legal, newsletter signup

### T11: Analytics on all CTAs
- [ ] Every CTA button fires `cta_clicked` event
- [ ] Every section fires `section_viewed` on visibility

### T12: firebase.json security headers
- [ ] X-Frame-Options: DENY
- [ ] X-Content-Type-Options: nosniff
- [ ] Referrer-Policy: strict-origin-when-cross-origin

### T13: Build + deploy
- [ ] `./gradlew :app:webApp:jsBrowserProductionWebpack` passes
- [ ] `firebase deploy --only hosting` succeeds
- [ ] Live at https://eyeonsite-3b8e0.web.app
