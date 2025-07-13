plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("org.sonarqube") version "6.2.0.5505"
}
sonar {
    properties {
        property("sonar.projectKey", "CodigoCreativoUTEC_mobilePFT_7e6d6750-d21c-465b-8965-d60487ac1443")
        property("sonar.projectName", "mobilePFT")
    }
}
android {
    namespace = "com.codigocreativo.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.codigocreativo.mobile"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3.android)
    implementation(libs.filament.android)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    implementation(libs.identity.credential)
    implementation(libs.androidx.room.ktx)
    implementation(libs.glide.v4160)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase Authentication
    implementation(platform(libs.firebase.bom.v3340))
    implementation(libs.firebase.auth.ktx)

    implementation (libs.androidx.credentials)
    implementation (libs.androidx.credentials.play.services.auth)
    implementation (libs.googleid)

    // Google Play Services para autenticación con Google
    implementation(libs.play.services.auth)

    // Retrofit y GSON
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttpClient para manejar peticiones HTTP
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Coroutines para llamadas asíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

    implementation (libs.jwtdecode)

    implementation (libs.androidx.credentials.v100)

    implementation (libs.androidx.credentials.v120beta03)
    implementation (libs.androidx.credentials.play.services.auth.v120beta03)
    implementation (libs.play.services.auth.v2070)

    // Glide para cargar imágenes
    implementation(libs.glide)

}

apply(plugin = "com.google.gms.google-services")
