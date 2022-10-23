plugins {
    java
}

group "me.aleksilassila.islands"
version "5.0.9"

repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.jetbrains:annotations:20.1.0")

    compileOnly("org.spigotmc:spigot-api:1.15.1-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.0-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.TechFortress:GriefPrevention:16.18")
    compileOnly("com.github.stefvanschie.inventoryframework:IF:0.10.7")
}

tasks.register<Copy>("copyJar") {
    from(layout.buildDirectory.file("libs/${project.name}.jar"))
    into("server/plugins")
}

tasks.named("build") {
    dependsOn("copyJar")
}
