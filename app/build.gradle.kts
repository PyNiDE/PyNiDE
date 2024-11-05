import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "com.pynide"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pynide"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        buildConfig = true
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

configurations.all {
    exclude(group = "androidx.recyclerview", module = "recyclerview")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}