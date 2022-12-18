package tk.mallumo.nuliko.android.io

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import api.rc.*
import api.rc.extra.*
import kotlinx.coroutines.*
import tk.mallumo.nuliko.*
import tk.mallumo.utils.*

class RepoPlayer(scope: CoroutineScope) : ImplRepo(scope) {

    private val liveFrame = mutableStateFlowOf<DataState<ImageBitmap>>(DataState.Idle())
    private var playerTimeout: Job? = null

    @Composable
    fun collect() = liveFrame.collectAsState()

    fun start() {
        liveFrame.value = DataState.Loading()
        // info to server
        val duration = 15.minute

        playerTimeout?.cancel()
        playerTimeout = scope.launch {
            delay(duration)
            if (isActive) stop()
        }
        val connectorId = Constants.Android.connectorId(Repository.deviceId)
        val msg = RCMessage(
            id = RCMessage.genID(connectorId),
            from = connectorId,
            to = Constants.Rpi.connectorId(),
            content = RCMessage.Content.StreamLiveStart(15.minute.toInt())
        )
        Repository.direct.send(msg)
    }

    fun stop() {
        if (liveFrame.value !is DataState.Idle) {
            liveFrame.value = DataState.Idle()

            val connectorId = Constants.Android.connectorId(Repository.deviceId)
            val msg = RCMessage(
                id = RCMessage.genID(connectorId),
                from = connectorId,
                to = Constants.Rpi.connectorId(),
                content = RCMessage.Content.StreamLiveStop()
            )
            Repository.direct.send(msg)
        }

    }

    fun post(data: ByteArray) {
        if(liveFrame.value !is DataState.Idle){
            liveFrame.value = DataState.Result(BitmapFactory.decodeByteArray(data, 0, data.size).asImageBitmap())
        }
    }
}
