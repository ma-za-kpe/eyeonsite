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
    id("com.google.gms.google-services") version "4.4.4" apply false
}

// Aggregate detekt task across all subprojects
tasks.register("detektAll") {
    group = "verification"
    description = "Runs Detekt on all subprojects"
    subprojects.forEach { subproject ->
        subproject.afterEvaluate {
            subproject.tasks.findByName("detekt")?.let { dependsOn(it) }
        }
    }
}
