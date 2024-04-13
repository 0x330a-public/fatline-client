plugins {
    id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
    kotlin("kapt") version libs.versions.kotlin
    alias(libs.plugins.anvil)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.blake3)
    implementation(libs.multiplatform.crypto.libsodium.bindings)
    implementation(libs.dagger.impl)
    kapt(libs.dagger.compiler)
}