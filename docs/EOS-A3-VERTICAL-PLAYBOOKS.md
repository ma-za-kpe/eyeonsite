# A3 — Vertical Playbooks

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Purpose

Detailed playbook for each vertical EyeOnSite serves. Every vertical has different clients, different evidence requirements, different inspectors, and different economics.

---

## P0: Construction / Housing (V1 Launch Vertical)

### Why First
- Highest emotional stakes — diaspora life savings at risk
- Recurring need — monthly/bi-weekly progress checks throughout build (12–24 months)
- Clear evidence criteria — construction stages are visible and photographable
- Personal experience — Maku has direct network in Accra construction scene
- Largest individual transaction values ($10K–$100K+ builds)

### Client Profile
- Diaspora individual building a house in home country
- Typical budget: $20K–$100K total construction, willing to pay $30–$60/month for verification
- Decision driver: fear of contractor fraud, desire for peace of mind
- Purchase cycle: finds EyeOnSite when construction starts OR after first trust breakdown

### Task Types
| Task | Frequency | Budget | Inspector Tier |
|------|-----------|--------|---------------|
| Initial site verification ("does my plot exist?") | One-time | $30–$50 | Basic |
| Foundation stage check | One-time | $30–$40 | Basic |
| Monthly progress monitoring | Monthly | $25–$40 | Basic → Professional |
| Materials verification ("are they using quality cement/rebar?") | Ad-hoc | $40–$60 | Professional |
| Milestone completion (lintel, roof, finishing) | Per milestone | $40–$60 | Professional |
| Final completion / handover inspection | One-time | $80–$150 | Expert |

### Evidence Requirements
- Minimum 8 photos: N/S/E/W exterior, 2x interior, 1x materials, 1x site overview
- GPS within 200m of declared construction site
- Checklist: construction stage, worker presence, materials on site, quality assessment, percentage complete, defects, security
- Optional: 2-minute walkthrough video
- Photo of any visible permits or building plans displayed at site

### Inspector Requirements
- Basic tier sufficient for most tasks
- Professional tier for materials quality assessment and milestone completion
- Expert tier for final completion / structural assessment
- Construction knowledge: basic understanding of building stages (foundation → blockwork → lintel → roofing → plastering → finishing)
- Training module: 20-minute construction inspection course (photo examples of each stage, what to look for, common red flags)

### Economics
```
Average task budget:        GHS 45 ($3.50)
Average tasks per project:  12 (monthly × 12 months average build)
Average LTV per client:     $42 (12 × $3.50)
Platform revenue per client: $7.56 (18% × $42)
Gross margin:               ~75%
```

### Growth Levers
- Partner with diaspora construction consultants (they recommend EyeOnSite to clients)
- Content marketing: "10 signs your contractor is cutting corners" → SEO + social
- Referral: clients recommend to friends also building
- Seasonal: peak construction (dry season Oct–Mar in Ghana) → targeted campaigns

---

## P1: Real Estate / Property Verification

### Why P1
- Natural extension from construction — same clients, same diaspora corridor
- One-time high-value tasks (property purchases are $10K–$500K)
- Land fraud is epidemic (documented extensively in EOS-01)

### Client Profile
- Diaspora individual buying property (land, house, apartment) sight unseen
- Typical willingness to pay: $40–$100 for pre-purchase verification
- Decision driver: fear of title fraud, fear of buying disputed land, need to see before buying
- Purchase cycle: finds EyeOnSite during property search, before committing money

### Task Types
| Task | Frequency | Budget | Inspector Tier |
|------|-----------|--------|---------------|
| Land existence verification ("is this plot real?") | One-time | $30–$50 | Basic |
| Property condition assessment | One-time | $40–$70 | Professional |
| Boundary marker verification | One-time | $50–$80 | Professional |
| Neighborhood assessment (surroundings, infrastructure, access roads) | One-time | $30–$50 | Basic |
| Title/document cross-reference (physical + legal check) | One-time | $80–$150 | Expert |
| Rental property condition check (for absentee landlords) | Quarterly | $30–$50 | Basic |

### Evidence Requirements
- Minimum 10 photos: 4-side exterior, room-by-room interior (if accessible), boundary markers, signage, street view
- GPS within 100m (tighter for property — exact location matters)
- Checklist: property type, occupancy, condition (1–5 scale), boundary markers, encroachment, address match, damage, maintenance
- Document photos: any visible title boards, for-sale signs, agent contact details
- Neighboring properties: photos of adjacent structures (encroachment risk)

