import kotlin.concurrent.thread

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