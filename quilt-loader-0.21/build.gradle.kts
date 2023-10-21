plugins {
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom)
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
    minecraft(libs.minecraft20)
    mappings(variantOf(libs.yarn.mapping20) {
        classifier("v2")
    })
    modImplementation(libs.quilt.loader)

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }

    runtimeOnly(project(":fabric-like-language-adapter")) {
        exclude(module = "fabric-loader")
    }
    include(project(":fabric-like-language-adapter"))

    modRuntimeOnly(libs.modmenu) {
        exclude(module = "fabric-loader")
    }
}
