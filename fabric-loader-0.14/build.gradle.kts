plugins {
    alias(libs.plugins.architectury.loom)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.yarn.mapping)

    implementation(libs.fabric.loader)
    implementation(project(":preloading-callbacks"))

    include(project(":language-adapter"))
    include(project(":preloading-callbacks"))
}
