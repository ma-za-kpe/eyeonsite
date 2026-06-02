import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProps = Properties().also { props ->
    rootProject.file("local.properties").takeIf { it.exists() }
        ?.inputStream()?.use { props.load(it) }
}

// versionName: read from release-please manifest; fallback to 0.1.0
val manifestFile = rootProject.file(".release-please-manifest.json")
val resolvedVersionName: String =
    if (manifestFile.exists()) {
        Regex(""""\."\s*:\s*"([^"]+)"""").find(manifestFile.readText())?.groupValues?.get(1) ?: "0.1.0"
    } else {
        "0.1.0"
    }

// versionCode: injected by CI as -PversionCode=<run_number>; defaults to 1 locally
val resolvedVersionCode: Int = (findProperty("versionCode") as String?)?.toInt() ?: 1

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
    id("com.google.gms.google-services")
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.firebaseAppDistribution)
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
}

detekt {
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    baseline = rootProject.file("config/detekt/baseline.xml")
    buildUponDefaultConfig = true
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.app.shared)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    // Firebase (BoM manages all version alignment)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}

android {
    namespace = "com.maku.eyeonsite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.maku.eyeonsite"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = resolvedVersionCode
        versionName = resolvedVersionName
        manifestPlaceholders["MAPS_API_KEY"] = localProps.getProperty("MAPS_API_KEY", "")
    }
    flavorDimensions += "variant"
    productFlavors {
        create("client") {
            dimension = "variant"
            firebaseAppDistribution {
                appId = "1:922315268903:android:9a262a3db6364511fb7f02"
                groups = "internal"
                artifactType = "APK"
            }
        }
        create("inspector") {
            dimension = "variant"
            applicationIdSuffix = ".inspector"
            firebaseAppDistribution {
                appId = "1:922315268903:android:83ace1bab8aeac06fb7f02"
                groups = "internal"
                artifactType = "APK"
            }
        }
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
