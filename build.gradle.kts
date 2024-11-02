// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
id("org.sonarqube") version "5.1.0.4882"
}

sonar {
  properties {
    property("sonar.projectKey", "CodigoCreativoUTEC_mobilePFT_ac8d7ecb-83c0-42cf-986e-373f612278a8")
    property("sonar.projectName", "mobilePFT")
  }

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath(libs.google.services)
    }
}

