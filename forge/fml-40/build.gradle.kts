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
    minecraft(libs.minecraft.get1().get18().get2())
    mappings(variantOf(libs.yarn.mapping.get1().get18().get2()) {
        classifier("v2")
    })
    forge(libs.forge.get40())

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }

    include(implementation(project(":forge:language-provider"))!!)
    include(implementation(project(":forge:api"))!!)
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
