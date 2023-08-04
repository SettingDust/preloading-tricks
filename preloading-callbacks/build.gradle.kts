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

publishing {
    publications {
        create<MavenPublication>("preloading-callbacks") {
            groupId = "${rootProject.group}"
            artifactId = "preloading-callbacks"
            version = "${rootProject.version}"
            from(components.getByName("java"))
        }
    }
}
