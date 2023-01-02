package tk.mallumo.io

import api.rc.RCMessage
import api.rc.extra.Constants
import api.rc.genID
import api.rc.toProtoBuff
import io.ktor.client.request.*
import kotlinx.coroutines.*
import tk.mallumo.isDebug
import tk.mallumo.nuliko.runConnector

class RepoDirect : ImplRepo() {

    companion object {
        val deviceId: String by lazy {
            if (!isDebug) "RPI4x2"
            else "RPI4xDEBUG"
        }


    }

    override val scope = CoroutineScope(CoroutineName("RepoDirect")) + Dispatchers.IO

    fun run() {
        scope.launch {
            runConnector(Constants.Rpi.appId, deviceId, ::handleMessage)
        }
    }

    private suspend fun handleMessage(msg: RCMessage) {
        when (val content = msg.content) {
            is RCMessage.Content.StreamLiveStart -> Repository.onvif.streamLiveStart(
                id = content.camId,
                from = msg.from,
                durationMs = content.durationMs
            )

            is RCMessage.Content.StreamLiveStop -> Repository.onvif.streamLiveStop(
                id = content.camId,
                from = msg.from
            )

            is RCMessage.Content.StreamHistoryStart -> Repository.onvif.streamHistoryStart(
                id = content.camId,
                target = msg.from,
                time = content.time,
                durationMs = content.durationMs
            )

            is RCMessage.Content.StreamHistoryStop -> Repository.onvif.streamHistoryStop(msg.from)

            is RCMessage.Content.StreamHistoryAsk -> {
                postContent(
                    target = msg.from,
                    content = RCMessage.Content.StreamHistoryAnswer(
                        items = Repository.diskManager.getHistoryStructure(content.camId)
                    )
                )
            }

            is RCMessage.Content.StreamData,
            is RCMessage.Content.StreamHistoryAnswer -> Unit

            is RCMessage.Content.Gpio -> {
                Repository.gpio.pinSetup(content.id, content.state, content.durationMs)
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

    private suspend fun postContent(target: String, content: RCMessage.Content): Boolean {
        val msg = RCMessage(
            id = RCMessage.genID(Constants.Rpi.connectorId(deviceId)),
            from = Constants.Rpi.connectorId(deviceId),
            to = target,
            content = content
        )

        return postMessageFrame(msg)
    }

    private suspend fun postMessageFrame(msg: RCMessage): Boolean {
        val (targetApp, targetDev) = msg.to.split("_")
        return Constants.clientDirect(msg.from).use {
            it.post(Constants.buildServerProtoMsgUrl(targetApp, targetDev)) {
                setBody(msg.toProtoBuff())
            }.status.value == 200
        }
    }
}
