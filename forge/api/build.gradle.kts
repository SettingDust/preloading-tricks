plugins {
    alias(catalog.plugins.forge.gradle)
    alias(catalog.plugins.librarian.forgegradle)
    alias(catalog.plugins.mixin)
}

minecraft {
    mappings(
        "parchment", "${catalog.versions.parchmentmc.asProvider().get()}-${catalog.versions.minecraft.get()}")
}

dependencies {
    minecraft(catalog.minecraft.forge)
    annotationProcessor(variantOf(catalog.mixin) { classifier("processor") })

    implementation(project(":services")) {
        isTransitive = false
    }
}

tasks {
    jar {
        manifest.attributes(
            "FMLModType" to "LIBRARY"
        )
    }
}
