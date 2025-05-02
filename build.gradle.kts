buildscript {
    dependencies {
        // Remove the KSP plugin classpath here
       // classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")  // Keep the Kotlin plugin classpath here
    }
}

plugins {
    id("com.android.application") version "8.8.1" apply false // Only define the plugin version here
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false // Define Kotlin plugin version
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false // Define KSP plugin version
}

