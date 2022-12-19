package api.rc

import com.soywiz.krypto.AES
import com.soywiz.krypto.Padding
import api.rc.extra.Constants
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.protobuf.ProtoBuf
import tk.mallumo.utils.GenID

@OptIn(ExperimentalSerializationApi::class)
val protoSerializer = ProtoBuf {
    encodeDefaults = true
    serializersModule = SerializersModule {
        polymorphic(Message::class) {
            subclass(
                Message.Ping::class,
                Message.Ping.serializer()
            )
            subclass(
                Message.Registration::class,
                Message.Registration.serializer()
            )
            subclass(
                Message.Draw::class,
                Message.Draw.serializer()
            )
            subclass(
                Message.Confirm::class,
                Message.Confirm.serializer()
            )
            subclass(
                Message.RemoteControl::class,
                Message.RemoteControl.serializer()
            )
        }

        ///external


        polymorphic(RCMessage.Content::class) {
            subclass(
                RCMessage.Content.StreamLiveStart::class,
                RCMessage.Content.StreamLiveStart.serializer()
            )
            subclass(
                RCMessage.Content.StreamLiveStop::class,
                RCMessage.Content.StreamLiveStop.serializer()
            )
            subclass(
                RCMessage.Content.StreamHistoryAsk::class,
                RCMessage.Content.StreamHistoryAsk.serializer()
            )
            subclass(
                RCMessage.Content.StreamHistoryAnswer::class,
                RCMessage.Content.StreamHistoryAnswer.serializer()
            )
            subclass(
                RCMessage.Content.StreamHistoryStart::class,
                RCMessage.Content.StreamHistoryStart.serializer()
            )
            subclass(
                RCMessage.Content.StreamHistoryStop::class,
                RCMessage.Content.StreamHistoryStop.serializer()
            )
            subclass(
                RCMessage.Content.StreamData::class,
                RCMessage.Content.StreamData.serializer()
            )
            subclass(
                RCMessage.Content.Gpio::class,
                RCMessage.Content.Gpio.serializer()
            )
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Message> Message.Companion.fromProto(bytes: ByteArray): T =
    protoSerializer.decodeFromByteArray<Message>(bytes) as T

@OptIn(ExperimentalSerializationApi::class)
fun Message.toProto(): ByteArray =
    protoSerializer.encodeToByteArray(this)

fun Message.Registration.Companion.create(app: String, id: String) = Message.Registration(
    from = "${app}_$id",
    to = "api",
    id = generateMessageID(id, "api"),
    body = Message.Registration.Body(
        id = id,
        checksum = AES.encryptAesCbc(
            data = "${app}_$id".toByteArray(),
            key = Constants.AES_KEY,
            iv = ByteArray(16),
            padding = Padding.ISO10126Padding
        ).encodeBase64()
    )
)

fun generateMessageID(from: String, to: String): String = "${from}_${to}_${GenID.get()}"


fun RCMessage.Companion.decode(proto: ByteArray): RCMessage? {
    return RCMessage.fromProtoBuff(proto)
}

fun RCMessage.Companion.genID(deviceID: String) = "${GenID.get()}-$deviceID"

@OptIn(ExperimentalSerializationApi::class)
fun RCMessage.toProtoBuff(): ByteArray {
    return protoSerializer.encodeToByteArray(this)
}

@OptIn(ExperimentalSerializationApi::class)
internal fun RCMessage.Companion.fromProtoBuff(proto: ByteArray): RCMessage? {
    return runCatching {
        protoSerializer.decodeFromByteArray<RCMessage>(proto)
    }.getOrNull()
}
