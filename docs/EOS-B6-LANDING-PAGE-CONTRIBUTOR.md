# B6 — Landing Page Contributor Guide

**EyeOnSite** · Manti Labs · June 2026

---

## The Single Most Important Fact

**The landing page is static HTML — not Compose for Web.**

`public/index.html` is what Firebase Hosting serves at `geteyeonsite.com`. The Kotlin/JS `app/webApp` module compiles into a JS bundle but that bundle is **not loaded by `index.html`** and therefore **never runs in any browser**. All landing page changes go in `public/`.

This is the #1 thing that will confuse you if you read `CLAUDE.md` first, because it says the web surface uses Compose for Web. That's the long-term plan. For now, the live site is plain HTML/CSS/JS.

---

## File Map

```
public/
├── index.html       ← THE landing page. Every section is here.
├── styles.css       ← All styles. One file, one source of truth.
├── analytics.js     ← gtag initialisation + CTA click tracking.
├── security.js      ← CSP nonce / security helpers.
├── changelog.html   ← /changelog route. Update manually each release.
├── privacy.html     ← /privacy
├── terms.html       ← /terms
├── favicon.svg
├── site.webmanifest
└── ...              ← icons, robots.txt, sitemap.xml, etc.
```

`firebase.json` maps URL routes → files. Add new pages there.

---

## How to Add a New Section

### 1. Add the HTML block to `index.html`

Sections follow a consistent pattern. Insert your block **inside `<main id="landing">`** at the right position in the page flow:

```html
<!-- My New Section -->
<section class="section" id="my-section" aria-labelledby="my-heading">
    <div class="section-inner">
        <p class="label gold">SECTION LABEL</p>
        <h2 id="my-heading" class="section-h2 white">Section headline.</h2>
        <p class="section-subhead stone">Supporting copy goes here.</p>

        <!-- your content -->
    </div>
</section>
```

**Alternating backgrounds** — sections naturally sit on the `--deep-forest` dark background. Add class `bg-ivory` for a light section:

```html
<section class="section bg-ivory" ...>
```

**Common layout pattern — cards:**
```html
<div class="cards-row">
    <div class="card dark-card">
        <h3 class="card-title white">Title</h3>
        <p class="card-body stone">Body copy.</p>
    </div>
    <!-- repeat -->
</div>
```

### 2. Add CSS for any new elements

Open `styles.css` and add your rules **before** the `/* ── Admin */` block, in a clearly labelled comment group:

```css
/* ── My New Section ──────────────────────────────────────────────── */
.my-element {
    /* styles */
}

@media (max-width: 600px) {
    .my-element { /* mobile overrides */ }
}
```

**Do not add new CSS files.** Everything lives in `styles.css`.

### 3. Add analytics tracking (optional)

Any element with a `data-cta-id` attribute is automatically tracked as a `cta_click` event by `analytics.js`. No extra JS needed:

```html
<a href="#waitlist" data-cta-id="my_cta" data-cta-section="my_section">
    Click me
</a>
```

For section-view tracking, add the section's `id` and a display name to the `sectionTracking` array in `index.html` (search for `sectionTracking` — it's near the bottom of the inline `<script>`).

---

## Design System Quick Reference

All values live in the `:root` block at the top of `styles.css`.

### Colors

| Variable | Hex | Usage |
|----------|-----|-------|
| `--forest` | `#0B6E4F` | Primary green — buttons, accents |
| `--deep-forest` | `#022B1E` | Section/page background |
| `--footer-bg` | `#011710` | Footer background (slightly darker) |
| `--gold` | `#D4A843` | Labels, highlights, callouts |
| `--ink` | `#1A1A19` | Dark text on light backgrounds |
| `--ash` | `#4A4A47` | Secondary text on light backgrounds |
| `--stone` | `#9C9A92` | Muted text on dark backgrounds |
| `--ivory` | `#F5F4F0` | Light section background |
| `--white` | `#FFFFFF` | Headings on dark backgrounds |

Use CSS variables, never hex literals in new rules.

### Typography helpers

```
.gold / .white / .ink / .ash / .stone    — text colour
.label                                   — eyebrow label (uppercase, tracked)
.section-h2                              — section headline
.section-subhead                         — section sub-headline
```

### Buttons

```html
<!-- Primary (solid Forest) -->
<a href="#" class="btn btn-primary">Join Waitlist</a>

<!-- Ghost (transparent, white border) -->
<a href="#" class="btn btn-ghost">Learn More</a>

<!-- Secondary (SavannaGold) -->
<a href="#" class="btn btn-secondary">Highlight Action</a>

<!-- Sizes -->
<a href="#" class="btn btn-primary btn-sm">Small</a>
<a href="#" class="btn btn-primary btn-lg">Large</a>
<a href="#" class="btn btn-primary btn-full">Full width</a>
```

