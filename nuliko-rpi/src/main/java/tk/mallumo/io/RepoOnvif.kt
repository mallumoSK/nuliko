package tk.mallumo.io

import be.teletask.onvif.models.OnvifDevice
import be.teletask.onvif.models.OnvifMediaProfile
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import tk.mallumo.GlobalParams
import tk.mallumo.ext.*
import tk.mallumo.log.logINFO
import tk.mallumo.utils.second
import java.net.NetworkInterface
import kotlin.collections.set


class RepoOnvif : ImplRepo() {

    private var jobCam1: Job? = null
    private var jobCam2: Job? = null

    private val streamClients = mutableMapOf<Int, MutableList<Pair<String, Long>>>()
    private val historyJobs = mutableMapOf<String, Job>()

    override val scope = CoroutineScope(CoroutineName("RepoOnvif")) + Dispatchers.IO

    fun runCam2() {
        jobCam2?.runCatching {
            cancel()
        }
        jobCam2 = scope.launch {
            println("main loop start")
            var dev: OnvifDevice? = null
            var prof: OnvifMediaProfile? = null

            while (isActive) {
                while (isActive && dev == null && prof == null) {
                    findOnvifProfile("admin", 3702).also {
                        dev = it.first
                        prof = it.second
                    }
                }

                if (!isActive) break

                var errCounter = 0
                kotlin.runCatching {
                    while (isActive && errCounter < 10 && dev != null && prof != null) {
                        val snapshotUrl = dev!!.snapshotUrl(prof!!) ?: ""
                        runCatching {
                            val bytes = clientRestCam().use {
                                it.get(snapshotUrl).body<ByteArray>()
                            }
                            val data = bytes.toMat().toBytesOfWebP()

                            val timestamp = Repository.diskManager.storeImage(2, data)
                            val now = System.currentTimeMillis()

                            streamClients[2]
                                ?.filter { it.second > now }
                                ?.forEach {
                                    runCatching {
                                        Repository.direct.postVideoFrame(it.first, timestamp.toInt(), data)
                                    }.onFailure {
                                        it.printStackTrace()
                                    }
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
                dev = null
                prof = null
            }
        }
    }

    fun runCam1() {
        jobCam1?.runCatching { cancel() }
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

                            val timestamp = Repository.diskManager.storeImage(1, bytes)
                            val now = System.currentTimeMillis()
                            streamClients[1]
                                ?.filter { it.second > now }
                                ?.forEach {
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
            while (ipParts == null) {
                ipParts = withContext(Dispatchers.IO) {
                    NetworkInterface.getNetworkInterfaces()
                        .asSequence()
                        .filterNot { it.name.startsWith("tun") }
                        .mapNotNull { it.ip }
                        .firstOrNull()
                        ?.split(".")
                }
                delay(1.second)
            }

            for (ipSuffix in range) {
//                logERROR("${ipParts[0]}.${ipParts[1]}.${ipParts[2]}.$ipSuffix:$port")
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
        jobCam1?.runCatching { cancel() }
        jobCam2?.runCatching { cancel() }
        jobCam1 = null
        jobCam2 = null
        super.close()
    }


    fun streamLiveStart(id: Int, from: String, durationMs: Int) {
        streamHistoryStop(from)
        val clients = streamClients.getOrPut(id) { mutableListOf() }
        clients += from to (System.currentTimeMillis() + durationMs)
        logINFO("cam $id start")

    }

    fun streamLiveStop(id: Int, from: String) {
        streamClients[id]?.removeIf { it.first == from }
        logINFO("cam $id stop")
    }


    fun streamHistoryStart(id: Int, target: String, time: String, durationMs: Long) {
        streamLiveStop(1, target)
        streamLiveStop(2, target)
        streamHistoryStop(target)
        historyJobs[target] = scopeGlobal.launch(Dispatchers.IO) {
            val files = Repository.diskManager.getParts(id, time, durationMs)
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

