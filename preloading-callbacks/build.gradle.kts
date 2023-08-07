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
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
}
