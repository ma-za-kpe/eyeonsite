# Landing Page - Tasks

## Status Legend

- [ ] Not started
- [~] In progress
- [x] Complete

## Tasks

### T1: AnalyticsService interface and implementations

- [x] `shared/commonMain` - `AnalyticsService` interface
- [x] `shared/androidMain` - `FirebaseAnalyticsService` implementation
- [x] `shared/jsMain` - `JsAnalyticsService` implementation (console stub until Firebase Analytics JS/gtag is wired)
- [x] `shared/commonTest` - `MockAnalyticsService` for tests

### T2: LandingPage root and NavBar

- [x] `LandingPage.kt` - scroll container, section orchestration, analytics on load
- [x] `NavBar.kt` - logo, nav links, and "Get Started" CTA

### T3: HeroSection

- [x] Headline, subheadline, primary CTA
- [x] Trust badge row
- [x] Fade-in animation

### T4: ProblemSection

- [x] Three-paragraph story
- [x] $100B data callout
- [x] Slide-up animation

### T5: HowItWorksSection

- [x] Three step cards with staggered animation

### T6: EvidencePreviewSection

- [x] Mock report card with photo grid, checklist, quality score, and GPS coordinates
- [x] Before/after contrast callout

### T7: VerticalsSection

- [x] Four vertical cards with hover effects

### T8: TrustSection

- [x] Three trust pillars

### T9: PricingSection

- [x] Four tier cards with highlighted construction tier

### T10: FooterSection

- [x] Links, legal copy, and final CTAs

### T11: Analytics on all CTAs

- [x] Every CTA button fires `cta_clicked`
- [x] Every section fires `section_viewed` on composition
- [ ] Replace console analytics with Firebase Analytics JS or gtag production integration

### T12: firebase.json security headers

- [x] `X-Frame-Options: DENY`
- [x] `X-Content-Type-Options: nosniff`
- [x] `Referrer-Policy: strict-origin-when-cross-origin`
- [x] `Permissions-Policy: camera=(), microphone=(), geolocation=()`

### T13: Build, visual QA, and deploy

- [x] `./gradlew :app:webApp:jsBrowserProductionWebpack` passes
- [x] Local dev server responds `200 OK` at `http://localhost:8080/`
- [ ] Desktop and mobile browser screenshot QA
- [ ] `firebase deploy --only hosting` succeeds from this branch or after merge
- [ ] Live deployment verified at `https://eyeonsite-3b8e0.web.app`
