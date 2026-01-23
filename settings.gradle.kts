pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    // This plugin tells Gradle: "If you can't find Java, download it from here."
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "shadow-ledger"