pluginManagement {
    repositories {
        maven("https://maven.architectury.dev/")
        maven {
            name = "Quilt"
            url = uri("https://maven.quiltmc.org/repository/release")
        }
        // Currently needed for Intermediary and other temporary dependencies
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "NeoForge"
            url = uri("https://maven.neoforged.net/releases")
        }

        maven {
            name = "Forge"
            url = uri("https://maven.minecraftforge.net/")
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "preloading-tricks"

include("preloading-callbacks")

include("fabric-loader-0.14")
include("quilt-loader-0.21")
include("fabric-like-language-adapter")

include("fml-47")
include("fml-40")
include("forge-language-provider")