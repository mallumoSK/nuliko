import org.gradle.api.plugins.*

class Toolkit private constructor(private val properties: ExtraPropertiesExtension) {
    companion object {
        private lateinit var instance: Toolkit
        fun get(extensions: ExtraPropertiesExtension): Toolkit {
            if (!::instance.isInitialized) {
                instance = Toolkit(extensions)
            }
            return instance
        }
    }

    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String = get(property.name)

    operator fun get(key: String): String = (
            properties.runCatching { get(key) }.getOrNull()
                ?: properties.runCatching { get(key.replace("_", ".")) }.getOrNull()
                ?: properties.runCatching { get(key.replace(".", "_")) }.getOrNull())?.toString()!!

}

fun org.gradle.kotlin.dsl.DependencyHandlerScope.ktorServer(toolkit: Toolkit) {
    val ktorV = toolkit["version.ktor"]

    implementation("io.ktor:ktor-server-core-jvm:$ktorV")
//    implementation("io.ktor:ktor-server-jetty:$ktorV")
    implementation("io.ktor:ktor-server-netty:$ktorV")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorV")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktorV")
    implementation("io.ktor:ktor-server-partial-content:$ktorV")
    implementation("io.ktor:ktor-server-cors:$ktorV")
    implementation("io.ktor:ktor-network-tls-certificates:$ktorV")
    implementation("io.ktor:ktor-server-websockets:$ktorV")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorV")

    implementation("io.ktor:ktor-server-auth:$ktorV")
    implementation("com.soywiz.korlibs.krypto:krypto-jvm:${toolkit["version.krypto"]}")

    implementation("ch.qos.logback:logback-classic:${toolkit["version.logback"]}")
}

fun org.gradle.kotlin.dsl.DependencyHandlerScope.ktorClientJVM(toolkit: Toolkit) {
    val ktorV = toolkit["version.ktor"]
    implementation("io.ktor:ktor-client-cio:$ktorV")
    ktorClient(toolkit)
}
fun org.gradle.kotlin.dsl.DependencyHandlerScope.ktorClientAndroid(toolkit: Toolkit) {
    val ktorV = toolkit["version.ktor"]
    implementation("io.ktor:ktor-client-android:$ktorV")
    ktorClient(toolkit)
}

fun org.gradle.kotlin.dsl.DependencyHandlerScope.ktorClient(toolkit: Toolkit) {
    val ktorV = toolkit["version.ktor"]

    implementation("io.ktor:ktor-client-core:$ktorV")
    implementation("io.ktor:ktor-client-auth:$ktorV")
    implementation("io.ktor:ktor-client-encoding:$ktorV")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorV")

    implementation("com.soywiz.korlibs.krypto:krypto:${toolkit["version.krypto"]}")
}

fun org.gradle.kotlin.dsl.DependencyHandlerScope.kotlinX(toolkit: Toolkit) {
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:${toolkit["version.coroutines"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${toolkit["version.serialization"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${toolkit["version.serialization"]}")
}

fun org.gradle.kotlin.dsl.DependencyHandlerScope.mallumo(toolkit: Toolkit) {
    implementation("tk.mallumo:utils:${toolkit["version.utils"]}")
    implementation("tk.mallumo:log:${toolkit["version.log"]}")
    implementation("com.google.code.gson:gson:2.10")

}

private fun org.gradle.kotlin.dsl.DependencyHandlerScope.implementation(scope: String) {
    add("implementation", scope)
}
