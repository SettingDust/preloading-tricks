plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.msrandom.net/repository/cloche")
    maven("https://raw.githubusercontent.com/settingdust/maven/main/repository/") {
        name = "SettingDust's Maven"
    }
    mavenLocal()
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(gradleApi())
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("earth.terrarium:cloche:0.18.11-dust.18")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.4.1")
    implementation("com.palantir.git-version:com.palantir.git-version.gradle.plugin:5.0.0")
    implementation("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:2.3.20")
    implementation("net.msrandom:minecraft-codev-core:0.6.9-dust.1")
    implementation("net.msrandom:minecraft-codev-fabric:0.7.0-dust.2")
    implementation("net.msrandom:minecraft-codev-forge:0.8.4-dust.1")
    implementation("net.msrandom:minecraft-codev-includes:0.6.5-dust.1")
    implementation("net.msrandom:minecraft-codev-runs:0.6.8-dust.1")
}