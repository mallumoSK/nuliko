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
    implementation(files("/opt/GitHub/me/nuliko/nuliko-rpi/src/libs/opencv-470.jar"))
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

tasks.create("deploy") {
    group = "application"
    val src = file("/opt/GitHub/me/nuliko/nuliko-rpi/build/libs/nuliko-rpi-$version-all.jar")
    doLast {//192.168.100.146  nuliko.local
        exe("scp ${src.absolutePath} pi@nuliko.local:/opt/nuliko/nuliko-rpi.jar")
    }
}

tasks["deploy"].mustRunAfter("shadowJar")
tasks["deploy"].dependsOn("shadowJar")


