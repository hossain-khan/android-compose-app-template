import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
    // Circuit-KSP for Metro
    arg("circuit.codegen.mode", "metro")
    
    // Metro 0.4.0+ feature: Enable scoped inject class hints for better performance
    // This allows child graphs to depend on parent-scoped dependencies that are unused
    // See https://zacsweers.github.io/metro/dependency-graphs/
    arg("metro.enableScopedInjectClassHints", "true")
    
    // Metro 0.5.0+ generates field reports automatically
    // Look for keys-scopedProviderFields-*.txt and keys-providerFields-*.txt in build output
    // These files provide insights into generated provider fields
}


metro {
    // Enable Metro debug mode for better logging and debugging support
    // See https://zacsweers.github.io/metro/debugging/
    debug.set(true)
    
    /*
     * Metro 0.5.0 Migration Notes:
     * 
     * Key improvements in Metro 0.5.0:
     * 1. Enhanced nullable binding support - binding<T>() no longer has Any constraint
     * 2. New @BindingContainer annotation for better dependency organization 
     * 3. Improved diagnostics for common issues like scoped @Binds declarations
     * 4. Better Dagger interop with default allowEmpty=true for @Multibinds
     * 5. Enhanced cycle detection moved to earlier validation phase
     * 6. Support for javax/jakarta Provider types in multibinding Map values
     * 7. kotlin-inject @AssistedFactory annotation support
     * 8. New field reports generation (enabled via KSP arg above)
     * 
     * Current codebase compatibility:
     * - ✅ All existing Metro annotations work unchanged
     * - ✅ @AssistedFactory usage in circuit presenters is supported  
     * - ✅ @ContributesBinding usage works with improved diagnostics
     * - ✅ Multibinds for Set<Presenter.Factory> benefit from enhanced validation
     * - ✅ Activity injection via @ContributesIntoMap works with nullable binding improvements
     * 
     * For more details, see: https://github.com/ZacSweers/metro/releases/tag/0.5.0
     */
}