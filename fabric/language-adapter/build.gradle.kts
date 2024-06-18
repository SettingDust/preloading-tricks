plugins {
    alias(catalog.plugins.fabric.loom)
}

dependencies {
    minecraft(catalog.minecraft.fabric)
    mappings(variantOf(catalog.mapping.yarn) {
        classifier("v2")
    })
    modImplementation(catalog.fabric.loader)

    implementation(project(":services")) {
        isTransitive = false
    }
}
