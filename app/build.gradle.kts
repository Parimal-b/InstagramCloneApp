plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.instagramclone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.instagramclone"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.04.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.firebase:firebase-firestore:24.11.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation ("androidx.navigation:navigation-compose:2.7.7")

    implementation ("com.google.firebase:firebase-auth:22.3.1")
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.firebase:firebase-storage-ktx")

    implementation ("com.google.dagger:hilt-android:2.50")
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    kapt("com.google.dagger:hilt-android-compiler:2.50")

    implementation ("io.coil-kt:coil-compose:1.3.2")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Test dependencies
    testImplementation("io.mockk:mockk:1.12.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito:mockito-android:3.12.4")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Unit testing
    testImplementation("junit:junit:4.13.2")

    // Mocking framework
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    androidTestImplementation("org.mockito:mockito-android:3.12.4")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.5")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.5")

    // JUnit dependencies
    testImplementation("junit:junit:4.13.2")

}

