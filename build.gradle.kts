// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("org.sonarqube") version "6.2.0.5505"
}

sonar {
    properties {
        property("sonar.projectKey", "CodigoCreativoUTEC_mobilePFT_7e6d6750-d21c-465b-8965-d60487ac1443")
    property("sonar.projectName", "mobilePFT")
    }
}

buildscript {
    repositories {
        google()
    }
    dependencies {
       classpath(libs.google.services)
    }
}
