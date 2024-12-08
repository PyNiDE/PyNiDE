@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.themebuilder)
    alias(libs.plugins.autoresconfig)
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
        versionName = "1.0"

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
        viewBinding = true
    }

    buildTypes {
        getByName("debug") {
            versionNameSuffix = "-debug"
        }
        getByName("release") {
            versionNameSuffix = "-release"
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

materialThemeBuilder {
    themes {
        create("PyNiDE") {
            primaryColor = "#3F51B5"
            lightThemeFormat = "Theme.Material3.Light.%s"
            lightThemeParent = "Theme.Material3.Light"
            darkThemeFormat = "Theme.Material3.Dark.%s"
            darkThemeParent = "Theme.Material3.Dark"
        }
    }
    generatePaletteAttributes = true
    generateTextColors = true
}

autoResConfig {
    generatedClassFullName = "com.pynide.IDELocales"
    generateRes = false
    generatedArrayFirstItem = "SYSTEM"
    generateLocaleConfig = true
}

dependencies {
    implementation(libs.core)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.fragment.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.preference.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.splashscreen)

    implementation(libs.rikkax.insets)
    implementation(libs.rikkax.layoutInflater)
    implementation(libs.rikkax.simplemenu.preference)
    implementation(libs.rikkax.material.preference) {
        exclude("dev.rikka.rikkax.appcompat", "appcompat")
    }

    implementation(libs.utilcodex)
    implementation(libs.eventbus)
    coreLibraryDesugaring(libs.desugar)
}