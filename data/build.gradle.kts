plugins {
    id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
    alias(libs.plugins.kapt)
    alias(libs.plugins.anvil)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(libs.blake3)
    implementation(libs.multiplatform.crypto.libsodium.bindings)
    implementation(libs.dagger.impl)
    kapt(libs.dagger.compiler)
}