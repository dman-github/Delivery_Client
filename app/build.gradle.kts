import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.okada.rider.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.okada.rider.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 15
        versionName = "1.14"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        // Load the apiKeys.properties file which contains keys
        val apiKeysProperties = Properties()
        val apiKeysFile = rootProject.file("apiKeys.properties")
        if (apiKeysFile.exists()) {
            apiKeysFile.inputStream().use { apiKeysProperties.load(it) }
        }
        // Set your Google Maps API key as a build configuration field
        resValue("string", "GOOGLE_MAPS_API_KEY", "\"${apiKeysProperties.getProperty("GOOGLE_MAPS_API_KEY")}\"")
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
        compose = false
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.9.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")

    //Material
    implementation("com.google.android.material:material:1.11.0")

    //Firebase
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation ("com.firebaseui:firebase-ui-auth:7.2.0")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.firebase:firebase-functions:21.0.0")
    //Easy permissions
    implementation("com.vmadalin:easypermissions-ktx:1.0.0")

    //Location
    implementation("com.google.android.gms:play-services-location:21.2.0")

    //Geofire
    implementation("com.firebase:geofire-android:3.2.0")

    //Maps and Places
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.libraries.places:places:3.4.0")

    //Circular image view
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    // Sliding up panel
    implementation ("com.sothree.slidinguppanel:library:3.4.0")

    // Glide for loading images
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    //retrofit
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    //RxJava
    implementation ("io.reactivex.rxjava2:rxandroid:2.1.1")

    // Google maps utils
    implementation("com.google.maps.android:android-maps-utils:3.8.2")

    //Eventbus
    implementation("org.greenrobot:eventbus:3.3.1")

    //Crashlytics
    implementation("com.google.firebase:firebase-crashlytics:19.2.1")
    implementation("com.google.firebase:firebase-analytics:22.1.2")

}