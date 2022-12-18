import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*

fun DependencyHandlerScope.ktorServer(toolkit: Toolkit) {
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

fun DependencyHandlerScope.ktorClientJVM(toolkit: Toolkit) = ktorClientJVMInternal(toolkit).forEach(::implementation)
fun KotlinDependencyHandler.ktorClientJVM(toolkit: Toolkit) = ktorClientJVMInternal(toolkit).forEach(::implementation)

private fun ktorClientJVMInternal(toolkit: Toolkit): List<String> =ktorClientInternal(toolkit) +
    "io.ktor:ktor-client-cio:${toolkit["version.ktor"]}"

fun DependencyHandlerScope.ktorClientAndroid(toolkit: Toolkit) = ktorClientAndroidInternal(toolkit).forEach(::implementation)
fun KotlinDependencyHandler.ktorClientAndroid(toolkit: Toolkit) = ktorClientAndroidInternal(toolkit).forEach(::implementation)

private fun ktorClientAndroidInternal(toolkit: Toolkit): List<String> =ktorClientInternal(toolkit) +
    "io.ktor:ktor-client-android:${toolkit["version.ktor"]}"

fun DependencyHandlerScope.ktorClient(toolkit: Toolkit) = ktorClientInternal(toolkit).forEach(::implementation)
fun KotlinDependencyHandler.ktorClient(toolkit: Toolkit) = ktorClientInternal(toolkit).forEach(::implementation)

private fun ktorClientInternal(toolkit: Toolkit) = listOf(
    "io.ktor:ktor-client-cio:${toolkit["version.ktor"]}",
    "io.ktor:ktor-client-core:${toolkit["version.ktor"]}",
    "io.ktor:ktor-client-auth:${toolkit["version.ktor"]}",
    "io.ktor:ktor-client-encoding:${toolkit["version.ktor"]}",
    "io.ktor:ktor-client-content-negotiation:${toolkit["version.ktor"]}",
    "com.soywiz.korlibs.krypto:krypto:${toolkit["version.krypto"]}"
)
