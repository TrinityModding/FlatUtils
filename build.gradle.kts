plugins {
    id("java")
}

group = "me.hydos.flatutils"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.intellij:forms_rt:7.0.3")
    implementation("com.github.weisj:darklaf-core:3.0.2")

    implementation(platform("org.lwjgl:lwjgl-bom:3.3.2"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-nfd")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows")
    runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-windows")
}