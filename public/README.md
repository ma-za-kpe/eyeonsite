# EyeOnSite Landing Page

Static HTML/CSS/JS landing page served via Firebase Hosting. No build step, no framework, no Gradle.

## Stack

| Concern | Solution |
|---|---|
| HTML | Vanilla, semantic, single `index.html` |
| CSS | Vanilla, `styles.css` — CSS custom properties, no preprocessor |
| JS | ES modules via `<script type="module">`, Firebase JS SDK v10 (CDN) |
| Fonts | DM Sans + JetBrains Mono — Google Fonts |
| Analytics | Firebase Analytics via `gtag` |
| Database | Cloud Firestore — `waitlist` collection |
| Auth (admin) | Firebase Auth — Google Sign-In |
| Hosting | Firebase Hosting (`eyeonsite-3b8e0`) |

## File structure

```
public/
├── index.html              # All pages (landing + /mazakpe admin)
├── styles.css              # Full design system
├── favicon.svg
├── apple-touch-icon.svg
├── android-chrome-192.svg
├── android-chrome-512.svg
└── site.webmanifest
```

## Local dev

```bash
# Serve locally on port 8080 — respects firebase.json rewrites and headers
firebase serve --only hosting --port 8080
```

Open `http://localhost:8080/` for the landing page.  
Open `http://localhost:8080/mazakpe` for the admin dashboard.

> **Hard refresh after CSS changes:** `Ctrl+Shift+R` (Windows/Linux) or `Cmd+Shift+R` (Mac).

## Deploy

```bash
firebase deploy --only hosting
```

No build step. Firebase uploads changed files only — typically takes under 10 seconds.

Live URL: https://eyeonsite-3b8e0.web.app

## Design system

All colors, spacing, and typography are CSS custom properties in `:root` at the top of `styles.css`.

| Token | Value | Use |
|---|---|---|
| `--forest` | `#0B6E4F` | Primary brand, buttons, links |
| `--deep-forest` | `#022B1E` | Dark section backgrounds |
| `--gold` | `#D4A843` | Accent, labels, featured elements |
| `--signal` | `#34D399` | Verified badges, success states |
| `--ivory` | `#F5F4F0` | Light section backgrounds |
| `--ink` | `#1A1A19` | Body text on light backgrounds |
| `--stone` | `#9C9A92` | Muted text, placeholders |
| `--footer-bg` | `#011710` | Footer background |
| `--font-sans` | DM Sans | All UI text |
| `--font-mono` | JetBrains Mono | GPS coords, IDs, data |

## Page sections (in order)

1. **NavBar** — sticky, gains blur/background on scroll
2. **Hero** — dark, DeepForest background
3. **Problem** — light, Ivory background
4. **How It Works** — dark, 3-step cards
5. **Evidence Preview** — light, mockup report card
6. **Verticals** — light, 4-tile grid
7. **Trust** — dark, 3 pillars
8. **Pricing** — light, 4-tier cards (Construction is featured)
9. **Waitlist** — dark, Firestore form + role selector
10. **Footer** — darkest (`#011710`)

## Waitlist form

Submits to the `/api/waitlist` Cloud Function, which validates the payload and writes to Firestore `waitlist` server-side.

Stored fields:

```js
{ name, email, role, location, message, createdAt, source: 'web_landing' }
```

`role` values: `diaspora_client` | `field_agent`

The browser does not write to Firestore directly. Firestore rules allow read access only for the admin email. See `firestore.rules` at the repo root.

## Admin dashboard (`/mazakpe`)

Google Sign-In gates access to a table of all waitlist entries. Only `makpalyy@gmail.com` is authorized — other accounts are rejected immediately after sign-in.

The SPA rewrite in `firebase.json` routes all paths to `index.html`; the page detects `window.location.pathname === '/mazakpe'` and swaps the visible `<main>`.

## Adding a new section

1. Add the HTML block inside `<main id="landing">` in `index.html`
2. Add styles to `styles.css` — use existing CSS custom properties for colors
3. Add a `gtag('event', 'section_viewed', { section: '...' })` call if tracking is needed
4. No rebuild or restart required — `firebase serve` picks up file changes on the next request

## Firebase config

Firebase credentials are inlined in `index.html` (they are public-safe — security is enforced by Firestore rules, not by hiding the config). Do not add server-side secrets here.

Project ID: `eyeonsite-3b8e0`  
Analytics ID: `G-J1X286KNHJ`
