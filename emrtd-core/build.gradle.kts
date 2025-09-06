
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.omartech.emrtd_core"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.omartech.emrtd_core"
        minSdk = 26
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    // Core eMRTD stack (latest as of Jul 31, 2025)
//    implementation(libs.jmrtd)    // eMRTD host API
//    implementation(libs.bcprov) // crypto primitives
//    implementation(libs.bcpkix) // CMS/X.509 helpers
//    // JPEG2000 decoding for DG2 portraits (pure-Java JJ2000 via JAI ImageIO)
//    implementation(libs.jai.core)
//    implementation(libs.jai.jp2)

}