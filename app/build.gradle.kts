import java.util.Properties

plugins {
    alias(libs.plugins.kapt)
    alias(libs.plugins.serialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.anvil)
    alias(libs.plugins.ksp)
    alias(libs.plugins.googleServices)
}

val serverPropertiesFile = rootProject.file("server.properties")
val serverProperties = Properties()
if (serverPropertiesFile.exists()) {
    serverProperties.load(serverPropertiesFile.inputStream())
}

android {
    namespace = "online.mempool.fatline.client"
    compileSdk = 34

    defaultConfig {
        applicationId = "online.mempool.fatline.client"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        val serverUrl = serverProperties.getProperty("SERVER_URL", null)

        buildConfigField("String", "SERVER_URL", "\"$serverUrl\"")

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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.fcm.bom))
    implementation(platform(libs.coil.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.slack.circuit.foundation)
    api(libs.okhttp)
    api(libs.slack.circuit.codegen.annotations)
    ksp(libs.slack.circuit.codegen)
    implementation(project(":api"))
    implementation(project(":data"))
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.dagger.impl)
    implementation(libs.datastore)
    kapt(libs.dagger.compiler)
    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    testImplementation(libs.room.testing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit.jupiter)
}