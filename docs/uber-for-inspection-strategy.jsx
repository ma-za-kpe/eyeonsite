import { useState } from "react";

const verticals = [
  {
    id: "construction",
    name: "Diaspora Construction & Housing",
    icon: "🏗️",
    painScore: 10,
    marketSize: "$4-6B+",
    priority: "P0 — Launch Vertical",
    markets: ["Accra", "Lagos/Abuja", "Nairobi", "Kampala"],
    pain: [
      "56% of diaspora investors cite corruption as the #1 barrier to investing back home (Commonwealth Secretariat survey)",
      "In Nigeria, an estate agent collected ₦21M from 81 different people for the SAME apartment",
      "Lagos State recovered ₦478.13M and 18 properties from real estate fraud in 2024 alone",
      "A Kenyan diaspora buyer paid Sh4.25M for a maisonette that was never built (Mizizi Africa Homes)",
      "In Zimbabwe, a construction company received ~$1M for a mansion, warehouse and mall — then diverted the funds",
      "Developers in Kenya forge Land Registration numbers, create clean paperwork, then target diaspora buyers via YouTube ads",
      "70% of real estate purchases in Lagos were made by diaspora Nigerians in 2021 — mostly sight unseen",
      "No mandatory escrow accounts in Kenya means developers control all funds with zero accountability",
    ],
    useCases: [
      "Construction progress monitoring (weekly/bi-weekly photo + video reports)",
      "Pre-purchase land/property verification",
      "Title document cross-referencing with land registries",
      "Post-construction handover inspections",
      "Occupancy and condition checks for rental properties",
      "Materials delivery confirmation",
    ],
    whyFirst:
      "Highest emotional urgency. Diaspora members are literally losing life savings. The pain is visceral, personal, and recurring. Ghana alone receives $4.7B in annual remittances, with housing being a top use case. 135,000+ Ghanaians in the UK alone, 600,000 families in Ghana depend on remittances.",
  },
  {
    id: "realestate",
    name: "Real Estate & Property Management",
    icon: "🏠",
    painScore: 9,
    marketSize: "$2-4B",
    priority: "P1 — Adjacent Expansion",
    markets: ["Lagos", "Accra", "Nairobi", "Kampala", "Dar es Salaam"],
    pain: [
      "Title fraud is endemic — forged Certificates of Occupancy are common across Nigeria, Ghana, Kenya",
      "Off-plan developments have become a goldmine for fraudsters due to poor regulation",
      "Diaspora landlords can't verify tenant behavior, property condition, or rent collection",
      "Property management companies in Kenya charge 8-12% of monthly rent but accountability is low",
      "No central database to track developers' histories or warn potential buyers",
    ],
    useCases: [
      "Pre-purchase due diligence inspections",
      "Tenant verification and occupancy checks",
      "Rental property condition monitoring",
      "Post-renovation quality verification",
      "Boundary and encroachment verification",
    ],
    whyFirst:
      "Natural extension of construction vertical. Same inspectors, same diaspora clients, ongoing revenue (monthly property checks vs one-time construction monitoring).",
  },
  {
    id: "agriculture",
    name: "Agriculture & Farm Monitoring",
    icon: "🌾",
    painScore: 8,
    marketSize: "$1-3B",
    priority: "P1 — Adjacent Expansion",
    markets: [
      "Kenya (tea, coffee, horticulture)",
      "Ghana (cocoa)",
      "Uganda (coffee, tea)",
      "Nigeria (various)",
    ],
    pain: [
      "Diaspora-funded farms and orchards are frequently misused or neglected by local managers",
      "Supply chain opacity for agricultural exporters — no way to verify what's happening on the ground",
      "Loan defaults on agricultural lending due to unverifiable crop progress",
      "Climate disruptions create urgent need for real-time field verification",
      "Harvest theft and misreporting are rampant — diaspora investors can't verify yields",
    ],
    useCases: [
      "Crop progress photo/video reports at key growth stages",
      "Soil and irrigation system checks",
      "Harvest verification and yield documentation",
      "Input delivery confirmation (seeds, fertilizer, equipment)",
      "Livestock headcount and condition verification",
      "Farm infrastructure condition reports",
    ],
    whyFirst:
      "Strong overlap with construction inspectors in rural areas. Kenya and Uganda have massive agricultural diaspora investment. Farmerline connection gives you insider knowledge.",
  },
  {
    id: "fintech",
    name: "Fintech / MSME Lending Verification",
    icon: "🏦",
    painScore: 8,
    marketSize: "$500M-1.5B",
    priority: "P2 — Enterprise B2B",
    markets: ["Nigeria", "Kenya", "Ghana", "Tanzania", "Ethiopia"],
    pain: [
      "Nigeria alone has 40 million MSMEs — most with no credit history or formal collateral",
      "Fintech lenders can't physically verify collateral, business activity, or borrower legitimacy in informal economies",
      "High NPL (non-performing loan) rates because digital scoring alone misses physical reality",
      "Banks report 70+ hours per SME credit assessment — physical verification is the bottleneck",
      "Predatory lending has emerged where lenders skip verification entirely, leading to defaults and regulatory crackdowns",
      "Most fintech lenders rely on the assumption that good clients subsidize defaulting ones — driving up rates for everyone",
    ],
    useCases: [
      "Asset and collateral verification for MSME loans",
      "Merchant/business activity confirmation",
      "Inventory and stock level checks",
      "Warehouse and storage facility inspections",
      "Business premises verification (does this shop actually exist?)",
      "Post-disbursement loan utilization checks",
    ],
    whyFirst:
      "B2B revenue — fintechs and banks pay reliably, at volume, and on contract. This is your WeGoLook equivalent for Africa. Ethiopia alone has disbursed over 50 billion ETB (~$421M) in uncollateralized digital MSME loans in just 2.5 years — all needing verification.",
  },
  {
    id: "insurance",
    name: "Insurance Claims & Risk Assessment",
    icon: "🛡️",
    painScore: 7,
    marketSize: "$98.5B market (2025), growing to $166B by 2034",
    priority: "P2 — Enterprise B2B",
    markets: ["South Africa", "Kenya", "Nigeria", "Ghana", "Tanzania"],
    pain: [
      "Africa's insurance market hit $98.5B in 2025, growing at 5.79% CAGR — but penetration is still very low",
      "Fraudulent claims are a massive drain — AI fraud detection boosted accuracy by 35% in South Africa but physical verification still needed",
      "Slow claims processing in remote areas — traditional adjusters take days/weeks",
      "Growing crop insurance demand (parametric + traditional) needs ground-truth verification",
      "Regulators are pushing for faster claims processing timelines and more robust handling",
    ],
    useCases: [
      "On-site property damage assessment (vehicles, buildings)",
      "Crop damage verification for agricultural insurance",
      "Health facility condition assessments",
      "Post-disaster rapid assessment (flooding, fire)",
      "Risk assessment for new policy underwriting",
      "Claims photo/video evidence collection",
    ],
    whyFirst:
      "Mirrors the WeGoLook model exactly. Enterprise contracts, recurring revenue, high margins. But requires inspector certification and training — not a day-one vertical.",
  },
  {
    id: "ngo",
    name: "NGO / Development Project Monitoring",
    icon: "🌍",
    painScore: 7,
    marketSize: "$500M-1B",
    priority: "P2 — Impact Vertical",
    markets: ["Pan-African", "Uganda", "Kenya", "Nigeria", "Ghana"],
    pain: [
      "USAID OIG investigated a $4.7M project finding bribery, conflicts of interest, and falsified program records",
      "NGO accountability is a top challenge — donors can't verify if schools, wells, and clinics were actually built",
      "Diaspora church groups and hometown associations collectively fund community projects with zero oversight",
      "Uganda's NGO sector formed a dedicated Internal Risk Management Group of 66 organizations specifically to combat fraud",
      "GAO found the US African Development Foundation lacked strategic fraud prevention approaches",
      "Donor skepticism is growing — 'Where did my money actually go?' is the universal question",
    ],
    useCases: [
      "Community project progress verification (schools, wells, clinics, roads)",
      "Donor fund utilization confirmation",
      "Impact assessment photo/video documentation",
      "Disaster relief distribution verification",
      "Community development milestone tracking",
      "Accountability reporting for hometown associations",
    ],
    whyFirst:
      "Emotionally compelling, strong diaspora tie-in (churches, hometown associations). But payment reliability is lower than enterprise verticals. Good for brand building and social proof.",
  },
  {
    id: "supplychain",
    name: "Supply Chain & Logistics Audits",
    icon: "📦",
    painScore: 6,
    marketSize: "$300M-800M",
    priority: "P3 — Future Expansion",
    markets: ["Nigeria", "Kenya", "Ghana", "South Africa"],
    pain: [
      "Theft, delays, and quality issues in last-mile delivery across Africa's e-commerce boom",
      "Import/export quality verification is expensive and slow",
      "Warehouse stock levels are frequently misreported",
      "E-commerce returns fraud — items swapped or damaged before return",
    ],
    useCases: [
      "Warehouse and stock level audits",
      "Delivery confirmation with photo evidence",
      "Quality inspection of received goods",
      "Cold chain compliance checks",
      "Import/export cargo verification",
    ],
    whyFirst:
      "Enterprise vertical with recurring revenue. But requires specialized inspector training and competes with existing logistics providers' internal audit teams.",
  },
  {
    id: "solar",
    name: "Solar & Off-Grid Energy Installations",
    icon: "☀️",
    painScore: 6,
    marketSize: "$200M-500M",
    priority: "P3 — Future Expansion",
    markets: ["Kenya", "Nigeria", "Ghana", "Uganda", "Tanzania"],
    pain: [
      "Off-grid solar installations are booming but quality verification is absent",
      "Pay-as-you-go solar companies need field verification of installations",
      "Donor-funded energy projects need progress monitoring",
      "Rural installations are hard to audit — inspectors travel hours for one check",
    ],
    useCases: [
      "Installation quality verification",
      "Equipment condition monitoring",
      "Post-installation performance checks",
      "Donor project progress reporting",
    ],
    whyFirst:
      "Niche but growing fast. Good alignment with rural inspector networks built for agriculture vertical.",
  },
];

