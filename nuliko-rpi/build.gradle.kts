import kotlin.concurrent.*

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
    val src = file("/opt/GitHub/nuliko/nuliko-rpi/build/libs/nuliko-rpi-1.0.0-all.jar")
    doLast {
        exe("scp ${src.absolutePath} marian@devices2.tapgest.com:/var/www/android/tools/tmp/nk.jar")
    }
}

tasks["deploy"].mustRunAfter("shadowJar")

fun exe(cmd: String) {
    fun java.io.BufferedReader.lineByLine(onNewLine: (String) -> Unit) {
        use {
            while (true) {
                val line = it.readLine() ?: break
                onNewLine(line)
            }
        }
    }
    println()
    println(cmd)
    Runtime.getRuntime()
        .exec(arrayOf("sh", "-c", cmd))
        .apply {
            val input = inputStream.bufferedReader()
            val errput = errorStream.bufferedReader()
            thread {
                runCatching {
                    input.lineByLine {
                        println(it)
                    }
                }
                runCatching {
                    errput.lineByLine {
                        System.err.println(it)
                    }
                }
            }
            println("state ${waitFor()}")
        }
}
