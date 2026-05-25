package com.maku.eyeonsite.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun TermsPage() {
    LegalPage(title = "Terms of Service", lastUpdated = "May 2026") {
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            LegalSection("Overview") {
                LegalBody(
                    "These Terms of Service govern your use of EyeOnSite, an on-demand " +
                        "verification marketplace operated by Manti Labs LLC (Delaware, USA). " +
                        "By joining our waitlist or using the platform, you agree to these terms. " +
                        "If you do not agree, do not use the service.",
                )
            }

            LegalSection("1. Platform Description") {
                LegalBody(
                    "EyeOnSite connects diaspora investors ('clients') who need on-site " +
                        "verification of properties, construction projects, farmland, and " +
                        "business collateral in African cities, with trained local inspectors " +
                        "who physically visit sites and capture GPS-verified photographic evidence. " +
                        "Think of it as Uber for property verification — escrow-backed, " +
                        "GPS-stamped, and fraud-resistant.",
                )
            }

            LegalSection("2. Eligibility") {
                LegalBulletList(
                    listOf(
                        "You must be at least 18 years old",
                        "You must provide accurate registration information",
                        "You must not be prohibited from using the service under any applicable law",
                        "Inspector applicants must complete identity verification and onboarding before accepting tasks",
                    ),
                )
            }

            LegalSection("3. Client Terms") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegalSubheading("Task Posting")
                    LegalBody(
                        "Clients post verification tasks by describing the site, location, and " +
                            "required evidence (e.g. construction progress photos, occupancy " +
                            "confirmation). Tasks must be lawful — no requests to verify illegal " +
                            "activities or trespass on protected land.",
                    )
                    LegalSubheading("Escrow & Payment")
                    LegalBody(
                        "Clients fund an escrow before an inspector is matched. Funds are held by " +
                            "Stripe (Manti Labs LLC is the merchant of record). Escrow is released " +
                            "only when the client accepts the submitted evidence, or automatically " +
                            "released after 48 hours if no dispute is raised.",
                    )
                    LegalSubheading("Evidence Review")
                    LegalBody(
                        "Clients have 48 hours after evidence submission to accept, request a " +
                            "revision, or raise a dispute. Constructive feedback must be provided " +
                            "for revision requests. Repeated groundless disputes may result in " +
                            "account suspension.",
                    )
                }
            }

            LegalSection("4. Inspector Terms") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegalSubheading("Independent Contractor Status")
                    LegalBody(
                        "Inspectors are independent contractors, not employees of Manti Labs LLC. " +
                            "You control your schedule, accept or decline tasks, and use your own " +
                            "device and transport. EyeOnSite does not set your hours or require exclusivity.",
                    )
                    LegalSubheading("Evidence Capture Requirements")
                    LegalBulletList(
                        listOf(
                            "All evidence must be captured using the EyeOnSite in-app camera — no gallery uploads, ever",
                            "You must physically be at the task site (geofence enforced — within 200m)",
                            "GPS coordinates, timestamp, and device sensor data are recorded at every photo",
                            "Evidence must match the task checklist items provided by the client",
                            "Submitting fraudulent, staged, or recycled photos is grounds for permanent ban and legal action",
                        ),
                    )
                    LegalSubheading("Payouts")
                    LegalBody(
                        "Inspectors are paid via Flutterwave — MTN MoMo, M-Pesa, or bank " +
                            "transfer. Payouts are processed within 24 hours of escrow release. " +
                            "EyeOnSite retains a platform fee (disclosed at task acceptance). " +
                            "Payouts may be withheld pending fraud investigation.",
                    )
                }
            }

            LegalSection("5. Prohibited Activities") {
                LegalBulletList(
                    listOf(
                        "Submitting fake, staged, recycled, or AI-generated evidence",
                        "Inspector-client collusion to misrepresent site conditions",
                        "GPS spoofing, mock location use, or any location manipulation",
                        "Uploading evidence from camera rolls or external sources",
                        "Creating multiple accounts to circumvent bans or verification",
                        "Harassing other users, inspectors, or site occupants",
                        "Using EyeOnSite to facilitate illegal property transactions",
                        "Attempting to manipulate or circumvent the escrow system",
                    ),
                )
            }

            LegalSection("6. Dispute Resolution") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegalBody(
                        "Disputes are reviewed by the EyeOnSite team within 48 hours. We examine " +
                            "GPS data, photo metadata, submission timestamps, and task history. " +
                            "Our decision is final for disputes under \$500 equivalent. For larger " +
                            "disputes, parties may escalate to binding arbitration under Delaware " +
                            "law. Frivolous disputes reduce your client trust score.",
                    )
                }
            }

            LegalSection("7. Platform Fees") {
                LegalBody(
                    "EyeOnSite charges a platform fee on each completed task, deducted from the " +
                        "escrow before inspector payout. The current fee schedule is displayed at " +
                        "task creation and task acceptance. Fees are non-refundable on successfully " +
                        "completed and accepted tasks.",
                )
            }

            LegalSection("8. Intellectual Property") {
                LegalBody(
                    "Evidence photos captured through EyeOnSite remain owned by the client who " +
                        "commissioned the task. Inspectors grant EyeOnSite a non-exclusive licence " +
                        "to use anonymised evidence data to improve fraud detection. The EyeOnSite " +
                        "brand, platform, and underlying technology are owned by Manti Labs LLC.",
                )
            }

            LegalSection("9. Limitation of Liability") {
                LegalBody(
                    "EyeOnSite is a marketplace platform — we connect clients and inspectors but " +
                        "are not responsible for the underlying asset, investment decision, or " +
                        "site conditions. We are not liable for indirect, incidental, or " +
                        "consequential damages. Our total liability to any party is limited to " +
                        "the fees paid on the specific task in dispute.",
                )
            }

            LegalSection("10. Termination") {
                LegalBody(
                    "We may suspend or terminate accounts for violations of these Terms, fraudulent " +
                        "activity, or inactivity (12+ months). You may close your account at any " +
                        "time by emailing makpalyy@gmail.com. Termination does not affect pending " +
                        "tasks or escrow balances.",
                )
            }

            LegalSection("11. Governing Law") {
                LegalBody(
                    "These Terms are governed by the laws of the State of Delaware, USA. " +
                        "Any disputes not resolved through our internal process are subject to " +
                        "binding arbitration in Wilmington, Delaware, under JAMS rules. " +
                        "Nothing in these Terms limits your statutory consumer rights.",
                )
            }

            LegalSection("12. Changes to These Terms") {
                LegalBody(
                    "We may update these Terms from time to time. Material changes will be " +
                        "communicated by email (waitlist members) or in-app notification (active " +
                        "users) at least 14 days before taking effect. Continued use of the " +
                        "platform after the effective date constitutes acceptance.",
                )
            }

            LegalSection("13. Contact") {
                LegalBody(
                    "Manti Labs LLC · Wilmington, Delaware, USA\n" +
                        "Operator: Maku Mazakpe\n" +
                        "Email: makpalyy@gmail.com",
                )
            }
        }
    }
}
