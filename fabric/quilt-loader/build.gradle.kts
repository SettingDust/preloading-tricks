plugins {
    alias(catalog.plugins.quilt.loom)
    alias(catalog.plugins.shadow)
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
    modImplementation(catalog.quilt.loader)

    implementation(project(":services")) {
        isTransitive = false
    }

    shadow(runtimeOnly(project(":fabric:language-adapter")) {
        exclude(module = "fabric-loader")
    }) {
        isTransitive = false
    }

    modRuntimeOnly(catalog.modmenu) {
        exclude(module = "fabric-loader")
    }
}

tasks {
    shadowJar {
        dependsOn(sourcesJar)
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier = "dev"
        destinationDirectory = layout.buildDirectory.dir("devlibs")
        exclude("fabric.mod.json")
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }
}