### Fonts

- **DM Sans** — all body and UI text (loaded from Google Fonts)
- **JetBrains Mono** — data, IDs, GPS coordinates, version badges (`font-family: var(--font-mono)`)

---

## Routing — Adding a New Page

1. Create `public/your-page.html`
2. Add a rewrite in `firebase.json`:

```json
{ "source": "/your-page", "destination": "/your-page.html" }
```

Place it **before** the `"source": "**"` catch-all.

3. If the page loads external scripts or connects to new domains, update the `Content-Security-Policy` header in `firebase.json`. See the CSP section below.

---

## Beta Download Links

The "Beta testing is open" section has two download buttons that currently link to `#waitlist` as a placeholder.

**To replace them with real Firebase App Distribution invite links:**

1. Firebase Console → **App Distribution** → select the **Client** app (`com.maku.eyeonsite`)
2. Testers & Groups → **internal** group → **Copy invite link**
3. Repeat for the **Inspector** app (`com.maku.eyeonsite.inspector`)
4. In `public/index.html`, find the `<!-- BETA DOWNLOAD LINKS -->` comment block and replace the two `href="#waitlist"` values with the real URLs

The real invite links look like:
```
https://appdistribution.firebase.google.com/testerapps/<hash>
```
where `<hash>` is a short alphanumeric token generated by Firebase — **not** the Firebase App ID string.

---

## Updating the Changelog Page

`public/changelog.html` is updated **manually** each time release-please cuts a new version tag.

1. Open `CHANGELOG.md` — release-please writes the new version section at the top
2. Open `public/changelog.html`
3. Copy the new version's features and fixes into a new `.cl-version` block at the top (above the previous version block)
4. Update the `v0.X.Y` version badge in `public/index.html` footer to match

Future automation option: add a CI step that runs a script converting `CHANGELOG.md` → `changelog.html` and commits the result. The current manual approach is fine at V0/V1 pace.

---

## Content Security Policy

The CSP header in `firebase.json` controls what the page can load. If you need to add an external script, image source, or API endpoint:

1. Find the `Content-Security-Policy` header value in `firebase.json`
2. Add your domain to the relevant directive:
   - `script-src` — new external JS file
   - `connect-src` — new API endpoint (fetch/XHR)
   - `img-src` — new image domain
   - `style-src` — new stylesheet origin
3. If adding an **inline** `<script>` block to `index.html`, generate its SHA-256 hash and add it to `script-src`:

```bash
# Generate hash for an inline script block
echo -n 'your inline script content' | openssl dgst -sha256 -binary | base64
# Add result as: 'sha256-<hash>'
```

Never add `'unsafe-inline'` to `script-src`.

---

## Testing Locally

**Option A — Firebase emulator (recommended, matches production exactly):**
```bash
firebase serve --only hosting
# Opens http://localhost:5000
```

**Option B — open directly:**
Just open `public/index.html` in a browser. Works for layout and styling. The waitlist form POST will fail (no Cloud Function), and Firebase auth won't work — but the visual is accurate.

---

## Deploying

Push to `main`. GitHub Actions CI runs automatically:

1. Lint → Tests → Build web bundle (Kotlin/JS, not used by the static page but verified)
2. `deploy-web` job deploys `public/` to Firebase Hosting via `FirebaseExtended/action-hosting-deploy`

**To deploy only hosting without waiting for a full CI run** (e.g., you only changed `public/`):

```bash
firebase deploy --only hosting
```

Requires Firebase CLI (`npm install -g firebase-tools`) and to be authenticated (`firebase login`).

---

## The Kotlin/JS webApp Module

`app/webApp/` contains a Compose for Web implementation of the landing page. It compiles successfully on every CI run but **is not wired into `index.html`** and does not affect what users see.

The plan is to eventually replace the static HTML with the Kotlin/JS bundle. When that time comes:

1. Add `<div id="ComposeTarget"></div>` to `index.html` where the app should render
2. Add a CI step that copies `app/webApp/build/dist/js/productionExecutable/webApp.js` into `public/`
3. Load it in `index.html`: `<script src="/webApp.js"></script>`
4. The static HTML sections would then be replaced by the Compose-rendered UI

Until that migration happens, treat `app/webApp/` as a Kotlin/iOS/Web shared UI prototype and edit the static `public/` files for any live-site changes.
