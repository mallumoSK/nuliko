import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*

fun DependencyHandlerScope.mallumo(toolkit: Toolkit) = mallumoInternal(toolkit).forEach(::implementation)
fun KotlinDependencyHandler.mallumo(toolkit: Toolkit) = mallumoInternal(toolkit).forEach(::implementation)

private fun mallumoInternal(toolkit: Toolkit): List<String> = listOf(
    "tk.mallumo:utils:${toolkit["version.utils"]}",
    "tk.mallumo:log:${toolkit["version.log"]}",
    "com.google.code.gson:gson:2.10"
)