const businessModel = {
  primaryRevenue: [
    {
      name: "Task Commission (Take Rate)",
      rate: "15-20%",
      description:
        "On every completed inspection task. Charged to the client (diaspora user or enterprise). Industry benchmark: gig platforms charge 5-20% to clients, 3-5% to workers. Higher rate justified by trust/escrow premium.",
      example:
        "Inspection task costs $50 → Platform takes $7.50-$10 → Inspector receives $40-$42.50",
    },
    {
      name: "Escrow Fee",
      rate: "2-3%",
      description:
        "Processing fee on escrow hold/release. Standard in marketplace transactions. Covers payment processing + fraud reserve.",
      example:
        "$500 construction milestone escrow → $10-$15 escrow fee",
    },
    {
      name: "Subscription Plans (Enterprise B2B)",
      rate: "$99-$999/month",
      description:
        "For fintech lenders, insurance companies, property management firms, NGOs that need regular inspections at volume. Includes API access, bulk task creation, dedicated inspector pools, SLA guarantees.",
      example:
        "Fintech lender running 200 collateral checks/month → $499/month + reduced per-task rate",
    },
  ],
  secondaryRevenue: [
    {
      name: "Premium Inspector Tiers",
      description:
        "Verified professionals (engineers, surveyors, licensed inspectors) command higher task rates. Platform takes larger commission on premium tasks.",
    },
    {
      name: "Data & Reports API",
      description:
        "Anonymized, geo-tagged verification data sold to real estate platforms, insurance underwriters, credit bureaus, development agencies. This is the long-term moat.",
    },
    {
      name: "Urgency Pricing",
      description:
        "Rush inspections (same-day, 4-hour) at 1.5-2x standard rate. Diaspora users often have urgent needs when deals are closing.",
    },
    {
      name: "Inspector Certification & Training",
      description:
        "Charge for inspector upskilling programs (property assessment, construction progress evaluation). Creates stickier supply side.",
    },
  ],
  unitEconomics: {
    avgTaskValue: "$30-$80 (consumer), $50-$150 (enterprise)",
    takeRate: "15-20%",
    avgRevenuePerTask: "$6-$30",
    inspectorPayoutPerTask: "$20-$60",
    escrowProcessingCost: "~2.5% (Stripe/Flutterwave)",
    customerAcquisitionCost: "$15-$40 (diaspora digital marketing)",
    targetGrossMargin: "60-70% at scale",
  },
};

