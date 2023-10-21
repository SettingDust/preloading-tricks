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
    minecraft(libs.minecraft.get1().get19())
    mappings(variantOf(libs.yarn.mapping.get1().get19()) {
        classifier("v2")
    })
    forge(libs.forge.get41())

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }

    include(implementation(project(":forge:language-provider"))!!)
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
