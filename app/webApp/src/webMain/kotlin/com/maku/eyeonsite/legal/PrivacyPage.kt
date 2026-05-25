package com.maku.eyeonsite.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPage() {
    LegalPage(title = "Privacy Policy", lastUpdated = "May 2026") {
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            LegalSection("Overview") {
                LegalBody(
                    "Manti Labs LLC ('we', 'EyeOnSite') operates an on-demand verification " +
                        "marketplace connecting diaspora investors with verified local inspectors " +
                        "in African cities. This Privacy Policy explains what data we collect, " +
                        "why we collect it, and how you can exercise your rights under the Ghana " +
                        "Data Protection Act 2012 (Act 843), UK GDPR, Nigeria NDPA 2023, " +
                        "Kenya Data Protection Act 2019, and related laws.",
                )
            }

            LegalSection("1. Information We Collect") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegalSubheading("Waitlist Registration")
                    LegalBody(
                        "Full name, email address, your role (diaspora client or field agent), " +
                            "location (country and city), and an optional message. Used solely to " +
                            "notify you at launch and understand geographic demand.",
                    )
                    LegalSubheading("Account Information (at app launch)")
                    LegalBody(
                        "Phone number, email address, government-issued ID number (inspectors only, " +
                            "for identity verification), profile photo, and account preferences.",
                    )
                    LegalSubheading("Usage & Device Data")
                    LegalBody(
                        "Device type, operating system, browser, screen resolution, session " +
                            "duration, pages visited, and button interactions. Collected via " +
                            "Firebase Analytics (Google).",
                    )
                    LegalSubheading("Location Data (inspectors only)")
                    LegalBody(
                        "GPS coordinates captured at the moment of each evidence photo. Location " +
                            "is never tracked passively in the background — only during active " +
                            "inspection with the in-app camera. Multi-source signals (GPS, network, " +
                            "WiFi, cell tower) are used for fraud prevention.",
                    )
                    LegalSubheading("Payment Data")
                    LegalBody(
                        "We do not store card numbers or banking details. Client payments are " +
                            "processed by Stripe; inspector payouts are processed by Flutterwave " +
                            "(MTN MoMo, M-Pesa, bank transfer). Their respective privacy policies apply.",
                    )
                }
            }

            LegalSection("2. How We Use Your Information") {
                LegalBulletList(
                    listOf(
                        "To operate and deliver the on-demand verification service",
                        "To match inspectors to tasks based on location and availability",
                        "To authenticate your identity and prevent fraud",
                        "To send service notifications (task updates, payment confirmations)",
                        "To analyse usage and improve product features",
                        "To comply with applicable data protection and anti-fraud regulations",
                        "We never sell your personal data to third parties for advertising",
                    ),
                )
            }

            LegalSection("3. Data Sharing") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegalBody("We share your data only with service providers essential to operating the platform:")
                    LegalKeyValue(
                        listOf(
                            "Google Firebase" to "Authentication, database, file storage, analytics, cloud functions",
                            "Stripe" to "Payment processing and escrow for diaspora clients",
                            "Flutterwave" to "Inspector payouts via MTN MoMo, M-Pesa, and bank transfer",
                            "Cloud Vision API" to "AI photo quality and fraud detection on evidence images",
                        ),
                    )
                    LegalBody(
                        "All third parties are contractually required to protect your data and " +
                            "may not use it for their own marketing purposes.",
                    )
                }
            }

            LegalSection("4. Data Retention") {
                LegalBulletList(
                    listOf(
                        "Waitlist data: until you request deletion, or 24 months from collection",
                        "Account data: while your account is active and 7 years after closure (financial compliance)",
                        "Evidence photos and reports: duration of the task plus 3 years",
                        "Analytics data: aggregated and anonymised after 14 months (Google Analytics default)",
                    ),
                )
            }

            LegalSection("5. Your Rights") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegalBody(
                        "Depending on your location, you have the following rights regarding your personal data:",
                    )
                    LegalBulletList(
                        listOf(
                            "Access — request a copy of the data we hold about you",
                            "Rectification — correct inaccurate or incomplete data",
                            "Erasure — request deletion ('right to be forgotten')",
                            "Portability — receive your data in a machine-readable format",
                            "Objection — object to specific types of processing",
                            "Withdrawal of consent — at any time, where processing is consent-based",
                        ),
                    )
                    LegalBody("To exercise any right, email makpalyy@gmail.com. We respond within 30 days.")
                }
            }

            LegalSection("6. Cookies & Analytics") {
                LegalBody(
                    "Our web platform uses Google Analytics 4 (GA4) for anonymised usage data. " +
                        "We use session cookies for authentication. You can opt out of analytics " +
                        "via the Google Analytics Opt-out browser extension or by declining cookies " +
                        "in your browser settings.",
                )
            }

            LegalSection("7. International Data Transfers") {
                LegalBody(
                    "EyeOnSite is operated by Manti Labs LLC (Delaware, USA). Data is processed " +
                        "on Google Cloud infrastructure primarily in the US and nearest GCP regions. " +
                        "Transfers from the UK or EU are covered by Standard Contractual Clauses. " +
                        "Transfers involving Ghanaian data comply with Ghana DPA 2012 cross-border " +
                        "provisions. Nigerian user data is processed with NDPA 2023 lawful basis.",
                )
            }

            LegalSection("8. Security") {
                LegalBody(
                    "We implement industry-standard security: Firebase Authentication with phone " +
                        "OTP, Firestore Security Rules enforcing row-level access, TLS in transit, " +
                        "Google-managed encryption at rest, and least-privilege IAM for all " +
                        "internal service accounts. Evidence photos are stored in private Cloud " +
                        "Storage buckets accessible only via time-limited signed URLs.",
                )
            }

            LegalSection("9. Children") {
                LegalBody(
                    "EyeOnSite is not directed at children under 18. We do not knowingly collect " +
                        "data from minors. If you believe a minor has provided us data, contact " +
                        "makpalyy@gmail.com and we will delete it promptly.",
                )
            }

            LegalSection("10. Contact") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegalBody(
                        "Data Controller: Manti Labs LLC, registered in Delaware, USA.\n" +
                            "Operator: Maku Mazakpe\n" +
                            "Email: makpalyy@gmail.com",
                    )
                    LegalBody(
                        "For complaints you may also contact: the Data Protection Commission " +
                            "of Ghana (dataprotection.org.gh), the UK ICO (ico.org.uk), or the " +
                            "Nigeria Data Protection Commission (ndpc.gov.ng).",
                    )
                }
            }
        }
    }
}
