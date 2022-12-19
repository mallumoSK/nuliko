package tk.mallumo.nuliko.android.io

import kotlinx.coroutines.*
import tk.mallumo.log.*
import tk.mallumo.nuliko.android.*
import tk.mallumo.nuliko.android.system.*

object Repository {

    private val baseScope = CoroutineScope(Dispatchers.IO)

    val deviceId by lazy {
        logERROR(app.deviceID)
        app.deviceID
    }

    val player by lazy {
        RepoPlayer(baseScope + CoroutineName("player"))
    }

    val direct by lazy {
        RepoDirect(baseScope + CoroutineName("connector"))
    }

    val gpio by lazy {
        RepoGpio(baseScope + CoroutineName("gpio"))
    }
}
