# EyeOnSite — Project Steering

## What This Is
EyeOnSite is an on-demand verification marketplace for Africa. Diaspora investors post tasks; verified local inspectors deliver GPS-stamped, escrow-backed evidence. Think Bolt/Uber for physical verification.

**Built by:** Manti Labs (Maku Mazakpe)  
**Stage:** V0 → Demo MVP → V1  
**Package:** `com.maku.eyeonsite`  
**Firebase project:** `eyeonsite-3b8e0`  
**Live URL:** https://eyeonsite-3b8e0.web.app  
**GitHub:** https://github.com/ma-za-kpe/eyeonsite

## Core Principles
1. **Kotlin everywhere** — Android, iOS (CMP), Web (Compose for Web), no JS/TS/React/Flutter
2. **Google-first infrastructure** — Firebase + GCP only, no AWS/Azure/Supabase
3. **Offline-first** — every user action must succeed without connectivity
4. **Evidence integrity is the product** — GPS, EXIF, in-app capture only, never gallery uploads
5. **Escrow is a ledger** — all money movement is server-side, double-entry, auditable

## Module Structure
```
EyeOnSite/
├── app/
│   ├── shared/       # KMP shared UI + business logic (commonMain, androidMain, iosMain, jsMain)
│   ├── androidApp/   # Android app (Jetpack Compose + Firebase)
│   ├── webApp/       # Compose for Web (Kotlin/JS) — landing page + admin
│   └── iosApp/       # iOS app (CMP)
├── core/             # Pure Kotlin shared utilities
├── server/           # Ktor server placeholder
├── docs/             # Product documentation (EOS-00 through EOS-B5, EOS-D1)
└── .kiro/            # Kiro spec-driven development
    ├── steering/     # Persistent rules (this directory)
    └── specs/        # Feature specs (requirements, design, tasks)
```

## Current State (May 2026)
- KMP foundation built and deployed
- Theme: Color.kt, Theme.kt, Type.kt (DM Sans + JetBrains Mono)
- SignInScreen shared across all platforms
- Firebase Auth + Analytics wired into Android
- Web bundle deployed to Firebase Hosting
- Branch: `feat/landing-page-and-tooling`

## What NOT to Do
- No Next.js, React, Vercel, TypeScript for web surfaces
- No AWS or Azure services
- No Room (Android-only) — use SQLDelight
- No gallery/camera roll uploads for evidence
- No client-side escrow logic — Cloud Functions only
- No API keys in code — use Google Secret Manager
