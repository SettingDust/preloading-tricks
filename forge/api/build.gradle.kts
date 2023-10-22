plugins {
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom)
}

architectury {
    forge()
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn.mapping) {
        classifier("v2")
    })
    forge(libs.forge)

    implementation(project(":preloading-callbacks")) {
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
