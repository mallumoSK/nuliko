package tk.mallumo.nuliko

import api.rc.*
import api.rc.extra.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import tk.mallumo.log.*
import tk.mallumo.utils.*




suspend fun CoroutineScope.runConnector(
    appId: String,
    deviceId: String,
    onHandleMessage: suspend (RCMessage) -> Unit
) {
    val connectorId = Constants.createConnectorID(appId, deviceId)

    while (isActive) {
        runCatching {
            val registrationMessage = Message.Registration.create(appId, deviceId)
            clientSocket(connectorId).use {
                it.webSocket(
                    method = HttpMethod.Get,
                    host = Constants.server.host,
                    port = Constants.server.port,
                    path = "/${Constants.Path.WS_REGISTRATION}"
                ) {
                    logINFO("CONNECTING")
                    send(Frame.Binary(true, registrationMessage.toProto()))

                    for (message in incoming) {
                        when (message) {
                            is Frame.Binary -> {
                                val bytes = message.readBytes()
                                try {
                                    onHandleMessage(RCMessage.decode(bytes)!!)
                                } catch (e: Exception) {
                                    try {
                                        logWARN(Message.fromProto(bytes))
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                            else -> println("unknown message:${message::class.simpleName}")
                        }
                    }
                }
            }
        }.onFailure {
            logERROR("DISCONNECTED")
            it.printStackTrace()
            delay(15.second)
        }
    }
}

fun clientSocket(id: String) = HttpClient(CIO) {
    install(ContentEncoding) {
        gzip(1F)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 1.hour
        socketTimeoutMillis = 1.hour
        connectTimeoutMillis = 1.hour
    }
    install(WebSockets){
        pingInterval = 30.second
    }
    install(Auth) {
        digest {
            credentials {
                DigestAuthCredentials(id, buildAuthPassword(id))
            }
            realm = Constants.Realm.REGISTRATION
        }
    }
}
