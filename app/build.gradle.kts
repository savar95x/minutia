import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Single source of truth for the app version: version.properties at the repo root.
val appVersionName: String = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}.getProperty("VERSION_NAME").trim()

// 1.2.3 -> 10203. Always increases with the version, so every release installs as an update.
val appVersionCode: Int = appVersionName.split(".").map { it.toInt() }.let { (major, minor, patch) ->
    require(minor < 100 && patch < 100) { "Keep MINOR and PATCH below 100" }
    major * 10_000 + minor * 100 + patch
}

android {
    namespace = "dev.savar.minutia"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.savar.minutia"
        minSdk = 29
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.jks")
            storePassword = "clockwidget"
            keyAlias = "clock"
            keyPassword = "clockwidget"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    // Pinned past the BOM like Aria, Hiatus and Regista: the BOM's stable material3 keeps the Expressive APIs internal.
    implementation("androidx.compose.material3:material3:1.5.0-alpha27")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    // Shared kit (dev.savar.kit): app font via Play Services downloadable Google Fonts.
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
