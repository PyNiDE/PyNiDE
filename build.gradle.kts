plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.kotlin) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}