@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "com.pynide"
    compileSdk = 34
    ndkVersion = "26.2.11394342"

    defaultConfig {
        applicationId = "com.pynide"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        externalNativeBuild {
            cmake {
                arguments("-DANDROID_STL=c++_static")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = false
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = true

        disable += "MissingTranslation"
        disable += "ExtraTranslation"
        disable += "BlockedPrivateApi"
    }
}

configurations.configureEach {
    exclude("androidx.recyclerview", "recyclerview")
}

dependencies {
    implementation(libs.core)
    implementation(libs.appcompat)
    implementation(libs.fragment)

    coreLibraryDesugaring(libs.desugar)
}