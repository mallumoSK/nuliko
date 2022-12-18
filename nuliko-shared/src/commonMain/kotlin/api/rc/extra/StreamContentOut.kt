package api.rc.extra

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.File


class StreamContentOut(private val file: File) : OutgoingContent.WriteChannelContent() {
    override suspend fun writeTo(channel: ByteWriteChannel) {
        channel.toOutputStream().use {
            file.inputStream()
                .buffered()
                .copyTo(it, DEFAULT_BUFFER_SIZE)
//            it.flush()
//            it.close()
        }

    }

    override val contentType = ContentType.Application.Any
    override val contentLength: Long = file.length()
}
