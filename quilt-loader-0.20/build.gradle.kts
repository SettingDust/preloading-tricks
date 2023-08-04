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
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    modImplementation(libs.quilt.loader)
    implementation(project(":preloading-callbacks"))

    include(project(":preloading-callbacks"))

    modRuntimeOnly(libs.modmenu) {
        exclude(module = "fabric-loader")
    }
}
