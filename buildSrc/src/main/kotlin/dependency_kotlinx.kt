import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*


fun DependencyHandlerScope.kotlinX(toolkit: Toolkit) = kotlinXInternal(toolkit).forEach(::implementation)
fun KotlinDependencyHandler.kotlinX(toolkit: Toolkit) = kotlinXInternal(toolkit).forEach(::implementation)

fun DependencyHandlerScope.kotlinXandroid(toolkit: Toolkit) = kotlinXandroidInternal(toolkit).forEach(::implementation)
fun KotlinDependencyHandler.kotlinXandroid(toolkit: Toolkit) = kotlinXandroidInternal(toolkit).forEach(::implementation)


private fun kotlinXandroidInternal(toolkit: Toolkit) = kotlinXInternal(toolkit) +
    "org.jetbrains.kotlinx:kotlinx-coroutines-android:${toolkit["version.coroutines"]}"


private fun kotlinXInternal(toolkit: Toolkit) = listOf(
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:${toolkit["version.coroutines"]}",
    "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${toolkit["version.serialization"]}",
    "org.jetbrains.kotlinx:kotlinx-serialization-json:${toolkit["version.serialization"]}"
)

