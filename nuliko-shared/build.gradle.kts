plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val toolkit by lazy {
    Toolkit.get(extensions = extensions.extraProperties)
}

group = "tk.mallumo"
version = "unspecified"


kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    android()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                ktorClientJVM(toolkit)

            }
        }
        val androidMain by getting {
            dependencies {
                ktorClientAndroid(toolkit)
            }
        }

        val commonMain by getting {
            dependencies {
                ktorClient(toolkit)
                kotlinX(toolkit)
                mallumo(toolkit)
            }
        }
    }
}

android {
    compileSdkVersion(33)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
//        applicationId = "tk.mallumo.app"
        minSdkVersion(24)
        targetSdkVersion(33)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