const bootstrapPhases = [
  {
    phase: "Phase 0: Validation (Weeks 1-4)",
    city: "Accra, Ghana",
    corridor: "UK → Ghana (135,000+ Ghanaians in UK)",
    tasks: [
      "Manually run 10-15 inspection tasks yourself — you're the first inspector",
      "Source clients from: UK-Ghana WhatsApp groups, Ghanaian churches in London, MEST alumni network",
      "Document everything: photos, task flows, pain points, what clients actually ask for",
      "Validate willingness to pay: charge $25-50 per task, see who bites",
      "Build relationships with 5-10 local engineers/surveyors in Accra for supply side",
    ],
    metrics: ["Willingness to pay (conversion rate)", "Task completion satisfaction (NPS)", "Repeat request rate"],
    budget: "$500-$1,000 (your time + transport + marketing)",
  },
  {
    phase: "Phase 1: MVP Launch (Months 2-4)",
    city: "Accra → Kumasi expansion",
    corridor: "UK + US → Ghana ($4.7B annual remittances)",
    tasks: [
      "KMP app: basic task creation, GPS-stamped photo capture, in-app messaging, simple escrow (Stripe + MTN MoMo)",
      "Recruit 20-50 inspectors: university engineering students, gig workers, surveying graduates",
      "Inspector onboarding: background check, training video, test task",
      "Diaspora marketing: targeted Facebook/Instagram ads to Ghanaians in UK/US ($500-1K/month)",
      "Content flywheel: 'How we verified a $50K building project in Tema' — real case studies",
      "Partner with 1-2 remittance apps (NALA, LemFi) for referral integration",
      "WhatsApp group for first 50 diaspora clients — manual support, gather feedback",
    ],
    metrics: [
      "50-100 completed tasks",
      "< 10% dispute rate",
      "60%+ repeat rate within 90 days",
      "Inspector retention > 70%",
      "NPS > 50",
    ],
    budget: "$3,000-$8,000 (marketing + inspector incentives + infrastructure)",
  },
  {
    phase: "Phase 2: PMF & City #2 (Months 5-9)",
    city: "Accra + Lagos (Nigeria) or Nairobi (Kenya)",
    corridor: "Multi-corridor diaspora",
    tasks: [
      "If Accra metrics hit targets → replicate playbook in Lagos or Nairobi",
      "Add enterprise pilot: partner with 1 fintech lender for collateral verification",
      "Add agriculture vertical in parallel (Ghana cocoa country, Kenya coffee highlands)",
      "Build inspector tier system: Basic (photo tasks) → Professional (engineering background) → Expert (licensed surveyor)",
      "Implement automated fraud detection: GPS verification, EXIF validation, AI photo analysis",
      "Structured reporting templates per vertical",
    ],
    metrics: [
      "500+ completed tasks across cities",
      "1 enterprise contract signed",
      "Monthly revenue > $5K",
      "Unit economics positive (revenue per task > fully-loaded cost)",
    ],
    budget: "$10,000-$25,000 (team + multi-city operations + enterprise sales)",
  },
  {
    phase: "Phase 3: Scale & Fundraise (Months 10-18)",
    city: "3-5 cities across West & East Africa",
    corridor: "Pan-African diaspora + enterprise B2B",
    tasks: [
      "Raise pre-seed/seed ($200K-$500K) on traction data from Phase 2",
      "Full KMP app: Android + iOS + Web dashboard for enterprise clients",
      "Add insurance and NGO verticals",
      "API for enterprise integrations (fintechs, insurance companies, property platforms)",
      "Inspector community: leaderboards, bonuses, certification programs",
      "Data product: anonymized verification data layer for African markets",
    ],
    metrics: [
      "5,000+ completed tasks",
      "10+ enterprise contracts",
      "Monthly revenue > $30K",
      "3+ verticals active",
      "Inspector network > 500 across 5 cities",
    ],
    budget: "Seed funding dependent",
  },
];

