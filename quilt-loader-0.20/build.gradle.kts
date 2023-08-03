plugins {
    alias(libs.plugins.architectury.loom)
}

repositories {
    maven {
        name = "Quilt"
        url = uri("https://maven.quiltmc.org/repository/release")
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    implementation(libs.quilt.loader)
    implementation(project(":preloading-callbacks"))

    include(project(":language-adapter"))
    include(project(":preloading-callbacks"))
}
