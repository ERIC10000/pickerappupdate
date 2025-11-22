plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "1.9.23" // Kotlin serialization plugin
    id("kotlin-parcelize") // For Parcelable support (if needed)
}

android {
    namespace = "com.example.kenwapwa"
    compileSdk = 36 // Use the latest stable version

    defaultConfig {
        applicationId = "com.example.kenwapwa"
        minSdk = 24
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
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Supabase dependencies
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.4.0")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.4.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.3.0")
    implementation("io.ktor:ktor-client-okhttp:2.3.4")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // BCrypt for password hashing
    implementation("org.mindrot:jbcrypt:0.4")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.0")
    implementation("com.google.zxing:core:3.5.2")
}