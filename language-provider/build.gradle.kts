repositories {
    maven {
        name = "Forge"
        url = uri("https://maven.minecraftforge.net/")
    }
}

dependencies {
    implementation(libs.forge.spi)
    implementation(project(":preloading-callbacks"))
}
