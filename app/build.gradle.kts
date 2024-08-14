@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.calorycounter"
    compileSdk = 34

    bundle {
        language {
            enableSplit = false
        }
    }

    defaultConfig {
        applicationId = "com.example.calorycounter"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.wear)
    implementation(libs.androidx.work.runtime.ktx)
//    implementation(libs.play.services.basement)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //noinspection UseTomlInstead
    implementation("org.jsoup:jsoup:1.17.2")
    //noinspection UseTomlInstead
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    //noinspection UseTomlInstead
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    //noinspection UseTomlInstead
    implementation("jp.wasabeef:blurry:4.0.1")
    //noinspection UseTomlInstead
    implementation("org.apache.commons:commons-configuration2:2.11.0")
}