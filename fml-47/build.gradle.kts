plugins {
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom)
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
    minecraft(libs.minecraft20)
    mappings(variantOf(libs.yarn.mapping20) {
        classifier("v2")
    })
    forge(libs.forge47)

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }

    implementation(project(":forge-language-provider"))
    include(project(":forge-language-provider"))
}

tasks {
    classes {
        finalizedBy(jar)
    }

    jar {
        manifest.attributes(
            "FMLModType" to "LIBRARY"
        )
    }
}
