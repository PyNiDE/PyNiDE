@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.themebuilder)
}

android {
    namespace = "com.pynide"
    compileSdk = project.properties["ide.compileSdk"].toString().toInt()
    ndkVersion = project.properties["ide.ndkVersion"].toString()

    defaultConfig {
        applicationId = "com.pynide"
        minSdk = project.properties["ide.minSdk"].toString().toInt()
        targetSdk = project.properties["ide.targetSdk"].toString().toInt()
        versionCode = 1
        versionName = "1.0"
        resourceConfigurations += "en"

        externalNativeBuild {
            cmake {
                arguments("-DANDROID_STL=c++_static")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = project.properties["ide.cmakeVersion"].toString()
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
        }
        release {
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

dependencies {
    implementation(project(":terminal"))
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.core)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.fragment.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.preference.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.splashscreen)

    implementation(libs.rikka.insets)
    implementation(libs.rikka.layoutInflater)
    implementation(libs.rikka.simplemenu.preference)
    implementation(libs.rikka.material.preference) {
        exclude("dev.rikka.rikkax.appcompat", "appcompat")
        exclude("dev.rikka.rikkax.core", "core")
        exclude("dev.rikka.rikkax.material", "material")
    }
    implementation(libs.utilcodex)
    implementation(libs.eventbus)
}