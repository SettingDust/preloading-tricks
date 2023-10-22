plugins {
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom)
    alias(libs.plugins.shadow)
}

architectury {
    platformSetupLoomIde()
    loader("quilt")
}

val mod_id: String by rootProject

repositories {
    maven {
        name = "Quilt"
        url = uri("https://maven.quiltmc.org/repository/release")
    }
    maven("https://maven.terraformersmc.com/releases")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    modImplementation(libs.quilt.loader)

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }

    shadow(runtimeOnly(project(":fabric-like:language-adapter")) {
        exclude(module = "fabric-loader")
    }) {
        isTransitive = false
    }

    modRuntimeOnly(libs.modmenu) {
        exclude(module = "fabric-loader")
    }
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier = ""
        exclude("fabric.mod.json")
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }
}
