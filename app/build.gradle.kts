import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
    alias(libs.plugins.kotlinter)
}

android {
    namespace = "app.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.example"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Read key or other properties from local.properties
        val localProperties =
            project.rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use {
                Properties().apply { load(it) }
            }
        val apiKey = localProperties?.getProperty("SERVICE_API_KEY") ?: "MISSING-KEY"
        buildConfigField("String", "SERVICE_API_KEY", "\"$apiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            // For CI/CD: Use keystore from environment variables (GitHub Actions)
            // For local builds: Fall back to debug keystore
            val keystoreFile = System.getenv("KEYSTORE_FILE")?.let { rootProject.file(it) }
                ?: file("../keystore/debug.keystore")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
            val keyAliasValue = System.getenv("KEY_ALIAS") ?: "androiddebugkey"
            // Note: Using the same password for both store and key is a common practice and required
            // by the setup documented in RELEASE.md. If you need different passwords, add a KEY_PASSWORD
            // environment variable: System.getenv("KEY_PASSWORD") ?: keystorePassword
            val keyPasswordValue = keystorePassword

            storeFile = keystoreFile
            storePassword = keystorePassword
            keyAlias = keyAliasValue
            keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // Disable Instantiatable lint rule because we use a custom AppComponentFactory
        // (ComposeAppComponentFactory) for dependency injection. Activities are injected
        // via constructor parameters and instantiated by our DI framework (Metro) rather
        // than the Android system's default no-arg constructor mechanism.
        disable += "Instantiatable"
    }
}

kotlin {
    // See https://kotlinlang.org/docs/gradle-compiler-options.html
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.circuit.codegen.annotations)
    implementation(libs.circuit.foundation)
    implementation(libs.circuit.overlay)
    implementation(libs.circuitx.android)
    implementation(libs.circuitx.effects)
    implementation(libs.circuitx.gestureNav)
    implementation(libs.circuitx.overlays)
    ksp(libs.circuit.codegen)

    implementation(libs.javax.inject)

    implementation(libs.androidx.work)

    // Testing
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
}

ksp {
    // Circuit-KSP configuration for Metro DI integration
    // See https://slackhq.github.io/circuit/code-gen/
    arg("circuit.codegen.mode", "metro")
}

metro {
    // Enable Metro debug mode for better logging and debugging support
    // When enabled, Metro will emit detailed debug information about the dependency graph
    // See https://zacsweers.github.io/metro/latest/
    debug.set(true)

    // Shrink unused bindings to reduce generated code size (enabled by default)
    // See https://zacsweers.github.io/metro/latest/dependency-graphs/
    shrinkUnusedBindings.set(true)

    // Enable chunking of field initializers for better performance in large graphs (enabled by default)
    // See https://zacsweers.github.io/metro/latest/dependency-graphs/
    chunkFieldInits.set(true)
}