const accraCaseStudy = {
  why: [
    "$4.7B in annual remittances to Ghana, with housing as a top use case",
    "135,000+ Ghanaians in the UK alone; ~500K across OECD countries",
    "600,000 families in Ghana depend on remittances — many for construction projects",
    "Ghanaian migrants in the UK deliberately live in the lowest end of the housing market to maximize savings for construction projects back home",
    "No mandatory escrow for developers in Ghana — funds are unprotected",
    "Strong diaspora community infrastructure (churches, associations) for marketing",
    "You're physically in Accra — you can be the first inspector and build relationships",
    "MEST alumni network gives you instant credibility and connections",
  ],
  whyNotLagos:
    "Lagos is a bigger market ($19.8B remittances to Nigeria) but far more competitive — LandSafe, Mantel Verify, LANDIZ are already there. Accra lets you iterate in a less contested market before attacking Nigeria. Also, regulatory complexity in Nigeria (money transmission laws) is higher.",
  whyNotNairobi:
    "Kenya ($4.94B remittances) is strong but the fraud ecosystem is more sophisticated and the competitive landscape includes The Diaspora Access. Start where you have home advantage.",
};

export default function StrategyDashboard() {
  const [activeTab, setActiveTab] = useState("verticals");
  const [expandedVertical, setExpandedVertical] = useState("construction");
  const [expandedPhase, setExpandedPhase] = useState(0);

  const tabs = [
    { id: "verticals", label: "Verticals & Pain Points" },
    { id: "bootstrap", label: "Bootstrap Playbook" },
    { id: "business", label: "Business Model" },
    { id: "accra", label: "Why Accra First" },
  ];

  return (
    <div style={{
      fontFamily: "'IBM Plex Sans', 'Segoe UI', system-ui, sans-serif",
      background: "var(--background, #0a0a0a)",
      color: "var(--text-primary, #e8e8e8)",
      minHeight: "100vh",
      padding: "20px",
      maxWidth: "900px",
      margin: "0 auto",
    }}>
      <div style={{
        borderBottom: "2px solid var(--border-color, #2a2a2a)",
        paddingBottom: "16px",
        marginBottom: "24px",
      }}>
        <div style={{
          fontSize: "11px",
          textTransform: "uppercase",
          letterSpacing: "3px",
          color: "var(--text-secondary, #888)",
          marginBottom: "8px",
        }}>
          Manti Labs Research Brief — May 2026
        </div>
        <h1 style={{
          fontSize: "28px",
          fontWeight: 700,
          margin: "0 0 6px 0",
          color: "var(--text-primary, #f0f0f0)",
          lineHeight: 1.2,
        }}>
          Uber for Inspection: Africa Strategy
        </h1>
        <p style={{
          fontSize: "14px",
          color: "var(--text-secondary, #999)",
          margin: 0,
          lineHeight: 1.5,
        }}>
          Research-backed vertical analysis, business model, and go-to-market playbook for a diaspora trust verification marketplace across West & East Africa
        </p>
      </div>

      {/* Tabs */}
      <div style={{
        display: "flex",
        gap: "4px",
        marginBottom: "24px",
        flexWrap: "wrap",
      }}>
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            style={{
              padding: "8px 16px",
              border: activeTab === tab.id ? "1px solid var(--accent-color, #f59e0b)" : "1px solid var(--border-color, #333)",
              background: activeTab === tab.id ? "var(--accent-color, #f59e0b)" : "transparent",
              color: activeTab === tab.id ? "#000" : "var(--text-secondary, #aaa)",
              borderRadius: "4px",
              cursor: "pointer",
              fontSize: "13px",
              fontWeight: activeTab === tab.id ? 600 : 400,
              transition: "all 0.15s ease",
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* VERTICALS TAB */}
      {activeTab === "verticals" && (
        <div>
          <div style={{
            background: "var(--surface, #141414)",
            border: "1px solid var(--border-color, #2a2a2a)",
            borderRadius: "6px",
            padding: "16px",
            marginBottom: "20px",
            fontSize: "13px",
            lineHeight: 1.6,
            color: "var(--text-secondary, #bbb)",
          }}>
            <strong style={{ color: "var(--text-primary, #e8e8e8)" }}>The Core Thesis:</strong> Africa receives ~$100B in annual remittances (growing 10% CAGR). Nigeria alone gets $19.8B, Ghana $4.7B, Kenya $4.94B. The money flows — NALA, LemFi, Flutterwave solved that. <strong style={{ color: "var(--accent-color, #f59e0b)" }}>Nobody has solved what happens after the money lands.</strong> Every vertical below represents a trust gap where someone sends money and can't verify the outcome.
          </div>

          {verticals.map((v) => (
            <div
              key={v.id}
              style={{
                border: expandedVertical === v.id
                  ? "1px solid var(--accent-color, #f59e0b)"
                  : "1px solid var(--border-color, #2a2a2a)",
                borderRadius: "6px",
                marginBottom: "8px",
                overflow: "hidden",
                transition: "border-color 0.15s ease",
              }}
            >
              <button
                onClick={() => setExpandedVertical(expandedVertical === v.id ? null : v.id)}
                style={{
                  width: "100%",
                  padding: "14px 16px",
                  background: expandedVertical === v.id ? "var(--surface, #1a1a1a)" : "var(--surface, #111)",
                  border: "none",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: "12px",
                  color: "var(--text-primary, #e8e8e8)",
                  textAlign: "left",
                }}
              >
                <span style={{ fontSize: "20px" }}>{v.icon}</span>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: "14px", fontWeight: 600 }}>{v.name}</div>
                  <div style={{ fontSize: "11px", color: "var(--text-secondary, #888)", marginTop: "2px" }}>
                    {v.priority} · Market: {v.marketSize}
                  </div>
                </div>
                <div style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "4px",
                  fontSize: "11px",
                  color: "var(--text-secondary, #888)",
                }}>
                  <span>Pain</span>
                  <div style={{
                    width: "60px",
                    height: "6px",
                    background: "var(--border-color, #333)",
                    borderRadius: "3px",
                    overflow: "hidden",
                  }}>
                    <div style={{
                      width: `${v.painScore * 10}%`,
                      height: "100%",
                      background: v.painScore >= 9 ? "#ef4444" : v.painScore >= 7 ? "#f59e0b" : "#22c55e",
                      borderRadius: "3px",
                    }} />
                  </div>
                  <span>{v.painScore}/10</span>
                </div>
                <span style={{
                  transform: expandedVertical === v.id ? "rotate(180deg)" : "rotate(0deg)",
                  transition: "transform 0.15s ease",
                  fontSize: "12px",
                  color: "var(--text-secondary, #666)",
                }}>▼</span>
              </button>

              {expandedVertical === v.id && (
                <div style={{
                  padding: "16px",
                  background: "var(--surface, #0f0f0f)",
                  borderTop: "1px solid var(--border-color, #2a2a2a)",
                }}>
                  <div style={{ marginBottom: "16px" }}>
                    <div style={{
                      fontSize: "11px",
                      textTransform: "uppercase",
                      letterSpacing: "1.5px",
                      color: "#ef4444",
                      marginBottom: "8px",
                      fontWeight: 600,
                    }}>
                      Research-Backed Pain Points
                    </div>
                    {v.pain.map((p, i) => (
                      <div key={i} style={{
                        fontSize: "13px",
                        lineHeight: 1.6,
                        color: "var(--text-secondary, #bbb)",
                        padding: "6px 0 6px 16px",
                        borderLeft: "2px solid var(--border-color, #333)",
                        marginBottom: "4px",
                      }}>
                        {p}
                      </div>
                    ))}
                  </div>

                  <div style={{ marginBottom: "16px" }}>
                    <div style={{
                      fontSize: "11px",
                      textTransform: "uppercase",
                      letterSpacing: "1.5px",
                      color: "var(--accent-color, #f59e0b)",
                      marginBottom: "8px",
                      fontWeight: 600,
                    }}>
                      Use Cases
                    </div>
                    <div style={{
                      display: "flex",
                      flexWrap: "wrap",
                      gap: "6px",
                    }}>
                      {v.useCases.map((uc, i) => (
                        <span key={i} style={{
                          fontSize: "12px",
                          padding: "4px 10px",
                          background: "var(--border-color, #1a1a1a)",
                          border: "1px solid var(--border-color, #2a2a2a)",
                          borderRadius: "3px",
                          color: "var(--text-secondary, #ccc)",
                        }}>
                          {uc}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div style={{ marginBottom: "12px" }}>
                    <div style={{
                      fontSize: "11px",
                      textTransform: "uppercase",
                      letterSpacing: "1.5px",
                      color: "var(--text-secondary, #888)",
                      marginBottom: "6px",
                      fontWeight: 600,
                    }}>
                      Key Markets
                    </div>
                    <div style={{ fontSize: "13px", color: "var(--text-secondary, #bbb)" }}>
                      {v.markets.join(" → ")}
                    </div>
                  </div>

                  <div style={{
                    fontSize: "13px",
                    lineHeight: 1.6,
                    color: "var(--text-primary, #e8e8e8)",
                    padding: "12px",
                    background: "var(--border-color, #1a1a1a)",
                    borderRadius: "4px",
                    borderLeft: "3px solid var(--accent-color, #f59e0b)",
                  }}>
                    <strong>Strategic Rationale:</strong> {v.whyFirst}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* BOOTSTRAP TAB */}
      {activeTab === "bootstrap" && (
        <div>
          <div style={{
            background: "var(--surface, #141414)",
            border: "1px solid var(--border-color, #2a2a2a)",
            borderRadius: "6px",
            padding: "16px",
            marginBottom: "20px",
            fontSize: "13px",
            lineHeight: 1.6,
            color: "var(--text-secondary, #bbb)",
          }}>
            <strong style={{ color: "var(--text-primary, #e8e8e8)" }}>WeGoLook's Playbook:</strong> Started in one US metro, one vertical (insurance claims), bootstrapped with enterprise demand that pays reliably, expanded "Lookers" organically. Now part of Crawford & Company with 45,000+ inspectors. <strong style={{ color: "var(--accent-color, #f59e0b)" }}>Your adaptation: one city (Accra), one vertical (diaspora construction), bootstrapped with diaspora demand via community marketing.</strong>
          </div>

          {bootstrapPhases.map((p, i) => (
            <div
              key={i}
              style={{
                border: expandedPhase === i
                  ? "1px solid var(--accent-color, #f59e0b)"
                  : "1px solid var(--border-color, #2a2a2a)",
                borderRadius: "6px",
                marginBottom: "8px",
                overflow: "hidden",
              }}
            >
              <button
                onClick={() => setExpandedPhase(expandedPhase === i ? null : i)}
                style={{
                  width: "100%",
                  padding: "14px 16px",
                  background: expandedPhase === i ? "var(--surface, #1a1a1a)" : "var(--surface, #111)",
                  border: "none",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: "12px",
                  color: "var(--text-primary, #e8e8e8)",
                  textAlign: "left",
                }}
              >
                <div style={{
                  width: "28px",
                  height: "28px",
                  borderRadius: "50%",
                  background: expandedPhase === i ? "var(--accent-color, #f59e0b)" : "var(--border-color, #333)",
                  color: expandedPhase === i ? "#000" : "var(--text-secondary, #888)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "12px",
                  fontWeight: 700,
                  flexShrink: 0,
                }}>
                  {i}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: "14px", fontWeight: 600 }}>{p.phase}</div>
                  <div style={{ fontSize: "11px", color: "var(--text-secondary, #888)", marginTop: "2px" }}>
                    {p.city} · Corridor: {p.corridor}
                  </div>
                </div>
                <div style={{
                  fontSize: "11px",
                  color: "var(--accent-color, #f59e0b)",
                  fontWeight: 600,
                }}>
                  {p.budget}
                </div>
              </button>

              {expandedPhase === i && (
                <div style={{
                  padding: "16px",
                  background: "var(--surface, #0f0f0f)",
                  borderTop: "1px solid var(--border-color, #2a2a2a)",
                }}>
                  <div style={{ marginBottom: "16px" }}>
                    <div style={{
                      fontSize: "11px",
                      textTransform: "uppercase",
                      letterSpacing: "1.5px",
                      color: "var(--accent-color, #f59e0b)",
                      marginBottom: "8px",
                      fontWeight: 600,
                    }}>
                      Execution Tasks
                    </div>
                    {p.tasks.map((t, j) => (
                      <div key={j} style={{
                        fontSize: "13px",
                        lineHeight: 1.6,
                        color: "var(--text-secondary, #bbb)",
                        padding: "6px 0 6px 16px",
                        borderLeft: "2px solid var(--border-color, #333)",
                        marginBottom: "4px",
                      }}>
                        {t}
                      </div>
                    ))}
                  </div>

                  <div>
                    <div style={{
                      fontSize: "11px",
                      textTransform: "uppercase",
                      letterSpacing: "1.5px",
                      color: "#22c55e",
                      marginBottom: "8px",
                      fontWeight: 600,
                    }}>
                      Success Metrics
                    </div>
                    <div style={{
                      display: "flex",
                      flexWrap: "wrap",
                      gap: "6px",
                    }}>
                      {p.metrics.map((m, j) => (
                        <span key={j} style={{
                          fontSize: "12px",
                          padding: "4px 10px",
                          background: "rgba(34, 197, 94, 0.1)",
                          border: "1px solid rgba(34, 197, 94, 0.3)",
                          borderRadius: "3px",
                          color: "#22c55e",
                        }}>
                          {m}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}

          <div style={{
            marginTop: "16px",
            padding: "16px",
            background: "var(--surface, #141414)",
            border: "1px solid var(--border-color, #2a2a2a)",
            borderRadius: "6px",
            fontSize: "13px",
            lineHeight: 1.6,
            color: "var(--text-secondary, #bbb)",
          }}>
            <strong style={{ color: "var(--text-primary, #e8e8e8)" }}>Anti-Leakage Strategy (Your #1 Risk):</strong> Once a diaspora client finds a good inspector, they'll want to go direct. Combat this with: (1) escrow protection — no platform, no money safety guarantee, (2) structured evidence reports with timestamps that serve as legal documentation, (3) dispute resolution — platform mediates conflicts, (4) inspector insurance/background verification — trust badge only valid on-platform, (5) ongoing task management (recurring inspections, multi-milestone projects) that are painful to coordinate off-platform.
          </div>
        </div>
      )}

      {/* BUSINESS MODEL TAB */}
      {activeTab === "business" && (
        <div>
          <div style={{
            fontSize: "11px",
            textTransform: "uppercase",
            letterSpacing: "1.5px",
            color: "var(--accent-color, #f59e0b)",
            marginBottom: "12px",
            fontWeight: 600,
          }}>
            Primary Revenue Streams
          </div>

          {businessModel.primaryRevenue.map((r, i) => (
            <div key={i} style={{
              border: "1px solid var(--border-color, #2a2a2a)",
              borderRadius: "6px",
              padding: "16px",
              marginBottom: "8px",
              background: "var(--surface, #111)",
            }}>
              <div style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "8px",
              }}>
                <span style={{ fontSize: "14px", fontWeight: 600, color: "var(--text-primary, #e8e8e8)" }}>
                  {r.name}
                </span>
                <span style={{
                  fontSize: "14px",
                  fontWeight: 700,
                  color: "var(--accent-color, #f59e0b)",
                }}>
                  {r.rate}
                </span>
              </div>
              <div style={{ fontSize: "13px", color: "var(--text-secondary, #bbb)", lineHeight: 1.5, marginBottom: "8px" }}>
                {r.description}
              </div>
              <div style={{
                fontSize: "12px",
                color: "var(--text-secondary, #888)",
                padding: "8px 12px",
                background: "var(--border-color, #1a1a1a)",
                borderRadius: "4px",
                fontStyle: "italic",
              }}>
                Example: {r.example}
              </div>
            </div>
          ))}

          <div style={{
            fontSize: "11px",
            textTransform: "uppercase",
            letterSpacing: "1.5px",
            color: "var(--text-secondary, #888)",
            marginBottom: "12px",
            marginTop: "24px",
            fontWeight: 600,
          }}>
            Secondary Revenue Streams
          </div>

          <div style={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr",
            gap: "8px",
            marginBottom: "24px",
          }}>
            {businessModel.secondaryRevenue.map((r, i) => (
              <div key={i} style={{
                border: "1px solid var(--border-color, #2a2a2a)",
                borderRadius: "6px",
                padding: "12px",
                background: "var(--surface, #111)",
              }}>
                <div style={{ fontSize: "13px", fontWeight: 600, color: "var(--text-primary, #e8e8e8)", marginBottom: "4px" }}>
                  {r.name}
                </div>
                <div style={{ fontSize: "12px", color: "var(--text-secondary, #999)", lineHeight: 1.5 }}>
                  {r.description}
                </div>
              </div>
            ))}
          </div>

          <div style={{
            fontSize: "11px",
            textTransform: "uppercase",
            letterSpacing: "1.5px",
            color: "#22c55e",
            marginBottom: "12px",
            fontWeight: 600,
          }}>
            Target Unit Economics
          </div>

          <div style={{
            border: "1px solid var(--border-color, #2a2a2a)",
            borderRadius: "6px",
            padding: "16px",
            background: "var(--surface, #111)",
          }}>
            {Object.entries(businessModel.unitEconomics).map(([key, value]) => (
              <div key={key} style={{
                display: "flex",
                justifyContent: "space-between",
                padding: "8px 0",
                borderBottom: "1px solid var(--border-color, #1a1a1a)",
                fontSize: "13px",
              }}>
                <span style={{ color: "var(--text-secondary, #999)" }}>
                  {key.replace(/([A-Z])/g, " $1").replace(/^./, (s) => s.toUpperCase())}
                </span>
                <span style={{ color: "var(--text-primary, #e8e8e8)", fontWeight: 600 }}>{value}</span>
              </div>
            ))}
          </div>

          <div style={{
            marginTop: "16px",
            padding: "16px",
            background: "var(--surface, #141414)",
            border: "1px solid var(--border-color, #2a2a2a)",
            borderRadius: "6px",
            borderLeft: "3px solid var(--accent-color, #f59e0b)",
            fontSize: "13px",
            lineHeight: 1.6,
            color: "var(--text-secondary, #bbb)",
          }}>
            <strong style={{ color: "var(--text-primary, #e8e8e8)" }}>Revenue Model Comparison:</strong> African gig platforms (e.g., Efiko) charge 5% to clients + 3% withdrawal fee. Global inspection platforms like ProxyPics pay $22-$50 per task (client pays $40-$100). WeGoLook operates on enterprise contracts with fixed-rate per inspection. <strong style={{ color: "var(--accent-color, #f59e0b)" }}>Your hybrid model: consumer marketplace take rate (15-20%) for diaspora + enterprise subscriptions for B2B verticals (fintech, insurance). This dual revenue structure makes the business more resilient.</strong>
          </div>
        </div>
      )}

      {/* ACCRA TAB */}
      {activeTab === "accra" && (
        <div>
          <div style={{
            fontSize: "11px",
            textTransform: "uppercase",
            letterSpacing: "1.5px",
            color: "var(--accent-color, #f59e0b)",
            marginBottom: "12px",
            fontWeight: 600,
          }}>
            Why Accra + UK→Ghana is the Right Launch Corridor
          </div>

          {accraCaseStudy.why.map((reason, i) => (
            <div key={i} style={{
              fontSize: "13px",
              lineHeight: 1.6,
              color: "var(--text-secondary, #bbb)",
              padding: "10px 16px",
              borderLeft: "2px solid var(--accent-color, #f59e0b)",
              marginBottom: "6px",
              background: "var(--surface, #111)",
              borderRadius: "0 4px 4px 0",
            }}>
              {reason}
            </div>
          ))}

          <div style={{ marginTop: "24px" }}>
            <div style={{
              fontSize: "11px",
              textTransform: "uppercase",
              letterSpacing: "1.5px",
              color: "#ef4444",
              marginBottom: "12px",
              fontWeight: 600,
            }}>
              Why NOT Start in Lagos
            </div>
            <div style={{
              fontSize: "13px",
              lineHeight: 1.6,
              color: "var(--text-secondary, #bbb)",
              padding: "12px 16px",
              border: "1px solid rgba(239, 68, 68, 0.3)",
              borderRadius: "6px",
              background: "rgba(239, 68, 68, 0.05)",
            }}>
              {accraCaseStudy.whyNotLagos}
            </div>
          </div>

          <div style={{ marginTop: "16px" }}>
            <div style={{
              fontSize: "11px",
              textTransform: "uppercase",
              letterSpacing: "1.5px",
              color: "#ef4444",
              marginBottom: "12px",
              fontWeight: 600,
            }}>
              Why NOT Start in Nairobi
            </div>
            <div style={{
              fontSize: "13px",
              lineHeight: 1.6,
              color: "var(--text-secondary, #bbb)",
              padding: "12px 16px",
              border: "1px solid rgba(239, 68, 68, 0.3)",
              borderRadius: "6px",
              background: "rgba(239, 68, 68, 0.05)",
            }}>
              {accraCaseStudy.whyNotNairobi}
            </div>
          </div>

          <div style={{
            marginTop: "24px",
            padding: "16px",
            background: "var(--surface, #141414)",
            border: "1px solid var(--border-color, #2a2a2a)",
            borderRadius: "6px",
            borderLeft: "3px solid var(--accent-color, #f59e0b)",
          }}>
            <div style={{
              fontSize: "11px",
              textTransform: "uppercase",
              letterSpacing: "1.5px",
              color: "var(--accent-color, #f59e0b)",
              marginBottom: "10px",
              fontWeight: 600,
            }}>
              The Unfair Advantage Stack
            </div>
            <div style={{ fontSize: "13px", lineHeight: 1.8, color: "var(--text-secondary, #bbb)" }}>
              <div>→ You're physically in Accra — you ARE the first inspector</div>
              <div>→ MEST alumni network — instant credibility + first 10 clients</div>
              <div>→ Farmerline experience — you understand building for African markets, mobile-first, offline-first</div>
              <div>→ Manti Labs LLC (Delaware) — ready to receive diaspora payments via Stripe</div>
              <div>→ KMP expertise — you can ship Android + iOS + Web from day one</div>
              <div>→ Ugandan + Ghanaian roots — dual diaspora corridor access (UK/US → Ghana AND UK/US → Uganda)</div>
              <div>→ StartupTribunal audience — built-in distribution for launch story</div>
            </div>
          </div>

          <div style={{
            marginTop: "16px",
            padding: "16px",
            background: "var(--surface, #141414)",
            border: "1px solid var(--border-color, #2a2a2a)",
            borderRadius: "6px",
          }}>
            <div style={{
              fontSize: "11px",
              textTransform: "uppercase",
              letterSpacing: "1.5px",
              color: "#22c55e",
              marginBottom: "10px",
              fontWeight: 600,
            }}>
              Week 1 Action Items
            </div>
            <div style={{ fontSize: "13px", lineHeight: 1.8, color: "var(--text-secondary, #bbb)" }}>
              <div>1. Join 5 UK-Ghana diaspora Facebook/WhatsApp groups — start conversations about construction oversight pain</div>
              <div>2. Identify 3 Ghanaian churches in London with active building projects in Ghana</div>
              <div>3. Interview 5 people who've lost money on a Ghana construction project — document the stories</div>
              <div>4. Register the domain and set up a simple landing page with a waitlist</div>
              <div>5. Do your first "inspection" — pick a construction site in Accra, document it with GPS-stamped photos, write the report. This is your first case study.</div>
            </div>
          </div>
        </div>
      )}

      <div style={{
        marginTop: "32px",
        paddingTop: "16px",
        borderTop: "1px solid var(--border-color, #2a2a2a)",
        fontSize: "11px",
        color: "var(--text-secondary, #666)",
        textAlign: "center",
      }}>
        Sources: World Bank Remittance Data, Commonwealth Secretariat Diaspora Survey, OECD Ghana Migration Review, IMARC Group Insurance Market Report, EFCC Nigeria, Kenya Daily Nation, GAO USADF Report, TechCrunch, Afridigest, ISS Africa Futures. All data points cited from research conducted May 2026.
      </div>
    </div>
  );
}
