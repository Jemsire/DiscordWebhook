plugins {
    id("java")
}

group = "com.jemsire"

val manifestFile = file("src/main/resources/manifest.json")
val version: String by lazy {
    if (manifestFile.exists()) {
        val manifestContent = manifestFile.readText()
        val versionRegex = """"Version"\s*:\s*"([^"]+)"""".toRegex()
        versionRegex.find(manifestContent)?.groupValues?.get(1) ?: "1.0.0"
    } else {
        "1.0.0"
    }
}
project.version = version

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "25"
    targetCompatibility = "25"
}

repositories {
    mavenCentral()
    maven {
        name = "hytale"
        url = uri("https://maven.hytale.com/release")
    }
}

dependencies {
    implementation("com.hypixel.hytale:Server:+")
    compileOnly(files("libs/JemPlaceholders-1.0.0.jar"))
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}