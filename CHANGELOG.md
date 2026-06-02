# Changelog

All notable changes to EyeOnSite are documented here.
Auto-managed by [release-please](https://github.com/googleapis/release-please) via Conventional Commits.

---

## [0.2.0](https://github.com/ma-za-kpe/eyeonsite/compare/v0.1.0...v0.2.0) (2026-06-02)


### Features

* add @Preview to LogoMark; fix local distribution docs ([#18](https://github.com/ma-za-kpe/eyeonsite/issues/18)) ([962c005](https://github.com/ma-za-kpe/eyeonsite/commit/962c005ba44f185cef2262fd12fb26030ebdbd01))
* add country picker to sign-in screen ([b7e1378](https://github.com/ma-za-kpe/eyeonsite/commit/b7e13781642c1e8b1f04d4ffb46ed965da9721f3))
* add Firebase App Distribution Gradle plugin ([116a369](https://github.com/ma-za-kpe/eyeonsite/commit/116a36919a30da515ce671ede7ba94de2c628e71))
* auth flow — AuthViewModel, OtpScreen, RoleSelectionScreen, 11 unit tests ([35ce0fc](https://github.com/ma-za-kpe/eyeonsite/commit/35ce0fcebc65b070de736a4e5ce47bd6aa3d8609))
* auth flow — OtpScreen, RoleSelectionScreen, AuthViewModel, 11 unit tests ([4b20984](https://github.com/ma-za-kpe/eyeonsite/commit/4b20984d91c50cea47bf2c209915ffd527925e56))
* Client Dashboard — task hub, escrow summary, 20 tests, validate Gradle task ([0bb48b3](https://github.com/ma-za-kpe/eyeonsite/commit/0bb48b3f4413dad0aa320be58ae2613c31d2f929))
* Client Dashboard — task hub, escrow summary, 20 tests, validate task ([#8](https://github.com/ma-za-kpe/eyeonsite/issues/8)) ([dab3c6c](https://github.com/ma-za-kpe/eyeonsite/commit/dab3c6cd31a53d7d779f2bcdb0467260ddc2b31b))
* Cloud Storage upload and Firestore evidence doc write ([71c2838](https://github.com/ma-za-kpe/eyeonsite/commit/71c28386ab32a55b23d8371e296d3a9090ef3c28))
* dispute and revision request flow (Task [#14](https://github.com/ma-za-kpe/eyeonsite/issues/14)) ([1bd9b29](https://github.com/ma-za-kpe/eyeonsite/commit/1bd9b29085679032e0e9bd86d1f0ab4a0f5f9b2e))
* dual app personalities, dark/light themes, Settings screen ([#6](https://github.com/ma-za-kpe/eyeonsite/issues/6)) ([8c75f3a](https://github.com/ma-za-kpe/eyeonsite/commit/8c75f3af60084911e73440728c05a884487d50d5))
* EOS-14 security hardening pass ([#14](https://github.com/ma-za-kpe/eyeonsite/issues/14)) ([dfe30da](https://github.com/ma-za-kpe/eyeonsite/commit/dfe30da6768889f349f508e6c53ec5cc56d9427a))
* Evidence Capture — CameraX, GPS, upload pipeline, 16 tests ([#7](https://github.com/ma-za-kpe/eyeonsite/issues/7)) ([f6b04d6](https://github.com/ma-za-kpe/eyeonsite/commit/f6b04d67d5daeba58ebe6f597e3c9d4bc7ffa9e1))
* evidence review — client accept/revise/dispute flow with 20 tests ([56f4829](https://github.com/ma-za-kpe/eyeonsite/commit/56f4829eab257879fd0ef3e64ec5ad6bbdd8cd29))
* evidence review — client accept/revise/dispute flow with 20 tests ([87252fa](https://github.com/ma-za-kpe/eyeonsite/commit/87252faed242bac1ede381e8eabe2c04becb8f66))
* Firebase App Distribution setup — CI workflow, local CLI distribution, docs (EOS-A9) ([34cc435](https://github.com/ma-za-kpe/eyeonsite/commit/34cc435713f412f0540c4e4a0c2a7b1031f55dff))
* Firestore task creation, geohash matching, FCM backoff, all KMP features ([4483dd5](https://github.com/ma-za-kpe/eyeonsite/commit/4483dd57f7a9d6ad8b71e1de4a28b40ebd833597))
* **functions:** Cloud Vision AI photo quality scoring ([#13](https://github.com/ma-za-kpe/eyeonsite/issues/13)) ([ea7a706](https://github.com/ma-za-kpe/eyeonsite/commit/ea7a7064e02ff34aa0d549e3c9a9da8ab4344ff5))
* fundTask + releaseEscrow — demo escrow lifecycle ([3fbe6ab](https://github.com/ma-za-kpe/eyeonsite/commit/3fbe6abe7219973cba12c791e60a810aa5e431f5))
* fundTask + releaseEscrow — demo escrow lifecycle ([dd8d2af](https://github.com/ma-za-kpe/eyeonsite/commit/dd8d2af76b048cf47a52c254d0476178ddcf9dae))
* implement EOS-14 production hardening slices ([cee1410](https://github.com/ma-za-kpe/eyeonsite/commit/cee14103c5fd8b3d9d4c0085796524673a58b33b))
* inspector onboarding flow (Task [#15](https://github.com/ma-za-kpe/eyeonsite/issues/15)) ([fa38b4b](https://github.com/ma-za-kpe/eyeonsite/commit/fa38b4b4072e7b0299ea8aa1c23a771471943ed9))
* inspector task map — Google Maps Compose hub with custom markers ([4d360a5](https://github.com/ma-za-kpe/eyeonsite/commit/4d360a50893c8279222dc7ce1d9e224ed811acc0))
* KMP foundation — Firebase SDK, EyeOnSite theme, sign-in screen, docs ([bcaf072](https://github.com/ma-za-kpe/eyeonsite/commit/bcaf07288e5de297759e3e1d0209c115e29839fb))
* KMP foundation, theme, SignInScreen, analytics, tooling, CI config ([863f7d2](https://github.com/ma-za-kpe/eyeonsite/commit/863f7d272a7fb6152a8dc18f36a05e5ef33f7495))
* KMP product flavors — client and inspector APKs ([#22](https://github.com/ma-za-kpe/eyeonsite/issues/22)) ([4d61aab](https://github.com/ma-za-kpe/eyeonsite/commit/4d61aabcb69d6d4a6870e7b018b389326e7b3ffe))
* Koin DI — inject AuthViewModel, FirebaseAuthProvider, AnalyticsService ([1899358](https://github.com/ma-za-kpe/eyeonsite/commit/18993587b981632e4e6119805d52971d71df2954))
* Koin DI — inject AuthViewModel, FirebaseAuthProvider, AnalyticsService across KMP ([f872beb](https://github.com/ma-za-kpe/eyeonsite/commit/f872beb22ccc0019734c1a53053db56b4f843d12))
* landing page, legal pages, EyeOnSite mark, tooling ([4b8341b](https://github.com/ma-za-kpe/eyeonsite/commit/4b8341bacd533dcf1f44a0dd9a85de77723ae8ff))
* Matching Engine Cloud Functions — geo-match, FCM pings, 24 tests ([#10](https://github.com/ma-za-kpe/eyeonsite/issues/10)) ([6410ba4](https://github.com/ma-za-kpe/eyeonsite/commit/6410ba4607deda1e7beba80b13b5ef2d364cc6b4))
* offline-first evidence submission ([#12](https://github.com/ma-za-kpe/eyeonsite/issues/12)) ([d71583c](https://github.com/ma-za-kpe/eyeonsite/commit/d71583cd7067d14e78fc47758ad0302f97350bf3))
* onEvidenceSubmitted Cloud Function, Firestore rules, seed data ([388c3e3](https://github.com/ma-za-kpe/eyeonsite/commit/388c3e32f90118a2873c56ef6414d656c0280d8f))
* onEvidenceSubmitted, Firestore rules, seed data ([7513321](https://github.com/ma-za-kpe/eyeonsite/commit/7513321f53415cb9686ee4034f9883e37b1d2f9e))
* persist auth session so users stay logged in across app restarts ([b93f9db](https://github.com/ma-za-kpe/eyeonsite/commit/b93f9db2c68a5899ca6c79cb7be19b8887e9563b))
* PhotoQualityAnalyzer — local blur/brightness scoring with NoOp for non-Android ([32d27b3](https://github.com/ma-za-kpe/eyeonsite/commit/32d27b3bb38873a0d7fc12cebbbddf180720f67e))
* Privacy/Terms pages, working legal links, EyeOnSite launcher icon ([b766e05](https://github.com/ma-za-kpe/eyeonsite/commit/b766e052189bf5b2548165a4916544df78ee636e))
* SQLDelight offline cache + pending upload queue ([7776a1a](https://github.com/ma-za-kpe/eyeonsite/commit/7776a1a0f971b4f5e62f833c66b778d2f6efec12))
* Stripe + Flutterwave payment integration ([#11](https://github.com/ma-za-kpe/eyeonsite/issues/11)) ([2726fcf](https://github.com/ma-za-kpe/eyeonsite/commit/2726fcf50ff8576df2b92c48bf4e214301a55ce4))
* TaskCreationScreen, InspectorMatchingScreen, HomeScreen nav ([1ae6cad](https://github.com/ma-za-kpe/eyeonsite/commit/1ae6cad5540753df27bf6ec5f1be0ed2484c7d3b))
* TaskCreationScreen, InspectorMatchingScreen, updated HomeScreen nav ([d967df1](https://github.com/ma-za-kpe/eyeonsite/commit/d967df16d263ae337e75d2c0d3afa23aa9d5fff5))
* **versioning:** auto-generate AppVersion.kt from release-please manifest ([#17](https://github.com/ma-za-kpe/eyeonsite/issues/17)) ([06770c1](https://github.com/ma-za-kpe/eyeonsite/commit/06770c1380fcf9189085cffcaf03d0a033f16207))
* **versioning:** wire release-please version into Android builds and distribute workflow ([4d57ee2](https://github.com/ma-za-kpe/eyeonsite/commit/4d57ee2c8eddcd39a9d11955ca9890543564db52))
* **versioning:** wire release-please version into builds and distribute ([232b6e0](https://github.com/ma-za-kpe/eyeonsite/commit/232b6e0107c72a3e4c40d245ec228fe8037e36ea))
* wire all unused domain contracts into live flows (EOS-14 cont.) ([2ddc8cb](https://github.com/ma-za-kpe/eyeonsite/commit/2ddc8cb39cc0340b45d9bceb4f88a869fda2e6c7))
* wire Google Maps API key from local.properties into Android manifest ([7baaae8](https://github.com/ma-za-kpe/eyeonsite/commit/7baaae820d1767a2fc67baaaa5944f4c747ebcaf))
* wire real Firestore EvidenceReviewer for accept/dispute/revision ([acd7147](https://github.com/ma-za-kpe/eyeonsite/commit/acd7147ef603a754f26f81768d66f300541649ce))
* WorkManager pending upload sync worker ([9c75e2e](https://github.com/ma-za-kpe/eyeonsite/commit/9c75e2ed80818f9cb590ec1fcafa2b330e9bf67f))


### Bug Fixes

* **ci:** install firebase-tools on CI runner and add functions tests to pre-commit hook ([de41792](https://github.com/ma-za-kpe/eyeonsite/commit/de41792bf2711a707bed3ceb1f0af27053fab4ab))
* **ci:** remove flaky Gradle wrapper validation from distribute workflow ([e182f16](https://github.com/ma-za-kpe/eyeonsite/commit/e182f16dcf959ee1dc5febbca579f5e4a4d42acf))
* **ci:** remove flaky Gradle wrapper validation step ([909a619](https://github.com/ma-za-kpe/eyeonsite/commit/909a619a6a9ff953178d2301b67943ccd36b47cd))
* **functions:** use dedicated jest config for storage rules test ([f4d5eb9](https://github.com/ma-za-kpe/eyeonsite/commit/f4d5eb9c71e5552f439765692dff265c1be4c0ef))
* hardcode Firebase app ID in distribute workflow (env var expansion broken on Windows bash) ([b73f6a4](https://github.com/ma-za-kpe/eyeonsite/commit/b73f6a4467e204b5119377e16d4160df73b7c541))
* Koin JS webpack errors + WasmJS routing ([2e22a55](https://github.com/ma-za-kpe/eyeonsite/commit/2e22a55541e51fcb91d0ced7c833ef3c826558a6))
* make gradlew executable for CI runners ([a447764](https://github.com/ma-za-kpe/eyeonsite/commit/a4477648dbab7528126ba30881764092054a71ad))
* matching engine audit — race fix, terminal statuses, dep upgrades ([b1887e4](https://github.com/ma-za-kpe/eyeonsite/commit/b1887e4b368cd31e64fa5510d3862d0abbe20005))
* remove koin-compose-viewmodel from JS path; fix WasmJS routing ([3649586](https://github.com/ma-za-kpe/eyeonsite/commit/364958634565db6bc3c6e4004529757d927ea5e2))
* replace SVG LogoMark with Canvas-drawn EyeOnSite mark (all platforms) ([d781876](https://github.com/ma-za-kpe/eyeonsite/commit/d7818769c34b1e3974b9aa6d7d6836b69300fe11))
* **rules:** use resource==null to deny evidence overwrites and deletes ([51603b4](https://github.com/ma-za-kpe/eyeonsite/commit/51603b43a3aab5654efb6ce53f48ed7220f0710b))
* WebGL init failure + exhaustive TaskStatus when branches ([ee22eef](https://github.com/ma-za-kpe/eyeonsite/commit/ee22eef0b2add729b29d47e36226f6ca4ebc1cb0))


### Documentation

* update EOS-A6, EOS-A8 and add EOS-A10 E2E test architecture ([c99703c](https://github.com/ma-za-kpe/eyeonsite/commit/c99703c3e46be395c10fea7ab89b45a3c0b348ea))

## [0.1.0] — 2026-05-25

### Features

- KMP foundation: Firebase SDK, EyeOnSite brand theme, DM Sans + JetBrains Mono fonts
- SignInScreen shared across Android, iOS, and Web
- EyeOnSiteTheme with full light/dark Material3 color schemes
- Firebase Analytics + Auth wired into Android
- Landing page (Compose for Web) deployed to Firebase Hosting
- AnalyticsService interface with Firebase + Mock implementations
- Spotless + Detekt + pre-commit hooks
- GitHub Actions CI/CD pipeline
- .kiro spec-driven development structure
