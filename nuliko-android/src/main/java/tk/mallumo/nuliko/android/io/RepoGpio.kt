package tk.mallumo.nuliko.android.io

import api.rc.RCMessage
import api.rc.extra.Constants
import api.rc.genID
import api.rc.toProtoBuff
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tk.mallumo.log.logINFO
import kotlin.time.Duration

class RepoGpio(scope: CoroutineScope) : ImplRepo(scope) {

    fun start(id: Int, duration: Duration) {
        send(
            RCMessage.Content.Gpio(
                id = id,
                state = true,
                durationMs = duration.inWholeMilliseconds.toInt()
            )
        )
    }

    fun stop(id: Int) {
        send(
            RCMessage.Content.Gpio(
                id = id,
                state = false,
                durationMs = 0
            )
        )

    }

    private fun send(msg: RCMessage.Content) {
        scope.launch {
            try {
                val connectorId = Constants.Android.connectorId(Repository.deviceId)
                postMessageFrame(
                    RCMessage(
                        id = RCMessage.genID(connectorId),
                        from = connectorId,
                        to = Constants.Rpi.connectorId(Constants.Rpi.deviceIdDefault),
                        content = msg
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun postMessageFrame(msg: RCMessage) {
        val (targetApp, targetDev) = msg.to.split("_")
        val respond = Constants.clientDirect(msg.from).use {
            it.post(Constants.buildServerProtoMsgUrl(targetApp, targetDev)) {
                setBody(msg.toProtoBuff())
            }
        }
        logINFO(respond.status)
    }
}
