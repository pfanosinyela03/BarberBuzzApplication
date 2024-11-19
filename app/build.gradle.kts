plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.barberbuzz"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.barberbuzz"
        minSdk = 24
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

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.test.ext:junit-ktx:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation ("androidx.drawerlayout:drawerlayout:1.2.0")

    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.robolectric:robolectric:4.8")


    implementation ("com.squareup.okhttp3:okhttp:4.10.0")

    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.2")

    implementation ("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.2")



   //test dependencies
    testImplementation ("org.jetbrains.kotlin:kotlin-test")
    testImplementation ("org.mockito:mockito-core:4.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("io.mockk:mockk:1.12.3")


    testImplementation ("androidx.test.ext:junit:1.2.1")
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

// For AndroidX Test libraries
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.espresso:espresso-core:3.6.1")
    // Mockito for mocking Toast
    testImplementation ("org.robolectric:robolectric:4.10.3")
    testImplementation ("androidx.test:core:1.6.1")
    testImplementation ("org.mockito:mockito-core:5.4.0")











}