// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Applies the Kotlin Compose plugin for Jetpack Compose support in Kotlin
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"

    // Makes the Android application plugin available for subprojects, but does not apply it to this project
    alias(libs.plugins.android.application) apply false
    // Makes the Kotlin Android plugin available for subprojects, but does not apply it to this project
    alias(libs.plugins.kotlin.android) apply false

    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.3" apply false
}