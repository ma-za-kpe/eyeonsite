plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.sqldelight) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
}

val detektAll = tasks.register("detektAll") {
    group = "verification"
    description = "Runs Detekt on all subprojects"
}

// Single command that mirrors the pre-commit hook: spotless → detekt → tests → builds
tasks.register("validate") {
    group = "verification"
    description = "Full validation pipeline: spotless check, detekt, shared tests, Android build, web bundle"
    dependsOn(
        "spotlessCheck",
        "detektAll",
        ":app:shared:testAndroid",
        ":app:androidApp:assembleDebug",
        ":app:webApp:jsBrowserProductionWebpack",
    )
}

subprojects {
    plugins.withId("io.gitlab.arturbosch.detekt") {
        this@subprojects.tasks.withType(io.gitlab.arturbosch.detekt.Detekt::class.java).configureEach {
            // The Spec<FileTreeElement> overload of exclude() runs at execution time and filters
            // by absolute path — the only reliable way to drop generated sources (SQLDelight,
            // Compose resource generator) whose source roots live under build/ and bypass
            // path-based glob patterns that are relative to each registered source root.
            exclude { it.file.absolutePath.contains("${java.io.File.separator}build${java.io.File.separator}") }
            exclude { it.file.absolutePath.contains("${java.io.File.separator}generated${java.io.File.separator}") }
            // Exclude test source sets (backtick function names fail FunctionNaming)
            exclude { element ->
                val path = element.file.absolutePath
                path.contains("${java.io.File.separator}commonTest${java.io.File.separator}") ||
                    path.contains("${java.io.File.separator}androidHostTest${java.io.File.separator}") ||
                    path.contains("${java.io.File.separator}jsTest${java.io.File.separator}") ||
                    path.contains("${java.io.File.separator}wasmJsTest${java.io.File.separator}") ||
                    path.contains("${java.io.File.separator}iosArm64Test${java.io.File.separator}") ||
                    path.contains("${java.io.File.separator}iosSimulatorArm64Test${java.io.File.separator}")
            }
            exclude(
                // detekt 1.23.x crashes on Firebase Storage/FieldValue type resolution
                "**/FirestoreEvidenceRepository.android.kt",
                "**/FirebaseStorageEvidenceUploader.android.kt",
                "**/FirestoreEvidenceReviewer.android.kt",
            )
        }

        detektAll.configure {
            dependsOn(
                this@subprojects.tasks.matching {
                    it.name.startsWith("detekt") &&
                        !it.name.startsWith("detektBaseline") &&
                        it.name != "detektGenerateConfig"
                },
            )
        }
    }
}
