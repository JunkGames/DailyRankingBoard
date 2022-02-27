dependencies {
    implementation(kotlin("stdlib"))
    api("xyz.acrylicstyle.util:kotlin:0.16.6")
    api("xyz.acrylicstyle.util:yaml:0.16.6")
    api("xyz.acrylicstyle:minecraft-util:1.0.0")
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }
}
