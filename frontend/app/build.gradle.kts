plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)

    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)

}

android {
    namespace = "com.fontys.frontend"
    compileSdk = 36

    buildFeatures {
        compose = true
    }

    defaultConfig {
        applicationId = "com.fontys.frontend"
        minSdk = 31
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
        viewBinding = true
    }
}

dependencies {
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // AndroidX & Material (View System)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // For classic Material Components
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)

    // Google Play Services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // --- Jetpack Compose ---
    // --- Jetpack Compose ---
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Core Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose Material Libraries
    implementation(libs.androidx.compose.material)           // For basic material components in Compose
    implementation(libs.androidx.compose.material.icons.extended) // For the icons
    implementation(libs.androidx.compose.material3)          // For Material 3 components

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


// ✅ Kotlin DSL way to configure Secrets plugin
extensions.configure<com.google.android.libraries.mapsplatform.secrets_gradle_plugin.SecretsPluginExtension> {
    propertiesFileName = "local.properties"
    defaultPropertiesFileName = "local.properties.example"
}
