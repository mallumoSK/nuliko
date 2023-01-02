package tk.mallumo.nuliko.android.io

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import api.rc.RCMessage
import api.rc.extra.Constants
import api.rc.genID
import kotlinx.coroutines.*
import tk.mallumo.nuliko.DataState
import tk.mallumo.nuliko.mutableStateFlowOf
import tk.mallumo.utils.minute

class RepoPlayer(scope: CoroutineScope) : ImplRepo(scope) {

    private val liveFrame = mutableStateFlowOf<DataState<ImageBitmap>>(DataState.Idle())
    private var playerTimeout: Job? = null

    @Composable
    fun collect() = liveFrame.collectAsState()

    fun start(camId: Int) {
        liveFrame.value = DataState.Loading()
        // info to server
        val duration = 15.minute

        playerTimeout?.cancel()
        playerTimeout = scope.launch {
            delay(duration)
            if (isActive) stop(camId)
        }
        val connectorId = Constants.Android.connectorId(Repository.deviceId)
        val msg = RCMessage(
            id = RCMessage.genID(connectorId),
            from = connectorId,
            to = Constants.Rpi.connectorId(Constants.Rpi.deviceIdDefault),
            content = RCMessage.Content.StreamLiveStart(
                camId = camId,
                durationMs = 15.minute.toInt()
            )
        )
        Repository.direct.send(msg)
    }

    fun stop(camId: Int, resetImage: Boolean = false) {
        if (liveFrame.value !is DataState.Idle) {
            if (resetImage) {
                liveFrame.value = DataState.Idle()
            } else {
                liveFrame.value = DataState.Idle(liveFrame.value.entry)
            }

            val connectorId = Constants.Android.connectorId(Repository.deviceId)
            val msg = RCMessage(
                id = RCMessage.genID(connectorId),
                from = connectorId,
                to = Constants.Rpi.connectorId(Constants.Rpi.deviceIdDefault),
                content = RCMessage.Content.StreamLiveStop(camId)
            )
            Repository.direct.send(msg)
        }

    }

    fun post(data: ByteArray) {
        if (liveFrame.value !is DataState.Idle) {
            liveFrame.value = DataState.Result(BitmapFactory.decodeByteArray(data, 0, data.size).asImageBitmap())
        }
    }
}
