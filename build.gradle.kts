group = "net.azisaba.dailyrankingboard"
version = "2.1.2"

plugins {
    java
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    `maven-publish`
    `java-library`
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.acrylicstyle.xyz/repository/maven-public/") }
    maven { url = uri("https://libraries.minecraft.net/") }
}

subprojects {
    group = parent!!.group
    version = parent!!.version

    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.github.johnrengelman.shadow")
        plugin("maven-publish")
        plugin("java-library")
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://repo.acrylicstyle.xyz/repository/maven-public/") }
        maven { url = uri("https://libraries.minecraft.net/") }
    }
}

allprojects {
    publishing {
        repositories {
            maven {
                name = "repo"
                credentials(PasswordCredentials::class)
                url = uri(
                    if (project.version.toString().endsWith("SNAPSHOT"))
                        project.findProperty("deploySnapshotURL") ?: System.getProperty("deploySnapshotURL", "")
                    else
                        project.findProperty("deployReleasesURL") ?: System.getProperty("deployReleasesURL", "")
                )
            }
        }

        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }
}
