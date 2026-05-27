# EyeOnSite — Document Index

**Manti Labs** · May 2026 · v0.1-DRAFT

> On-demand verification marketplace for Africa. Bolt for physical inspection.

---

## Core Documents

| # | Document | Description |
|---|----------|------------|
| **00** | [Document Index](./EOS-00-DOCUMENT-INDEX.md) | This file. Table of contents and reading guide. |
| **01** | [Vision and Market Context](./EOS-01-VISION-AND-MARKET.md) | Why this exists. The $100B remittance trust gap. Competitive landscape. Why now, why us. |
| **02** | [Product Design](./EOS-02-PRODUCT-DESIGN.md) | Core concepts, personas, task lifecycle, evidence model, trust graph, app flows, dispute flow. |
| **03** | [Architecture](./EOS-03-ARCHITECTURE.md) | Five-plane system design. KMP structure. Firestore collections. Offline-first sync. Event model. Trust boundaries. |
| **04** | [Technical Stack](./EOS-04-TECHNICAL-STACK.md) | Every technology choice with rationale. Boring-stack principle. Dependency graph. Build and deploy commands. |
| **05** | [Manifest Specifications](./EOS-05-MANIFEST-SPECS.md) | All Kotlin data classes. Enums. Task manifest. Evidence package. Inspector profile. Escrow transaction. Vertical templates. Firestore security rules. |
| **06** | [Inspector Specification](./EOS-06-INSPECTOR-SPEC.md) | Onboarding flow. Tier system. Payout model. Fraud detection pipeline. Deactivation criteria. Safety protocols. |
| **07** | [Security and Compliance](./EOS-07-SECURITY-AND-COMPLIANCE.md) | Three-layer trust model. Top 10 threat model. Evidence forensics pipeline. Per-country regulatory map. Firebase security rules (complete). Incident response. |
| **08** | [Escrow and Payments](./EOS-08-ESCROW-AND-PAYMENTS.md) | Dual payment rails (Stripe + Flutterwave). End-to-end money flow. Fee structure. Multi-currency handling. Double-entry ledger. Reconciliation. |
| **09** | [Roadmap](./EOS-09-ROADMAP.md) | V0 through V5. Scope, success metrics, and kill criteria for each version. |
| **10** | [Business Model](./EOS-10-BUSINESS-MODEL.md) | Revenue streams per version. Unit economics. Revenue projections. CAC and anti-disintermediation. Sensitivity analysis. Break-even. |
| **11** | [Execution Plan](./EOS-11-EXECUTION-PLAN.md) | Week-by-week V0–V1 plan. Sprint breakdown. Hiring triggers. Budget allocation. Personal transition planning. |
| **12** | [Risks and Mitigations](./EOS-12-RISKS-AND-MITIGATIONS.md) | 20+ risks scored by probability and impact. Marketplace, operational, technical, business, reputational. Heat map. Top 5 to monitor. |
| **13** | [Sources and Glossary](./EOS-13-SOURCES-AND-GLOSSARY.md) | Every cited data point with source and confidence level. Platform, technical, business, and regulatory glossary. |

## Appendices

| # | Document | Description |
|---|----------|------------|
| **A1** | [Competitive Intelligence](./EOS-A1-COMPETITIVE-INTEL.md) | Deep profiles: LandSafe, Mantel Verify, LANDIZ, Diaspora Access, WeGoLook, iVueit, ProxyPics, NALA, LemFi. Positioning matrix. |
| **A2** | [City Launch Playbook](./EOS-A2-CITY-LAUNCH-PLAYBOOK.md) | Repeatable 8-week playbook. City scoring criteria. City-specific adaptations (Accra, Lagos, Nairobi). Launch metrics dashboard. |
| **A3** | [Vertical Playbooks](./EOS-A3-VERTICAL-PLAYBOOKS.md) | Per-vertical deep dives: construction (P0), property (P1), agriculture (P1), fintech (P2), insurance (P2), NGO (P2), supply chain (P3), solar (P3). Task types, evidence requirements, economics. |
| **A4** | [Dispute Resolution](./EOS-A4-DISPUTE-RESOLUTION.md) | Three-tier system (auto/mediation/arbitration). Dispute categories. Escrow actions. Client abuse detection. Inspector protections. Communication templates. |
| **A5** | [Matching Algorithm](./EOS-A5-MATCHING-ALGORITHM.md) | V1 simple geo-distance → V3 scored matching → V4 optimization engine. Ping mechanics. Edge cases. SLAs. Monitoring. |
| **A6** | [CLAUDE.md](./EOS-A6-CLAUDE-MD.md) | The complete CLAUDE.md for the monorepo. Copy to `eyeonsite/CLAUDE.md` at repo initialization. |
| **A7** | [Decision Log](./EOS-A7-DECISION-LOG.md) | Every architecture and product decision with reasoning, trade-offs, and revisit triggers. 10 founding decisions + rejected ideas. |
| **A8** | [Integration Mock Strategy](./EOS-A8-INTEGRATION-MOCK-STRATEGY.md) | How external services (Stripe, Flutterwave, Cloud Vision) are mocked during development and testing without leaking keys. |
| **A9** | [CI/CD and Distribution](./EOS-A9-CICD-AND-DISTRIBUTION.md) | GitHub Actions workflows, Android product flavors, Firebase App Distribution setup, secrets reference, and pre-commit pipeline. |

---

## Reading Guide

**Starting from zero?** Read in order: 01 → 02 → 09 → 10 → 11.

**Joining as a developer?** Read: A6 (CLAUDE.md) → 03 → 04 → 05 → 07.

**Investor / advisor?** Read: 01 → 10 → 09 → 12 → A1.

**Launching a new city?** Read: A2 → A3 → 06 → 11.

**Handling a dispute?** Read: A4 → 02 (dispute flow section) → 08 (escrow actions).

**Understanding the matching engine?** Read: A5 → 06 → 03 (event model).

---

## Document Stats

| Metric | Value |
|--------|-------|
| Total documents | 21 |
| Core documents (00–13) | 14 |
| Appendices (A1–A7) | 7 |
| Version | v0.1-DRAFT |
| Last updated | May 2026 |
| Author | Maku Mazakpe (Manti Labs) |
| Generated with | Claude (Anthropic) |

---

*All documents are living. Update after every sprint, every city launch, every major decision, and every quarter.*
