plugins {
    alias(libs.plugins.architectury.loom)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    forge(libs.forge)

    implementation(project(":preloading-callbacks"))

    include(project(":language-provider"))
    include(project(":preloading-callbacks"))
}
