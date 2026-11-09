plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing configuration is read from environment variables or from an
// untracked gradle.properties (e.g. in GRADLE_USER_HOME, never committed).
// Never hardcode signing credentials in this file. See SECURITY.md for details.
val releaseStoreFilePath: String = System.getenv("MYPEN_KEYSTORE_PATH")
    ?: findProperty("MYPEN_KEYSTORE_PATH") as String?
    ?: "../../key/mypen-release.jks"
val releaseStorePassword: String? = System.getenv("MYPEN_STORE_PASSWORD")
    ?: findProperty("MYPEN_STORE_PASSWORD") as String?
val releaseKeyAlias: String = System.getenv("MYPEN_KEY_ALIAS")
    ?: findProperty("MYPEN_KEY_ALIAS") as String?
    ?: "mypen"
val releaseKeyPassword: String? = System.getenv("MYPEN_KEY_PASSWORD")
    ?: findProperty("MYPEN_KEY_PASSWORD") as String?

val hasReleaseSigning = file(releaseStoreFilePath).exists() &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

android {
    namespace = "com.arena.mypen"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.arena.mypen"
        minSdk = 24
        targetSdk = 34
        versionCode = 1000
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = file(releaseStoreFilePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                logger.lifecycle(
                    "WARNING: Release signing secrets are not configured. " +
                        "The release build will be UNSIGNED. See SECURITY.md."
                )
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Uses the default auto-generated debug keystore so any machine
            // and CI can build debug APKs without the release keystore.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20231013")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}
