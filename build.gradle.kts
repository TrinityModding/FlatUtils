plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}

group = "me.hydos.flatutils"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    shadow(implementation("com.intellij:forms_rt:7.0.3")!!)
    shadow(implementation("com.github.weisj:darklaf-core:3.0.2")!!)

    shadow(implementation(platform("org.lwjgl:lwjgl-bom:3.3.2"))!!)

    shadow(implementation("org.lwjgl", "lwjgl"))
    shadow(implementation("org.lwjgl", "lwjgl-nfd"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-windows"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-windows"))
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "me.hydos.flatutils.Main"
    }
}
