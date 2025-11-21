//plugins {
//    id("com.android.application")
//    kotlin("android")
//    kotlin("plugin.serialization")
//    kotlin("plugin.compose")
//}
//
//
//
//android {
//    namespace = "com.example.joshtalktaskapp"
//    compileSdk = 34
//
//    defaultConfig {
//        applicationId = "com.example.joshtalktaskapp"
//        minSdk = 24
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            // if you want signing via command line, configure signingConfig here
//        }
//    }
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//    kotlinOptions {
//        jvmTarget = "17"
//    }
//}
//
//dependencies {
//    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
//    implementation(composeBom)
//    androidTestImplementation(composeBom)
//
//    implementation("androidx.core:core-ktx:1.13.1")
//    implementation("androidx.activity:activity-compose")
//    implementation("androidx.compose.ui:ui")
//    implementation("androidx.compose.material3:material3")
//    implementation("androidx.compose.ui:ui-tooling-preview")
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
//
//    // Coil for image loading (optional)
//    implementation("io.coil-kt:coil-compose:2.4.0")
//
//    // Kotlinx serialization for JSON
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
//
//    // Local unit test dependencies
//    testImplementation("junit:junit:4.13.2")
//
//
//    // Coroutines
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
//
//    // Testing dependencies
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
//    debugImplementation("androidx.compose.ui:ui-tooling")
//    debugImplementation("androidx.compose.ui:ui-test-manifest")
//}

// app/build.gradle.kts

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.example.joshtalktaskapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.joshtalktaskapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // add signingConfig here later if you want automatic signed release
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
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2023.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Coil for images
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("io.coil-kt:coil-compose:2.2.2")

    // Kotlinx serialization for JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
