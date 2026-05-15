// #region Plugin Management

dependencyResolutionManagement {
    pluginManagement {
        repositories {
            mavenCentral()
            gradlePluginPortal()
            maven("https://maven.msrandom.net/repository/cloche")
            maven("https://raw.githubusercontent.com/settingdust/maven/main/repository/") {
                name = "SettingDust's Maven"
            }
            mavenLocal()
        }
    }
}

// #endregion

// #region Project Settings

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "PreloadingTricks"

// #endregion