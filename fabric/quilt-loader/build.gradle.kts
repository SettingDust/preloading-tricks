plugins {
    alias(libs.plugins.quilt.loom)
    alias(libs.plugins.shadow)
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

    implementation(project(":services")) {
        isTransitive = false
    }

    shadow(runtimeOnly(project(":fabric:language-adapter")) {
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
