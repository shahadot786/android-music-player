plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.shahadot.android_music_player"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shahadot.android_music_player"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.exoplayer)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.palette)
    implementation(libs.waveformseekbar)
    implementation(libs.glide.transformations)
}