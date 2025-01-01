plugins {
    alias(libs.plugins.library)
}

android {
    namespace = "com.android.floatingtoolbar"
    compileSdk = project.properties["ide.compileSdk"].toString().toInt()

    defaultConfig {
        minSdk = project.properties["ide.minSdk"].toString().toInt()

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.core)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.utilcodex)
}