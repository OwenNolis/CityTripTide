plugins {
    // Apply the Android application plugin for building Android apps
    alias(libs.plugins.android.application)
    // Apply the Kotlin Android plugin for Kotlin support in Android
    alias(libs.plugins.kotlin.android)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

    // Add the Firebase Crashlytics Gradle plugin
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
}

android {
    namespace = "devkonig.citytriptride"
    compileSdk = 36

    defaultConfig {
        applicationId = "devkonig.citytriptride"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {

    // Core library for Android development
    implementation(libs.androidx.core.ktx)
    // UI components and Material Design
    implementation(libs.androidx.appcompat)
    // ConstraintLayout for flexible UI designs
    implementation(libs.material)
    // Compose UI toolkit for building native UIs
    implementation(libs.androidx.material3.android)
    // Adds JUnit library for unit testing
    testImplementation(libs.junit)
    // Adds AndroidX JUnit library for Android instrumentation testing
    androidTestImplementation(libs.androidx.junit)
    // Adds Espresso library for UI testing
    androidTestImplementation(libs.androidx.espresso.core)

    // Adds Compose UI testing support
    implementation(libs.androidx.activity.compose)
    // Adds the core Jetpack Compose UI library for building declarative UIs
    implementation(libs.androidx.ui)
    // Adds the Compose Material library for Material Design components
    implementation(libs.androidx.material)
    // Adds Jetpack Compose UI Tooling Preview library for live previews of composables in Android Studio
    implementation(libs.androidx.ui.tooling.preview)
    // Adds AndroidX Lifecycle Runtime KTX for lifecycle-aware components with Kotlin extensions
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Adds AndroidX LiveData KTX for observable data holder classes with Kotlin extensions
    implementation(libs.androidx.runtime.livedata)

    // Adds AndroidX Navigation Compose for navigation in Jetpack Compose applications
    implementation(libs.androidx.navigation.compose)

    // Adds the Coil library for image loading in Jetpack Compose applications
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Adds extended Material Design icons for Jetpack Compose UI
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Adds coroutine support for Google Play Services APIs in Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // Adds the Google Maps Compose library for integrating Google Maps in Jetpack Compose applications
    implementation(libs.maps.compose)
    // Adds the Google Maps Android API for integrating Google Maps in Android applications
    implementation(libs.play.services.maps)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation(libs.firebase.analytics)

    // Dependencies for Firebase products you want to use
    // See https://firebase.google.com/docs/android/setup#available-libraries
    // Adds Firebase Authentication library for user sign-in and authentication features
    implementation(libs.firebase.auth)
    // Adds Firebase Cloud Firestore for real-time NoSQL cloud database functionality
    implementation(libs.firebase.firestore)
}