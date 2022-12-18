package tk.mallumo.nuliko.android.io

import api.rc.*
import api.rc.extra.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import tk.mallumo.log.*

class RepoDirect(scope: CoroutineScope) : ImplRepo(scope) {

    fun send(msg: RCMessage) {
       scope.launch {
           try {
               postMessageFrame(msg)
           }catch (e:Exception){
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
