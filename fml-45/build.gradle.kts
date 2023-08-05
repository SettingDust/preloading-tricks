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
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    forge(libs.forge)

    implementation(project(":preloading-callbacks"))

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
