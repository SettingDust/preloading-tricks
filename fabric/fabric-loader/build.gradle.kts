plugins {
    alias(catalog.plugins.fabric.loom)
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

dependencies {
    minecraft(catalog.minecraft.fabric)
    mappings(variantOf(catalog.mapping.yarn) {
        classifier("v2")
    })
    modImplementation(catalog.fabric.loader)
    modImplementation(catalog.fabric.api)

    implementation(project(":services")) {
        isTransitive = false
    }

    include(implementation(project(":fabric:language-adapter"))!!)

    modRuntimeOnly(catalog.modmenu) {
        exclude(module = "fabric-loader")
    }
}

tasks {
    ideaSyncTask {
        enabled = true
    }
}
