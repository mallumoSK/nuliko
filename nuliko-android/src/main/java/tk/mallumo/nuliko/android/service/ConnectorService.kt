package tk.mallumo.nuliko.android.service

//import api.rc.*

import android.app.*
import android.content.*
import android.os.*
import androidx.core.app.*
import androidx.core.content.*
import api.rc.*
import api.rc.extra.*
import kotlinx.coroutines.*
import tk.mallumo.log.*
import tk.mallumo.nuliko.*
import tk.mallumo.nuliko.android.*
import tk.mallumo.nuliko.android.io.*

class ConnectorService : Service() {

    private val connectorScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    companion object {
        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, ConnectorService::class.java))
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, ConnectorService::class.java))
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupNotification()
        job?.cancel()
        job = connectorScope.launch(Dispatchers.IO) {
            runConnector(Constants.Android.appId, Repository.deviceId, ::handleMessage)
        }
        return START_STICKY
    }

    private fun setupNotification() {
        val channel = createChannel("Nuliko")
        val notification = NotificationCompat.Builder(this, channel.id)
            .setContentTitle(channel.name)
            .setContentText(channel.name)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(1, notification)
    }

    private fun createChannel(channel: String): NotificationChannel =
        getSystemService<NotificationManager>()!!.let { manager ->
            manager.getNotificationChannel(channel)
                ?: kotlin.run {
                    NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_HIGH).also {
                        manager.createNotificationChannel(it)
                    }
                }
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
        job?.cancel()
        super.onDestroy()
    }
}
