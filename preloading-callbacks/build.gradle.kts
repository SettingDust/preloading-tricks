plugins {
    `maven-publish`
}

tasks {
    jar {
        manifest.attributes(
            "FMLModType" to "LIBRARY"
        )
    }
}
