repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    implementation(libs.fabric.loader)
    implementation(project(":preloading-callbacks"))
}
