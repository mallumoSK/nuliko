package tk.mallumo.nuliko.android.service

//import api.rc.*

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import api.rc.RCMessage
import api.rc.extra.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tk.mallumo.log.logERROR
import tk.mallumo.nuliko.android.io.Repository
import tk.mallumo.nuliko.runConnector

class ConnectorService : Service() {

    private val connectorScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    companion object {
        var isConnected by mutableStateOf(false)
            private set

        fun start(ctx: Context) {
            ctx.startService(Intent(ctx, ConnectorService::class.java))
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, ConnectorService::class.java))
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        job?.cancel()
        job = connectorScope.launch(Dispatchers.IO) {
            runConnector(Constants.Android.appId, Repository.deviceId, ::handleMessage) {
                isConnected = it
            }
        }
        return START_STICKY
    }

    private fun handleMessage(msg: RCMessage) {
        when (val content = msg.content) {
            is RCMessage.Content.StreamData -> {
                Repository.player.post(content.data)
            }

            is RCMessage.Content.StreamHistoryAnswer -> TODO()
            is RCMessage.Content.StreamHistoryAsk -> TODO()
            is RCMessage.Content.StreamHistoryStart -> TODO()
            is RCMessage.Content.StreamHistoryStop -> TODO()
            is RCMessage.Content.Gpio,
            is RCMessage.Content.StreamLiveStart,
            is RCMessage.Content.StreamLiveStop -> logERROR(content)
        }
    }

    override fun onDestroy() {
        isConnected = false
        job?.cancel()
        super.onDestroy()
    }
}
