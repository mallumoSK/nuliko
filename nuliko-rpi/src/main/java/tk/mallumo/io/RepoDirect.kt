package tk.mallumo.io

import api.rc.*
import api.rc.extra.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import tk.mallumo.log.*

class RepoDirect : ImplRepo() {


    @OptIn(DelicateCoroutinesApi::class)
    override val scope = CoroutineScope(newFixedThreadPoolContext(2, "RepoDirect")) + Dispatchers.IO

    fun run() {
        scope.launch {
            while (isActive) {
                val registrationMessage = Message.Registration.create(Repository.appId, Repository.deviceId)
                clientSocket(Repository.connectorId).use {
                    it.webSocket(
                        method = HttpMethod.Get,
                        host = Constants.server.host,
                        port = Constants.server.port,
                        path = "/${Constants.Path.WS_REGISTRATION}"
                    ) {
                        send(Frame.Binary(true, registrationMessage.toProto()))

                        for (message in incoming) {
                            when (message) {
                                is Frame.Binary -> handleMessage(message)
                                else -> println("unknown message:${message::class.simpleName}")
                            }
                        }
                    }
                }
            }

        }
    }

    private suspend fun handleMessage(frame: Frame.Binary) {
        val data = frame.readBytes()
        try {
            val msg = RCMessage.decode(data)!!
            when (val content = msg.content) {
                is RCMessage.Content.StreamLiveStart -> Repository.onvif.streamLiveStart(msg.from, content.durationMs)
                is RCMessage.Content.StreamLiveStop -> Repository.onvif.streamLiveStop(msg.from)

                is RCMessage.Content.StreamHistoryStart ->  Repository.onvif.streamHistoryStart(msg.from,content.time, content.durationMs)
                is RCMessage.Content.StreamHistoryStop -> Repository.onvif.streamHistoryStop(msg.from)

                is RCMessage.Content.StreamHistoryAsk -> {
                    postContent(
                        target = msg.from,
                        content = RCMessage.Content.StreamHistoryAnswer(
                            Repository.diskManager.getHistoryStructure()
                        )
                    )
                }

                is RCMessage.Content.StreamData,
                is RCMessage.Content.StreamHistoryAnswer -> Unit
            }
        } catch (e: Exception) {
            try {
                logWARN(Message.fromProto(frame.readBytes()))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    suspend fun postVideoFrame(target: String, time: Int, bytes: ByteArray) {
        postContent(
            target = target,
            content = RCMessage.Content.StreamData(
                time = time,
                data = bytes
            )
        )
    }

    private suspend fun postContent(target: String, content: RCMessage.Content) {
        val msg = RCMessage(
            id = RCMessage.genID(Repository.connectorId),
            from = Repository.connectorId,
            to = target,
            content = content
        )

        postMessageFrame(msg)
    }

    private suspend fun postMessageFrame(msg: RCMessage) {
        val (targetApp, targetDev) = msg.to.split("_")
        val respond = clientDirect(Repository.connectorId).use {
            it.post(buildServerProtoMsgUrl(targetApp, targetDev)) {
                setBody(msg.toProtoBuff())
            }
        }
        logINFO(respond.status)
    }
}