### Market Entry (V2)
- Trigger: V1 construction clients ask about property verification (organic demand signal)
- Launch: add property template to Accra (same inspector pool, new training module)
- Expand: Lagos (70% diaspora property purchases) as priority city for this vertical

---

## P1: Agriculture / Farm Monitoring

### Why P1
- Diaspora investment in farming is significant (especially cocoa, cassava, rice in Ghana)
- Farmerline connection — Maku's professional expertise and network
- Recurring need — seasonal crop monitoring
- Agri-lending is booming — fintechs need field verification for farm loans

### Client Profiles

**Consumer:** Diaspora investor with a farm back home (cocoa farm in Kumasi, rice paddies in Northern Region). Needs seasonal verification that the farm is active and the manager isn't stealing proceeds.

**Enterprise:** Agri-fintech (Farmerline-adjacent companies) that lends to smallholder farmers and needs collateral verification, crop monitoring, input delivery confirmation.

### Task Types
| Task | Frequency | Budget | Inspector Tier |
|------|-----------|--------|---------------|
| Farm existence verification | One-time | $30–$50 | Basic |
| Crop stage monitoring | Seasonal (3–4x/year) | $25–$40 | Basic |
| Irrigation/infrastructure check | Quarterly | $30–$50 | Professional |
| Harvest assessment (pre-harvest yield estimate) | One-time | $50–$80 | Professional |
| Input delivery confirmation (fertilizer, seeds arrived?) | Ad-hoc | $20–$30 | Basic |
| Post-harvest storage verification | One-time | $30–$50 | Basic |

### Evidence Requirements
- Minimum 6 photos: crop panorama, close-up crop health, irrigation system, storage, input materials, boundary
- GPS critical — farms are in rural areas, exact coordinates matter for large plots
- Checklist: crop type, growth stage, area under cultivation, irrigation status, pest/disease, input materials, worker count, crop health (1–5)
- Geo-challenge: rural areas have weaker GPS signals and connectivity. Offline-first architecture is critical here.

### Special Considerations
- Rural connectivity: inspectors may be offline for hours. Evidence capture must work 100% offline.
- Travel distance: farms are often 30–60 min from nearest town. Adjust inspector payout upward for rural tasks.
- Seasonal timing: tasks cluster around planting (Mar–Apr), growing (Jun–Aug), and harvest (Oct–Dec) in Ghana.
- Inspector profile differs: agriculture students, agricultural extension officers, farm managers — not urban gig workers.

---

## P2: Fintech / MSME Lending Verification

### Why P2
- Enterprise revenue — fintechs pay per-API-call, not per-task
- Massive scale potential — Nigeria has 40M MSMEs, Kenya and Ghana have millions more
- Solves a real pain: lenders lose money on fabricated collateral and fake businesses

### Client Profile
- Fintech lender disbursing MSME loans ($100–$10,000 per loan)
- Needs: verify business exists, collateral is real, loan applicant isn't fabricating
- Volume: 50–500+ verification tasks/month per lender
- Willingness to pay: $3–$10 per verification (must be cheaper than sending own loan officer)

### Task Types
| Task | Frequency | Budget | Inspector Tier |
|------|-----------|--------|---------------|
| Business existence verification | Per loan application | $5–$15 | Basic |
| Collateral verification (equipment, inventory) | Per loan application | $10–$25 | Basic |
| Owner/manager presence confirmation | Per loan application | $5–$10 | Basic |
| Post-disbursement asset check | Monthly | $5–$15 | Basic |
| Defaulter asset location | Ad-hoc | $15–$30 | Professional |

### Integration Model (V3 API)
```
Fintech system → EyeOnSite API
  POST /v1/tasks
  {
    "vertical": "FINTECH",
    "template": "business_verification",
    "location": { "lat": 5.614, "lng": -0.186 },
    "address": "123 Oxford Street, Osu, Accra",
    "instructions": "Verify business exists at address. Check signage, inventory, operations.",
    "budget": { "amount": 1000, "currency": "GHS" },
    "deadline": "2026-06-15T17:00:00Z",
    "callback_url": "https://kreditpay.com/webhooks/eyeonsite"
  }

  → EyeOnSite matches inspector, completes task
  → Webhook to fintech:
  POST callback_url
  {
    "task_id": "...",
    "status": "COMPLETED",
    "evidence_url": "https://api.eyeonsite.com/v1/evidence/...",
    "quality_score": 0.85,
    "fraud_score": 0.05,
    "checklist_summary": { ... }
  }
```

