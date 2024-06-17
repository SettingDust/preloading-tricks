pluginManagement {
    repositories {
        maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases") { name = "NeoForge" }
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "Sponge Snapshots"
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val mod_name: String by settings

rootProject.name = mod_name

include("services")
include("fabric:language-adapter")
include("fabric:fabric-loader")
include("fabric:quilt-loader")
include("neoforge:api")
include("neoforge:language-loader")
include("neoforge:fml")
