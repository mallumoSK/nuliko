package tk.mallumo.nuliko.android.io

import kotlinx.coroutines.*
import tk.mallumo.log.logERROR
import tk.mallumo.nuliko.android.app
import tk.mallumo.nuliko.android.system.deviceID

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

}
