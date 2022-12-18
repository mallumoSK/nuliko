@file:OptIn(DelicateCoroutinesApi::class)

package tk.mallumo.io

import api.rc.extra.*
import be.teletask.onvif.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.*
import okhttp3.internal.*
import tk.mallumo.*
import tk.mallumo.utils.*
import java.io.*
import java.security.cert.*
import java.text.*
import java.util.*
import java.util.concurrent.atomic.*
import javax.net.ssl.*
import kotlin.coroutines.*

object Repository : Closeable {

    const val appId = "NRC"

    const val deviceId = "RPI4x0"

    val connectorId get() = "${appId}_$deviceId"

    val diskManager by lazy {
        RepoDiskManager()
    }

    val onvif by lazy {
        RepoOnvif()
    }

    val direct by lazy {
        RepoDirect()
    }

    override fun close() {
        diskManager.runCatching { close() }
        onvif.runCatching { close() }
        direct.runCatching { close() }
    }
}

abstract class ImplRepo : Closeable {

    companion object {
        private val scopeGen = AtomicInteger(0)
        @JvmStatic
        protected val scopeGlobal = CoroutineScope(Dispatchers.IO)
    }

    @OptIn(DelicateCoroutinesApi::class)
    protected open val scope =
        CoroutineScope(newSingleThreadContext("repo-${scopeGen.getAndIncrement()}")) + Dispatchers.IO

    protected fun clientRestCam() = HttpClient(CIO) {
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}

                    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}

                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                }
            }
        }
        install(ContentEncoding) {
            gzip(1F)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30.second
        }
    }

    protected fun clientSocket(id: String) = HttpClient(CIO) {
        install(ContentEncoding) {
            gzip(1F)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 1.hour
            socketTimeoutMillis = 1.hour
            connectTimeoutMillis = 1.hour
        }
        install(WebSockets)
        install(Auth) {
            digest {
                credentials {
                    DigestAuthCredentials(id, buildAuthPassword(id))
                }
                realm = Constants.Realm.REGISTRATION
            }
        }
    }


    protected fun clientDirect(id: String) = HttpClient(CIO) {
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}

                    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}

                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                }
            }
        }
        install(ContentEncoding) {
            gzip(1F)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 1.hour
        }
        install(HttpRedirect) {

        }
        install(WebSockets)
        install(Auth) {
            digest {
                credentials {
                    DigestAuthCredentials(id, buildAuthPassword(id))
                }
                realm = Constants.Realm.MESSAGE
            }
            digest {
                credentials {
                    DigestAuthCredentials(id, buildAuthPassword(id))
                }
                realm = Constants.Realm.REGISTRATION
            }
        }
    }

    protected fun buildServerProtoMsgUrl(targetAppId: String, targetDevId: String): String = buildString {
        append("http://${Constants.server.host}:${Constants.server.port}")
        append("/${Constants.Path.API_MESSAGE}")
        append("/$targetAppId")
        append("/proto")
        append("/$targetDevId")
    }

    override fun close() {
        scope.runCatching { cancel() }
    }
}
