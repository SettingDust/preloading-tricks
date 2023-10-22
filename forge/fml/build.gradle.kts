plugins {
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom)
    alias(libs.plugins.shadow)
}

architectury {
    platformSetupLoomIde()
    forge()
}

val mod_id: String by rootProject

loom {
    mods {
        named("main") {
            modSourceSets.empty()
            modFiles.setFrom(tasks.jar)
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    forge(libs.forge)

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }

    include(implementation(project(":forge:language-provider"))!!)
    shadow(implementation(project(":forge:api")) {
        isTransitive = false
    })
}

tasks {
    shadowJar {
        dependsOn(sourcesJar)
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier = "dev"
        destinationDirectory = layout.buildDirectory.dir("devlibs")
    }
    classes {
        finalizedBy(jar)
    }

    jar {
        manifest.attributes(
            "FMLModType" to "LIBRARY"
        )
    }
}
