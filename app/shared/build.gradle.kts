import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// ── AppVersion generation ────────────────────────────────────────────────────
// Reads version from .release-please-manifest.json and the latest section from
// CHANGELOG.md, then writes AppVersion.kt into the commonMain generated sources.
// This means APP_VERSION and APP_CHANGELOG are always in sync with releases.

val generateAppVersion by tasks.registering {
    val manifestFile = rootProject.file(".release-please-manifest.json")
    val changelogFile = rootProject.file("CHANGELOG.md")
    val outDir = layout.buildDirectory.dir("generated/appversion/commonMain/kotlin/com/maku/eyeonsite")

    inputs.files(manifestFile, changelogFile)
    outputs.dir(outDir)

    doLast {
        val version =
            if (manifestFile.exists()) {
                Regex(""""\."\s*:\s*"([^"]+)"""").find(manifestFile.readText())?.groupValues?.get(1) ?: "0.1.0"
            } else {
                "0.1.0"
            }

        val changelog =
            if (changelogFile.exists()) {
                val lines = changelogFile.readLines()
                val start = lines.indexOfFirst { it.startsWith("## ") }
                if (start < 0) {
                    ""
                } else {
                    val end = lines.drop(start + 1).indexOfFirst { it.startsWith("## ") }
                    if (end < 0) {
                        lines.drop(start).joinToString("\n").trim()
                    } else {
                        lines
                            .drop(start)
                            .take(end + 1)
                            .joinToString("\n")
                            .trim()
                    }
                }
            } else {
                ""
            }

        val escaped = changelog.replace("$", "\${'\$'}")
        val file =
            outDir
                .get()
                .asFile
                .also { it.mkdirs() }
                .resolve("AppVersion.kt")
        file.writeText(
            buildString {
                appendLine("package com.maku.eyeonsite")
                appendLine()
                appendLine("const val APP_VERSION = \"$version\"")
                appendLine()
                appendLine("val APP_CHANGELOG = \"\"\"")
                appendLine(escaped)
                appendLine("\"\"\".trimIndent()")
            },
        )
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
    alias(libs.plugins.sqldelight)
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

detekt {
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    baseline = rootProject.file("config/detekt/baseline.xml")
    buildUponDefaultConfig = true
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidLibrary {
        namespace = "com.maku.eyeonsite.app.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(
                tasks.named("generateAppVersion").map {
                    layout.buildDirectory.dir("generated/appversion/commonMain/kotlin")
                },
            )
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.functions)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.camerax.core)
            implementation(libs.camerax.camera2)
            implementation(libs.camerax.lifecycle)
            implementation(libs.camerax.view)
            implementation(libs.accompanist.permissions)
            implementation(libs.play.services.location)
            implementation(libs.maps.compose)
            implementation(libs.play.services.maps)
            implementation(libs.work.runtime.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
        }
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

sqldelight {
    databases {
        create("EyeOnSiteDatabase") {
            packageName.set("com.maku.eyeonsite.db")
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
