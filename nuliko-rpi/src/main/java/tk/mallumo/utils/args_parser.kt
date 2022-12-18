package tk.mallumo

import io.ktor.http.*
import java.io.File
import kotlin.reflect.KProperty

fun Array<String>.getArgsParamString(key: String, default: String): String =
    getArgsParam(key).let {
        if (it == null && isEmpty()) default
        else it ?: error("string param '$key' not defined")
    }

fun Array<String>.getArgsParamInt(key: String, default: Int): Int =
    getArgsParam(key)?.toIntOrNull().let {
        if (it == null && isEmpty()) default
        else it ?: error("int param '$key' not defined")
    }

fun Array<String>.getArgsParamFile(key: String, default: File): File =
    getArgsParam(key).let {
        if (((it == null || !File(it).exists()) && isEmpty())) default
        else if (it.isNullOrEmpty()) error("path param '$key' not defined")
        else if (!File(it).exists()) error("path param '$key', path not exists")
        else File(it)
    }

fun Array<String>.getArgsParam(key: String): String? =
    indexOfFirst { it == key }
        .takeIf { it > -1 }
        ?.let { getOrNull(it + 1) }


inline operator fun <reified V> Parameters.getValue(nothing: Nothing?, property: KProperty<*>): V? = when {
    V::class == Int::class -> (this[property.name]?.toIntOrNull() as? V)
    V::class == String::class -> (this[property.name] as? V)
    else -> null
}

