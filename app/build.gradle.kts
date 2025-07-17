plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("com.google.firebase.firebase-perf")

}

android {
    namespace = "com.gity.feliyaattendance"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gity.feliyaattendance"
        minSdk = 28
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildFeatures {
        buildConfig = true
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
//    Circle Image
    implementation(libs.circleimageview)

//    Chip Navigation
    implementation(libs.chip.navigation.bar)

//    Lottie
    implementation(libs.lottie)

//    Firebase Bom
    implementation(platform(libs.firebase.bom))

//    When using the BoM, don't specify versions in Firebase dependencies
    implementation(libs.firebase.analytics)

//    Firebase Firestore
    implementation(libs.firebase.firestore)

//    Firebase Auth
    implementation(libs.firebase.auth)

//    Firebase Perfomance Monitoring
    implementation("com.google.firebase:firebase-perf:21.0.2")


//    Coroutine Handling for Firebase
    implementation(libs.kotlinx.coroutines.play.services)

//    Glide
    implementation(libs.glide)

//    Data Store
    implementation(libs.androidx.datastore.preferences)

//    Image Auto Slider
    implementation(libs.imageslideshow)
    // All:
    implementation(libs.cloudinary.android.v302)
    //noinspection UseTomlInstead
    implementation("com.cloudinary:cloudinary-android:3.0.2")

// Download + Preprocess:
    implementation(libs.cloudinary.android.download)
    implementation(libs.cloudinary.android.preprocess)

//    Export data To Excel

    // Use HSSF instead of XSSF for Android compatibility
    implementation("org.apache.poi:poi:5.2.3")
    // Add required dependencies
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.xmlbeans:xmlbeans:5.1.1")

//    Support Library .env (Dot ENV)
    implementation ("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // TensorFlow Lite
    implementation ("org.tensorflow:tensorflow-lite:2.13.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")

// JSON parsing untuk metadata
    implementation ("com.google.code.gson:gson:2.10.1")

// Chart library untuk visualisasi (optional)
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}