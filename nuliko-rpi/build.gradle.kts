plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

val toolkit by lazy {
    Toolkit.get(extensions = extensions.extraProperties)
}

group = "tk.mallumo"
version = toolkit["version.nuliko.rpi"]


dependencies {
    mallumo(toolkit)
    kotlinX(toolkit)
    ktorClientJVM(toolkit)

    /*cam tools*/
    implementation("com.github.03:onvif:1.0.9")
    implementation("com.burgstaller:okhttp-digest:2.5")
    implementation(project(":nuliko-shared"))



}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

application {
    mainClass.set("tk.mallumo.MainKt")
}
