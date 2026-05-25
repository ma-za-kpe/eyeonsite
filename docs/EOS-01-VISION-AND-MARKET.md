# 01 — Vision and Market Context

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## One-Liner

EyeOnSite is Bolt for physical verification — on-demand, geo-matched inspectors who provide GPS-stamped, escrow-backed evidence that diaspora investments and enterprise operations in Africa are real.

## The Problem

Every year, Africa's diaspora sends ~$100B home in remittances. A significant portion funds physical projects — houses, land, farms, businesses. The money moves. The trust doesn't.

### What Happens Today

1. A Ghanaian nurse in London sends £40,000 to her brother in Accra to build a house
2. The brother hires a contractor, sends WhatsApp photos for the first two months
3. By month four, photos stop. Excuses start. "Materials are expensive." "Rain delayed us."
4. She flies home 14 months later. The house is half-finished. The money is gone.
5. She has no legal recourse, no documentation, no way to verify what happened

This is not an edge case. This is the default experience.

### The Scale of the Pain

**Remittance flows into Africa (World Bank/KNOMAD, 2024):**

- Total Africa: ~$95–100B annually, growing ~10% CAGR
- Nigeria: $19.8–23B (35% of Sub-Saharan Africa's total)
- Ghana: $4.7–6.6B (7% of GDP, supports 30%+ of households)
- Kenya: $4.94B (record high, 18% YoY increase)
- Egypt: $22.7B (Africa's largest recipient)
- Projected: $121.73B by 2030, potentially $500B by 2035 if transfer costs fall

**Diaspora populations (OECD, Ghana Statistical Service, 2022–2025):**

- Ghana: 1.5–3M abroad; ~500K in OECD countries; 135,854 in UK (2021 census)
- Nigeria: 1.7M+ in US/UK alone; largest African diaspora globally
- Kenya: significant US/UK/Canada populations; $4.94B in remittances in 2024

**The trust deficit (Commonwealth Secretariat, various diaspora surveys):**

- 56%+ of diaspora investors cite corruption as the #1 barrier to investing back home
- 70% of real estate purchases in Lagos in 2021 were by diaspora Nigerians — mostly sight unseen
- Lagos State recovered ₦478.13M and 18 properties from real estate fraud in 2024 alone
- In Nigeria, an estate agent collected ₦21M from 81 different people for the same apartment
- A Kenyan diaspora buyer paid Sh4.25M for a never-built maisonette
- A US-based investor in Kenya discovered her property had fabricated land registration numbers — the title didn't exist
- In Zimbabwe, a construction company received ~$1M for a mansion, warehouse and mall, then diverted the funds
- No mandatory escrow accounts for developers in Kenya — zero accountability on funds

**Key insight:** The remittance rails are solved. NALA ($40M Series A), LemFi ($1B+/month in volume), Flutterwave, Chipper Cash — they move money across borders cheaply and fast. Nobody has solved what happens after the money lands. That's the gap.

## The Solution

EyeOnSite is a two-sided marketplace that connects:

**Demand side (clients):** Diaspora investors, enterprise clients (fintechs, insurers, NGOs) who need physical verification of assets, projects, or operations in African cities.

**Supply side (inspectors):** Verified local agents — engineering students, surveyors, gig workers — who complete geo-verified inspection tasks using the EyeOnSite app.

**The mechanics:**
1. Client posts a task (location, instructions, checklist, budget)
2. Funds go into escrow (Stripe for diaspora, Flutterwave for local)
3. Nearby inspectors get pinged (Bolt/Uber-style geo-matching)
4. Inspector accepts, travels to site, captures evidence in-app (GPS-stamped photos, structured checklist, optional video)
5. Evidence auto-validated (GPS match, EXIF consistency, AI quality check)
6. Client reviews evidence package (48hr window)
7. Client accepts → escrow released to inspector. Both parties rate each other.
8. Client disputes → platform mediation → resolution within 7 days

## Competitive Landscape

### Managed Services (Africa-focused, not marketplace model)

| Company | Market | Model | Limitation |
|---------|--------|-------|------------|
| **LandSafe** | Nigeria (Abuja, Lagos, Ogun) | Property verification via vetted lawyers + AI. 24–48hr reports. | Managed service, not scalable marketplace. Nigeria only. |
| **Mantel Verify** | Nigeria (diaspora focus) | Premium independent verification: tech + legal + on-ground. | High-touch, high-cost. Not a platform. |
| **LANDIZ** | Nigeria | Diaspora team, remote verification, escrow, diaspora mortgages. | Vertically integrated (mortgage-first). Not on-demand. |
| **The Diaspora Access** | Kenya | Property management, land searches, inspections for overseas clients. | Manual, relationship-based. No tech platform. |

### Gig Marketplace (US-focused, not Africa)

| Company | Network | Model | Limitation |
|---------|---------|-------|------------|
| **WeGoLook** (Crawford) | 45,000+ "Lookers" | On-demand inspections for insurance/property claims. Founded 2009, acquired 2016. Raised $3.35M. | US/Canada/UK only. Enterprise insurance focus. No diaspora play. |
| **iVueit** | 30,000+ "Vuers" | Commercial property/facilities verification. | US only. Retail/commercial focus. Low per-task pay ($5–$20). |
| **ProxyPics** | 180,000+ data collectors | Property photography for appraisers/insurers. LiDAR, 3D, 360°. $22–$125/task. | US only. Real estate/lending vertical. |

### The White Space

No one is running the Uber/Bolt marketplace model for physical verification in African markets. Every African player is a managed service. Every marketplace player is US-focused. EyeOnSite sits at the intersection: marketplace mechanics + African markets + diaspora demand.

```
                    MARKETPLACE MODEL
                         ▲
                         │
         ┌───────────────┤
         │               │
    WeGoLook        ★ EyeOnSite ★
    iVueit          (the white space)
    ProxyPics            │
         │               │
    ─────┼───────────────┼─────────────►
    US/WESTERN      AFRICAN MARKETS
         │               │
         │          LandSafe
         │          Mantel Verify
         │          LANDIZ
         │          Diaspora Access
         │               │
         └───────────────┤
                         │
                         ▼
                  MANAGED SERVICE
```

## Lessons from Bolt/Uber

EyeOnSite borrows core marketplace mechanics but adapts for verification:

| Bolt/Uber Pattern | EyeOnSite Adaptation |
|-------------------|---------------------|
| Driver geo-matching | Inspector geo-matching within X km of task site |
| Real-time ride tracking | Real-time task status (en route → on site → capturing → submitted) |
| Fare estimation | Task budget estimation based on vertical, complexity, urgency |
| Driver ratings | Inspector Trust Score (completion rate, evidence quality, client rating, dispute rate) |
| Surge pricing | Urgency pricing (same-day = 1.5x, 4-hour = 2x) |
| Payment hold until ride complete | Escrow hold until evidence accepted |
| Driver onboarding (license, background check) | Inspector onboarding (ID, background check, training, test task) |
| Ride dispute resolution | Evidence dispute resolution with structured mediation |

**Key difference:** A ride either happened or it didn't. Inspection quality is subjective. This is why structured templates, mandatory photo angles, and objective checklists are critical — they turn subjective "was this a good inspection?" into verifiable "were all 12 checklist items completed with required photos?"

## Why Now

1. **KMP is production-ready.** JetBrains declared stable Nov 2023. Google co-investing since 2022. A single developer can ship Android + iOS + Web in one language.
2. **Mobile money is ubiquitous.** MTN MoMo (Ghana), M-Pesa (Kenya), bank transfers (Nigeria) — inspector payouts are frictionless.
3. **Diaspora digital communities are targetable.** Facebook groups, WhatsApp communities, church networks — concentrated, reachable, high-trust.
4. **Post-COVID normalization of remote oversight.** "I don't need to fly home to check" became acceptable.
5. **Africa's fintech lending explosion needs field verification.** Nigeria has 40M MSMEs. Ethiopia disbursed 50B+ ETB in uncollateralized digital loans in 2.5 years. Every loan benefits from physical verification.
6. **Africa's insurance market is growing.** $98.5B in 2025, projected $166B by 2034. Claims verification is a bottleneck.

## Why Us

1. **Maku is physically in Accra** — can be Inspector #1, build local relationships, iterate in real-time
2. **MEST Africa alumnus** — instant credibility and network in Ghana's tech ecosystem
3. **Senior Android/KMP developer at Farmerline** — builds mobile products for African markets professionally, understands offline-first, low-connectivity environments
4. **Manti Labs LLC (Delaware via Stripe Atlas)** — corporate structure ready to receive diaspora payments
5. **Ugandan + Ghanaian roots** — dual diaspora corridor access (UK/US → Ghana AND UK/US → Uganda)
6. **Technical stack mastery** — KMP, Firebase, Stripe, Flutterwave are proven tools in Maku's hands
7. **StartupTribunal audience** — built-in distribution for launch story

## Vision: V0 → V5

**V0:** You are the inspector. Manual. WhatsApp + Google Forms. Validate demand.
**V1:** MVP KMP app. One city (Accra). One vertical (construction). Prove the loop works.
**V2:** Multi-city, multi-vertical. Prove replication.
**V3:** Enterprise B2B + API. Prove enterprise revenue. Fundraise.
**V4:** Pan-African scale. Smart evidence. Data product.
**V5:** EyeOnSite becomes infrastructure — the trust verification layer for Africa.

---

*See [EOS-09-ROADMAP.md](./EOS-09-ROADMAP.md) for detailed version specs with success metrics and kill criteria.*
*See [EOS-13-SOURCES-AND-GLOSSARY.md](./EOS-13-SOURCES-AND-GLOSSARY.md) for primary sources on all cited data.*
