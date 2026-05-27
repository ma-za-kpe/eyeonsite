package com.maku.eyeonsite.domain

data class TrainingModule(
    val icon: String,
    val title: String,
    val body: String,
)

object OnboardingContent {
    val trainingModules: List<TrainingModule> =
        listOf(
            TrainingModule(
                icon = "🌍",
                title = "What is EyeOnSite?",
                body =
                    "EyeOnSite connects diaspora investors with trusted local inspectors across African cities. " +
                        "Your job is to physically visit a location, capture GPS-stamped photos, and submit a verified " +
                        "evidence package that gives clients confidence their investment is real.",
            ),
            TrainingModule(
                icon = "📷",
                title = "Evidence Standards",
                body =
                    "All photos must be captured using the in-app camera — never from your photo gallery. " +
                        "Photos must clearly show the subject from multiple angles: overview, detail, and surroundings. " +
                        "Blurry, overexposed, or misrepresentative photos will be rejected and affect your rating.",
            ),
            TrainingModule(
                icon = "📍",
                title = "GPS Accuracy",
                body =
                    "Wait for the GPS indicator to show high accuracy before capturing photos. " +
                        "Each photo is automatically stamped with your exact coordinates. " +
                        "If your GPS coordinates don't match the task location, the client may dispute your evidence. " +
                        "Always verify you are physically at the correct address before you start.",
            ),
            TrainingModule(
                icon = "⏱",
                title = "Task Lifecycle",
                body =
                    "Once you accept a task, travel to the site promptly. Capture all required photos, " +
                        "complete the checklist, add notes if needed, and submit. " +
                        "After submission, the client has 48 hours to accept or dispute. " +
                        "If accepted, your payout is released immediately to your registered mobile money account.",
            ),
            TrainingModule(
                icon = "⚖️",
                title = "Ethics & Integrity",
                body =
                    "Submitting fraudulent evidence — photos from a different location, images sourced from the " +
                        "internet, or staged scenes — results in permanent account suspension and potential legal action. " +
                        "EyeOnSite uses AI fraud detection and cross-verifies GPS data server-side. " +
                        "Your reputation is your business.",
            ),
        )

    val quizQuestions: List<QuizQuestion> =
        listOf(
            QuizQuestion(
                text = "Where must all evidence photos be captured?",
                options =
                    listOf(
                        "From your phone's photo gallery",
                        "Using the in-app camera only",
                        "Either the gallery or in-app camera",
                        "Via a third-party camera app",
                    ),
                correctIndex = 1,
            ),
            QuizQuestion(
                text = "What should you do before capturing photos at a task site?",
                options =
                    listOf(
                        "Submit immediately to save time",
                        "Call the client to confirm the address",
                        "Wait for the GPS indicator to show high accuracy",
                        "Take photos then enable GPS",
                    ),
                correctIndex = 2,
            ),
            QuizQuestion(
                text = "When is your payout released?",
                options =
                    listOf(
                        "As soon as you submit the evidence",
                        "After the client accepts the evidence",
                        "Automatically after 48 hours",
                        "Only after you manually request it",
                    ),
                correctIndex = 1,
            ),
            QuizQuestion(
                text = "What happens if you submit fraudulent evidence?",
                options =
                    listOf(
                        "You receive a warning on the first offence",
                        "The task is reassigned to another inspector",
                        "Your account is permanently suspended",
                        "You must refund the bounty amount",
                    ),
                correctIndex = 2,
            ),
            QuizQuestion(
                text = "If a client disputes your evidence, what happens next?",
                options =
                    listOf(
                        "You are immediately penalised",
                        "The EyeOnSite team reviews it within 24 hours",
                        "The payment is refunded automatically",
                        "The task is marked as failed",
                    ),
                correctIndex = 1,
            ),
        )

    const val QUIZ_PASS_THRESHOLD = 4

    @Suppress("MagicNumber")
    val SERVICE_RADIUS_OPTIONS: List<Int> = listOf(5, 10, 15, 20, 30, 50)
}
