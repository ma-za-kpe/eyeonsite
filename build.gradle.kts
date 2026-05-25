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
    alias(libs.plugins.firebaseCrashlytics) apply false
}

val detektAll = tasks.register("detektAll") {
    group = "verification"
    description = "Runs Detekt on all subprojects"
}

subprojects {
    plugins.withId("io.gitlab.arturbosch.detekt") {
        this@subprojects.tasks.withType(io.gitlab.arturbosch.detekt.Detekt::class.java).configureEach {
            exclude("**/build/**", "**/generated/**")
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
