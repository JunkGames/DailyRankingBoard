group = parent!!.group
version = parent!!.version

dependencies {
    implementation(project(":api"))
    implementation("xyz.acrylicstyle:java-util-kotlin:0.15.4")
    implementation("xyz.acrylicstyle:minecraft-util:0.5.3")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }

    test {
        useJUnitPlatform()
    }

    shadowJar {
        dependencies {
            exclude("com.reflections:reflections")
        }

        relocate("kotlin", "xyz.acrylicstyle.dailyranking.plugin.libs.kotlin")
        relocate("util", "xyz.acrylicstyle.dailyranking.plugin.libs.util")

        minimize()
    }

    withType<org.gradle.jvm.tasks.Jar> {
        archiveFileName.set("DailyRankingBoard-${parent?.version}.jar")
    }
}
