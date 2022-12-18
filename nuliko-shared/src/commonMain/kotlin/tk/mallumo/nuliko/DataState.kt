package tk.mallumo.nuliko

sealed class DataState<T>(val entry: T?) {
    class Idle<T>(entry: T? = null) : DataState<T>(entry)
    class Loading<T>(entry: T? = null) : DataState<T>(entry)
    class Result<T>(entry: T) : DataState<T>(entry) {
        @Suppress("unused", "SpellCheckingInspection")
        val nnvl
            get() = entry!!
    }

    class Error<T>(
        val message: String,
        val throwable: Throwable? = null,
        entry: T? = null
    ) : DataState<T>(entry)
}
