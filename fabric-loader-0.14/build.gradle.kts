plugins {
    alias(libs.plugins.architectury.loom)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    implementation(libs.fabric.loader)
    implementation(project(":preloading-callbacks"))

    include(project(":language-adapter"))
    include(project(":preloading-callbacks"))
}
