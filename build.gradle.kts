plugins {
    id("java")
}

group = "com.jemsire"

// Read version from manifest.json
val manifestFile = file("src/main/resources/manifest.json")
val version: String by lazy {
    if (manifestFile.exists()) {
        val manifestContent = manifestFile.readText()
        // Simple regex to extract version from JSON
        val versionRegex = """"Version"\s*:\s*"([^"]+)"""".toRegex()
        versionRegex.find(manifestContent)?.groupValues?.get(1) ?: "1.0.0"
    } else {
        "1.0.0" // fallback version
    }
}
project.version = version

repositories {
    mavenCentral()
    maven {
        name = "hytale"
        url = uri("https://maven.hytale.com/release") // Or "hytale-pre-release" for pre-release versions
    }
}

dependencies {
    implementation("com.hypixel.hytale:Server:+")
    //compileOnly(files("libs/JemPlaceholders-1.0.0.jar"))
}