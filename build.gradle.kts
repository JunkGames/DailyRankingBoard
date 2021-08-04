group = "xyz.acrylicstyle.dailyrankingboard"
version = "1.1.11"

plugins {
    java
    kotlin("jvm") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo2.acrylicstyle.xyz") }
    maven { url = uri("https://libraries.minecraft.net/") }
}

subprojects {
    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.github.johnrengelman.shadow")
        plugin("maven-publish")
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    val javaComponent = components["java"] as AdhocComponentWithVariants
    javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
        skip()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://repo2.acrylicstyle.xyz") }
        maven { url = uri("https://libraries.minecraft.net/") }
    }
}
