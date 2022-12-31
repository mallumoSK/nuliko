package tk.mallumo.io

import api.rc.extra.*
import be.teletask.onvif.*
import be.teletask.onvif.listeners.*
import be.teletask.onvif.models.*
import be.teletask.onvif.responses.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import okhttp3.internal.notifyAll
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import tk.mallumo.*
import tk.mallumo.ext.*
import tk.mallumo.log.*
import tk.mallumo.utils.*
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.coroutines.*
import kotlin.system.*
import kotlin.time.*


class RepoOnvif : ImplRepo() {

    private lateinit var jobCam1: Job
    private lateinit var jobCam2: Job

    private val streamClients = mutableListOf<Pair<String, Long>>()

    override val scope = CoroutineScope(CoroutineName("RepoOnvif")) + Dispatchers.IO

    fun runCam2() {
        jobCam2 = scope.launch {
            println("main loop start")


            var snapshotUrl = ""
            while (isActive) {

                while (isActive && snapshotUrl.isEmpty()) {
                    findOnvifProfile("admin", 3702).also {
                        snapshotUrl = it.first.snapshotUrl(it.second) ?: ""
                    }
                }

                if (!isActive) break

                var errCounter = 0
                kotlin.runCatching {
                    while (isActive && errCounter < 10) {
                        runCatching {
                            val bytes = clientRestCam().use {
                                it.get(snapshotUrl).body<ByteArray>()
                            }
                            val data = bytes.toMat().toBytesOfWebP()

                            val timestamp = Repository.diskManager.storeImage(data)
                            val now = System.currentTimeMillis()

                            streamClients
                                .filter { it.second > now }
                                .forEach {
                                    Repository.direct.postVideoFrame(it.first, timestamp.toInt(), data)
                                }
                        }.onFailure {
                            errCounter += 1
                        }.onSuccess {
                            errCounter = 0
                        }

                    }
                }.onFailure {
                    it.printStackTrace()
                }
                snapshotUrl = ""
            }
        }
    }

    fun runCam1() {
        jobCam1 = scope.launch {
            println("main loop start")


            var streamUrl = ""
            while (isActive) {

                while (isActive && streamUrl.isEmpty()) {
                    findOnvifProfile("cam1", 8899).also {
                        streamUrl = it.first.streamUrl(it.second) ?: ""
                    }
                }

                if (!isActive) break

                var cap: VideoCapture? = null
                val imgMat = Mat()
                var errCounter = 0
                kotlin.runCatching {
                    cap = VideoCapture(streamUrl)
                    println("main loop ready")

                    while (isActive && errCounter < 10) {
                        if (!cap!!.read(imgMat)) {
                            errCounter += 1
                        } else {
                            val bytes = imgMat.toBytesOfWebP()

                            val timestamp = Repository.diskManager.storeImage(bytes)
                            val now = System.currentTimeMillis()

                            streamClients.filter { it.second > now }
                                .forEach {
                                    Repository.direct.postVideoFrame(it.first, timestamp.toInt(), bytes)
                                }
                        }
                    }
                }.onFailure {
                    it.printStackTrace()
                }
                streamUrl = ""
                imgMat.runCatching {
                    release()
                }
                cap?.runCatching {
                    release()
                }
            }
        }
    }

    private suspend fun findOnvifProfile(authName: String, port: Int): Pair<OnvifDevice, OnvifMediaProfile> {
        var camDev: OnvifDevice?
        val range = IntRange(250, 250) + (2..255)

        while (true) {
            var ipParts: List<String>? = null
            while (ipParts == null){
                ipParts = withContext(Dispatchers.IO) {
                    NetworkInterface.getNetworkInterfaces()
                        .asSequence()
                        .mapNotNull { it.ip }
                        .firstOrNull()
                        ?.split(".")
                }
                delay(1.second)
            }

            for (ipSuffix in range) {
                logERROR("${ipParts[0]}.${ipParts[1]}.${ipParts[2]}.$ipSuffix:$port")
                camDev = OnvifDevice(
                    /* hostName = */ "${ipParts[0]}.${ipParts[1]}.${ipParts[2]}.$ipSuffix:$port",
                    /* username = */ authName,
                    /* password = */ GlobalParams.camAuthPass
                )
                runCatching {
                    return camDev to camDev.profile()!!
                }.onFailure { it.printStackTrace() }
            }
        }
    }

    override fun close() {
        if (::jobCam1.isInitialized) jobCam1.runCatching { cancel() }
        if (::jobCam2.isInitialized) jobCam2.runCatching { cancel() }
        super.close()
    }


    fun streamLiveStart(from: String, durationMs: Int) {
        streamHistoryStop(from)
        streamClients += from to (System.currentTimeMillis() + durationMs)
    }

    fun streamLiveStop(from: String) {
        streamClients.removeIf { it.first == from }
    }

    private val historyJobs = mutableMapOf<String, Job>()

    fun streamHistoryStart(target: String, time: String, durationMs: Long) {
        streamLiveStop(target)
        streamHistoryStop(target)
        historyJobs[target] = scopeGlobal.launch(Dispatchers.IO) {
            val files = Repository.diskManager.getParts(time, durationMs)
            for (file in files) {
                if (!isActive) break
                Repository.direct.postVideoFrame(
                    target = target,
                    time = file.timeStampFromName(),
                    bytes = file.readBytes()
                )
                delay(500)
            }
        }
    }

    fun streamHistoryStop(from: String) {
        historyJobs.remove(from)?.runCatching {
            cancel()
        }
    }
}

class OnvifException(code: Int, msg: String?) : Exception("$code : $msg")

