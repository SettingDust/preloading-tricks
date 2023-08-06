tasks {
    jar {
        manifest.attributes(
            "FMLModType" to "LIBRARY"
        )
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.1")
}
