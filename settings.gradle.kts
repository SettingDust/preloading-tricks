extra["minecraft"] = "1.21"

apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/common.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/kotlin.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/fabric.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/quilt.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/neoforge.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/mods.gradle.kts")

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
