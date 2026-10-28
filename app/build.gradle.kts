plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Keystore and password are supplied via CI secrets or a developer's local environment.
// Never commit a private key, a password or a password-bearing gradle.properties file.
val monolithKeystore = providers.environmentVariable("MONOLITH_KEYSTORE_FILE").orNull
    ?.takeIf { it.isNotBlank() }
val monolithSigningPassword = providers.environmentVariable("MONOLITH_KEY_PASSWORD").orNull
    ?.takeIf { it.isNotBlank() }

android {
    namespace = "dev.monolith.launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.monolith.launcher"
        minSdk = 26
        targetSdk = 36
        versionCode = providers.environmentVariable("MONOLITH_VERSION_CODE").orNull
            ?.toIntOrNull()?.takeIf { it >= 4 } ?: 4
        versionName = providers.environmentVariable("MONOLITH_VERSION_NAME").orNull
            ?.takeIf { it.isNotBlank() } ?: "0.9.0-beta"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (monolithKeystore != null && monolithSigningPassword != null) {
        signingConfigs {
            create("monolithRelease") {
                storeFile = file(monolithKeystore)
                storePassword = monolithSigningPassword
                keyAlias = "monolith"
                keyPassword = monolithSigningPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (monolithKeystore != null && monolithSigningPassword != null) {
                signingConfig = signingConfigs.getByName("monolithRelease")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
