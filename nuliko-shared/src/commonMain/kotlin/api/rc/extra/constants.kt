package api.rc.extra

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.compression.*
import tk.mallumo.utils.minute
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

//val PORT = 8881 -> real_portal
//val PORT = 8882 -> image_to_qr
//val PORT = 8883 -> test_portal
object Constants {
    private const val DEFAULT_HOST = "mallumo.tk"

    private const val DEFAULT_PORT = 15_000

    object Rpi {
        const val appId = "nRPI-RC"
        const val deviceIdDefault = "RPI4x2"
        fun connectorId(deviceId: String) = createConnectorID(appId, deviceId)
    }

    object Android {
        const val appId = "nANDROID-RC"

        //        const val deviceId = "RPI4x0"
        fun connectorId(deviceId: String) = createConnectorID(appId, deviceId)
    }

    internal fun createConnectorID(appId: String, deviceId: String) = "${appId}_$deviceId"

    val server = ServerConfig(
        host = DEFAULT_HOST,
        port = DEFAULT_PORT
    )

    object Realm {
        val MESSAGE = "api-message"
        val REGISTRATION = "api-reg"
    }

    object Path {
        val API_MESSAGE = "api/message"
        val WS_REGISTRATION = "ws/registration"
    }

    val AES_KEY = "xtyurth54xhWY;[]".toByteArray()


    fun clientDirect(id: String) = HttpClient(CIO) {
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
            requestTimeoutMillis = 1.minute
        }

        install(Auth) {
            digest {
                credentials {
                    DigestAuthCredentials(id, buildAuthPassword(id))
                }
                realm = Realm.MESSAGE
            }
            digest {
                credentials {
                    DigestAuthCredentials(id, buildAuthPassword(id))
                }
                realm = Realm.REGISTRATION
            }
        }
    }

    fun buildServerProtoMsgUrl(targetAppId: String, targetDevId: String): String = buildString {
        append("http://${server.host}:${server.port}")
        append("/${Path.API_MESSAGE}")
        append("/$targetAppId")
        append("/proto")
        append("/$targetDevId")
    }
}
