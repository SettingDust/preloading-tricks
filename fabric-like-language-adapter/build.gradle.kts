plugins {
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom)
}

architectury {
    fabric()
}

dependencies {
    minecraft(libs.minecraft20)
    mappings(variantOf(libs.yarn.mapping20) {
        classifier("v2")
    })
    modImplementation(libs.fabric.loader)

    implementation(project(":preloading-callbacks")) {
        isTransitive = false
    }
}
