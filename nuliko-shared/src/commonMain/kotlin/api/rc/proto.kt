@file:OptIn(ExperimentalSerializationApi::class)

package api.rc

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
interface Message {

    companion object;

    @ProtoNumber(1)
    var id: String

    @ProtoNumber(2)
    var from: String

    @ProtoNumber(3)
    var to: String

    @Serializable
    data class Ping(
        override var from: String,
        override var to: String,
        override var id: String,
    ) : Message

    @Serializable
    data class Registration(
        override var from: String,
        override var to: String,
        override var id: String,
        @ProtoNumber(4)
        var body: Body,
    ) : Message {

        @Serializable
        data class Body(
            @ProtoNumber(5)
            var id: String,
            @ProtoNumber(6)
            var checksum: String
        )
    }

    @Serializable
    data class Confirm(
        override var from: String,
        override var to: String,
        override var id: String,
        @ProtoNumber(4)
        var body: Body,
    ) : Message {
        @Serializable
        data class Body(
            @ProtoNumber(5)
            var id: String,
            @ProtoNumber(6)
            var err: String = ""
        )
    }

    @Serializable
    data class RemoteControl(
        override var from: String,
        override var to: String,
        override var id: String,
        @ProtoNumber(4)
        var body: Body,
    ) : Message {
        @Serializable
        data class Body(
            @ProtoNumber(5)
            var mode: String
        )
    }

    @Serializable
    data class Draw(
        override var from: String,
        override var to: String,
        override var id: String,
        @ProtoNumber(4)
        var body: Body,
    ) : Message {
        @Serializable
        data class Body(
            @ProtoNumber(5)
            var id: String = "",
            @ProtoNumber(6)
            var width: Float = 0F,
            @ProtoNumber(7)
            var height: Float = 0F,
            @ProtoNumber(8)
            var reset: Boolean = false,
            @ProtoNumber(9)
            var lines: List<Line> = listOf()
        ) {

            @Serializable
            data class Line(
                @ProtoNumber(10)
                var id: String,
                @ProtoNumber(11)
                var time: Long,
                @ProtoNumber(12)
                var color: Long,
                @ProtoNumber(13)
                var width: Float,
                @ProtoNumber(14)
                var path: List<Point>
            )

            @Serializable
            data class Point(
                @ProtoNumber(15)
                var x: Float,
                @ProtoNumber(16)
                var y: Float
            )
        }
    }
}

@Serializable
data class RCMessage(
    @ProtoNumber(1)
    val id: String = "",
    @ProtoNumber(2)
    var from: String = "",
    @ProtoNumber(3)
    var to: String = "",
    @ProtoNumber(4)
    var content: Content
) {

    companion object;

    sealed interface Content {

        @Serializable
        class StreamData(
            @ProtoNumber(5)
            val time: Int,
            @ProtoNumber(6)
            val data: ByteArray
        ) : Content {
            companion object
        }

        @Serializable
        class Gpio(
            @ProtoNumber(5)
            val id: Int,
            @ProtoNumber(6)
            val state: Boolean,
            @ProtoNumber(7)
            val durationMs: Int,
        ) : Content {
            companion object
        }

        @Serializable
        class StreamLiveStart(
            @ProtoNumber(5)
            val durationMs: Int
        ) : Content {
            companion object
        }

        @Serializable
        class StreamLiveStop : Content {
            companion object
        }

        @Serializable
        class StreamHistoryAsk: Content {
            companion object
        }

        @Serializable
        class StreamHistoryAnswer(
            @ProtoNumber(5)
            val items:List<Item>
        ) : Content {
            companion object

            @Serializable
            class Item(
                @ProtoNumber(6)
                var day:String,
                @ProtoNumber(7)
                var first:String,
                @ProtoNumber(8)
                var last:String
            )
        }

        @Serializable
        class StreamHistoryStart(
            @ProtoNumber(5)
            val time: String,
            @ProtoNumber(6)
            val durationMs: Long
        ) : Content {
            companion object
        }

        @Serializable
        class StreamHistoryStop : Content {
            companion object
        }
    }
}
