plugins {
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom)
}

architectury {
    fabric()
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    modImplementation(libs.fabric.loader)

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }
}
