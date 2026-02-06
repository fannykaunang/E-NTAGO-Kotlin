import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(FileInputStream(localFile))
}

android {
    namespace = "com.kominfo_mkq.entago"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.kominfo_mkq.entago"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY")}\"")

        val apiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
        buildConfig = true
        compose = true
    }
}

dependencies {
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Icons
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.google.android.material:material:1.11.0")

    // Maps OpenStreetMaps
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    //implementation("com.github.MKRGX:OSMBonusPack:6.9.0")
    //implementation("com.github.MKRGW:OSMBonusPack:v6.9.0")
    //implementation("com.github.MKRGW:OSMBonusPack:6.9.0")
    implementation("com.github.MKergall:osmbonuspack:6.9.0")

    // Image
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    //Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Room Database/Oflline mode
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // App Updates
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    // Google AI SDK for Android
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Markdown support (opsional, agar output AI bisa bold/list rapi)
    implementation("io.noties.markwon:core:4.6.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}