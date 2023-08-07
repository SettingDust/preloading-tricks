plugins {
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom)
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val mod_id: String by rootProject

loom {
    mods {
        register(mod_id) {
            sourceSet("main")
            sourceSet("main", project(":preloading-callbacks"))
            modFiles.from(project(":preloading-callbacks").tasks.jar.get().archiveFile)
        }
    }
}

repositories {
    maven("https://maven.terraformersmc.com/releases")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    modImplementation(libs.fabric.loader)

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }

    runtimeOnly(project(":fabric-like-language-adapter"))
    include(project(":fabric-like-language-adapter"))

    modRuntimeOnly(libs.modmenu) {
        exclude(module = "fabric-loader")
    }
}
