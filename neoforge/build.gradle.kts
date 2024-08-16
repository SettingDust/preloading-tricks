subprojects {
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

        withSourcesJar()

        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}
