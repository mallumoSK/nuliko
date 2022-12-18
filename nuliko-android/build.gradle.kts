plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
}

val toolkit by lazy {
    Toolkit.get(extensions = extensions.extraProperties)
}

android {
    namespace = "tk.mallumo.nuliko.android"
    compileSdk = 33

    defaultConfig {
        applicationId = "tk.mallumo.nuliko.android"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = toolkit["version.compose.android.compiller"]
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    sourceSets {
        getByName("debug") {
            java.srcDirs(
//                "src/main/java",
                "build/generated/ksp/debug/kotlin",
            )
        }
        getByName("release") {
            java.srcDirs(
//                "src/main/java",
                "build/generated/ksp/release/kotlin",
            )
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:${toolkit["version.compose.android.activity"]}")
    implementation("androidx.compose.ui:ui:${toolkit["version.compose.android"]}")
    implementation("androidx.compose.runtime:runtime:${toolkit["version.compose.android"]}")
    implementation("androidx.compose.ui:ui-tooling-preview:${toolkit["version.compose.android"]}")
    implementation("androidx.compose.material:material:1.3.1")
//    debugImplementation("androidx.compose.ui:ui-tooling:${toolkit["version.compose.android"]}")
//    debugImplementation("androidx.compose.ui:ui-test-manifest:${toolkit["version.compose.android"]}")

    implementation("tk.mallumo:navigation:${toolkit["version.navigation"]}")
    ksp("tk.mallumo:navigation-ksp:${toolkit["version.navigation"]}")

    /**/

    ktorClientAndroid(toolkit)
    kotlinXandroid(toolkit)
    implementation(project(":nuliko-shared"))
    /**/
    mallumo(toolkit)
//    implementation("tk.mallumo:utils:${toolkit["version.utils"]}")
//    implementation("tk.mallumo:log:${toolkit["version.log"]}")
//    implementation("com.google.code.gson:gson:2.10")

//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${toolkit["version.coroutines"]}")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${toolkit["version.serialization"]}")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${toolkit["version.serialization"]}")
/**/
}
ksp.arg("child", "player controller")