### Economics
```
Enterprise task budget:     GHS 15 ($1.15) — lower per-task, higher volume
Platform take:              15% = GHS 2.25
Inspector payout:           GHS 12.75
Volume per enterprise client: 100–500 tasks/month
Monthly revenue per client: $115–$575
```

---

## P2: Insurance Claims Verification

### Why P2
- Africa's insurance market: $98.5B (2025), growing to $166B by 2034
- Claims verification is a bottleneck — insurers send adjusters, which is slow and expensive
- WeGoLook proved this model in the US (acquired for this capability)

### Client Profile
- Insurance company processing claims (motor, property, health facility)
- Needs: independent field verification that claimed damage/loss is real
- Volume: varies widely, 20–200+ claims/month
- Willingness to pay: $15–$50 per verification (cheaper than sending an adjuster)

### Task Types
| Task | Budget | Inspector Tier |
|------|--------|---------------|
| Motor accident scene documentation | $20–$40 | Basic |
| Property damage assessment (fire, flood, storm) | $30–$60 | Professional |
| Health facility existence verification | $15–$25 | Basic |
| Inventory/asset damage documentation | $25–$50 | Professional |
| Post-repair verification | $20–$40 | Basic |

### Market Entry
- Target: V3 (Month 12+)
- Requires: Expert-tier inspectors with insurance knowledge
- Partnership approach: pilot with 1–2 insurers (Hollard Ghana, Jubilee Insurance Kenya)

---

## P2: NGO / Development Verification

### Why P2
- NGOs and development organizations need field monitoring but can't be everywhere
- Project verification, beneficiary counting, facility condition checks
- Steady, predictable task volume from institutional clients

### Client Profile
- International NGO (UNICEF, World Vision, CARE, GIZ) or bilateral donor
- Government ministry monitoring development projects
- Needs: independent verification of project implementation, not self-reported data

### Task Types
| Task | Budget | Inspector Tier |
|------|--------|---------------|
| School/clinic construction progress | $30–$60 | Professional |
| Water point functionality check | $20–$40 | Basic |
| Beneficiary activity verification | $15–$30 | Basic |
| Post-distribution monitoring (supplies delivered?) | $15–$25 | Basic |
| Facility condition assessment | $30–$50 | Professional |

### Market Entry
- Target: V3+ (alongside enterprise API)
- Long sales cycle — NGO procurement is slow (3–6 months)
- Partnership with one NGO as proof of concept → use case study to sell to others
- Pricing: subscription model ($299–$999/month) more likely than per-task

---

## P3: Supply Chain Verification

### Entry: V4+
- Verify goods at warehouses, ports, distribution points
- Client: logistics companies, importers, commodity traders
- Requires: specialized inspector knowledge (commodity grading, container inspection)

## P3: Solar / Energy Installation

### Entry: V4+
- Verify solar panel installations for diaspora investors and energy finance companies
- Growing market: off-grid solar in Africa is $1.75B+ market
- Requires: technical knowledge (solar system sizing, installation quality)

---

## Vertical Template Architecture

All templates share a common structure with vertical-specific configuration:

```kotlin
data class VerticalTemplate(
    val id: String,
    val verticalType: VerticalType,
    val name: String,
    val description: String,
    val version: Int,
    val requiredPhotos: Int,
    val requiredPhotoAngles: List<PhotoAngle>,
    val videoRequired: Boolean,
    val videoDurationSeconds: Int,
    val checklist: List<ChecklistItem>,
    val inspectorTierRequired: InspectorTier,
    val estimatedDurationMinutes: Int,
    val suggestedBudget: Money,
    val trainingModuleId: String,      // inspector must complete this training
    val reportTemplateId: String,       // for PDF report generation
    val fraudDetectionConfig: FraudConfig, // vertical-specific fraud thresholds
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
```

Templates are stored in Firestore (`templates/` collection), publicly readable. New templates can be added without app updates — the app dynamically renders checklists and photo requirements from the template definition.

---

*See [EOS-05-MANIFEST-SPECS.md](./EOS-05-MANIFEST-SPECS.md) for checklist data structures.*
*See [EOS-02-PRODUCT-DESIGN.md](./EOS-02-PRODUCT-DESIGN.md) for task lifecycle across verticals.*
