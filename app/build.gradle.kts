

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.simbasfriends"
    compileSdk = 35


    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.simbasfriends"
        minSdk = 26
        targetSdk = 34
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.cast.framework)
    implementation(libs.androidx.preference)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.lottie)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.google.material.v190)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.gson)
    implementation(libs.prefy)

    //Glide
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    //ImagePicker
    implementation (libs.imagepicker)

    //Stream chat
    implementation (libs.stream.chat.android.ui.components)
    implementation (libs.stream.chat.android.offline)
}