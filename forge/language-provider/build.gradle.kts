plugins {
    alias(catalog.plugins.architectury)
    alias(catalog.plugins.architectury.loom)
}

architectury {
    forge()
}

dependencies {
    minecraft(catalog.minecraft.fabric)
    mappings(variantOf(catalog.mapping.yarn) {
        classifier("v2")
    })
    forge(catalog.forge)

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }
}

tasks {
    jar {
        manifest.attributes(
            "FMLModType" to "LANGPROVIDER"
        )
    }
}
