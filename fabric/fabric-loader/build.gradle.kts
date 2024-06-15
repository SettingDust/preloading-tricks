plugins {
    alias(libs.plugins.fabric.loom)
}

val mod_id: String by rootProject

loom {
    mods {
        register(mod_id) {
            sourceSet("main")
            sourceSet("main", project(":services"))
        }
    }

    runs {
        named("client") {
            ideConfigGenerated(true)
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
    modImplementation(libs.fabric.api)

    implementation(project(":services")) {
        isTransitive = false
    }

    include(implementation(project(":fabric:language-adapter"))!!)

    modRuntimeOnly(libs.modmenu) {
        exclude(module = "fabric-loader")
    }
}

tasks {
    ideaSyncTask {
        enabled = true
    }
}
