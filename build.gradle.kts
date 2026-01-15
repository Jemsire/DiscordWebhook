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
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}