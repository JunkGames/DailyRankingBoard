group = parent!!.group
version = parent!!.version

dependencies {
    implementation(kotlin("stdlib"))
    implementation("xyz.acrylicstyle:java-util-kotlin:0.15.0")
    implementation("xyz.acrylicstyle:minecraft-util:0.3.2")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }
}
