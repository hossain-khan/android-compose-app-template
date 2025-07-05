import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    // kotlin.kapt plugin removed
    alias(libs.plugins.ksp)
    // anvil plugin removed
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
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

    // Dagger dependencies removed
    // implementation(libs.dagger)
    // kapt(libs.dagger.compiler)

    // Anvil dependencies removed
    // implementation(libs.anvil.annotations)
    // implementation(libs.anvil.annotations.optional)

    implementation(libs.metro.runtime)

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
    // Anvil-KSP args removed
    // arg("anvil-ksp-extraContributingAnnotations", "com.slack.circuit.codegen.annotations.CircuitInject")
    // arg("kotlin-inject-anvil-contributing-annotations", "com.slack.circuit.codegen.annotations.CircuitInject")

    // Add Circuit codegen mode for Metro if required, based on Metro documentation or sample.
    // The patch file showed: ksp { arg("circuit.codegen.mode", "METRO") }
    arg("circuit.codegen.mode", "METRO")
}
