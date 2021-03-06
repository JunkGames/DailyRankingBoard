dependencies {
    api(project(":api"))
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("org.spigotmc:spigot:1.17.1-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot:1.17.1-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
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
