package tk.mallumo.nuliko

import kotlinx.coroutines.flow.*

fun <T> dataStateFlow(state: DataState<T>? = null): MutableStateFlow<DataState<T>> =
    MutableStateFlow(state ?: DataState.Idle())


fun <T> mutableStateFlowOf(default: T) = MutableStateFlow(default)


fun <T> referenceOf(body: () -> T): T = body()


