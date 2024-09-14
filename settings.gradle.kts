extra["minecraft"] = "1.20.1"

apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/common.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/fabric.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/forge.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/parchmentmc.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/quilt.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/modmenu.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/mixin.gradle.kts")

pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases") { name = "NeoForge" }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement.versionCatalogs.named("catalog") {
    library("minecraft-fabric-1.21", "com.mojang", "minecraft").version("1.21")

    plugin("neoforge-gradle", "net.neoforged.gradle.userdev").version("7.+")
    plugin("neoforge-gradle-vanilla", "net.neoforged.gradle.vanilla").version("7.+")
    plugin("neoforge-gradle-mixin", "net.neoforged.gradle.mixin").version("7.+")

    // https://linkie.shedaniel.dev/dependencies?loader=neoforge
    library("neoforge", "net.neoforged", "neoforge").version("21.1.12")

    library("minecraft-forge-1.18.2", "net.minecraftforge", "forge")
        .version("1.18.2-40.2.21")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("com.gradle.develocity") version("3.17.6")
}

val mod_name: String by settings

rootProject.name = mod_name

include("services")
include("fabric:language-adapter")
include("fabric:fabric-loader")
include("fabric:quilt-loader")
//include("neoforge")
//include("neoforge:api")
//include("neoforge:language-loader")
//include("neoforge:fancy-mod-loader")
include("lexforge")
include("lexforge:api")
include("lexforge:language-provider")
include("lexforge:forge-mod-loader")
include("lexforge:forge-mod-loader-40")
