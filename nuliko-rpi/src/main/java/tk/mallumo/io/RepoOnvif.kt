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
import tk.mallumo.*
import tk.mallumo.log.*
import tk.mallumo.utils.*
import kotlin.coroutines.*
import kotlin.system.*
import kotlin.time.*
import kotlin.time.Duration.Companion.minutes


class RepoOnvif : ImplRepo() {

    private lateinit var job: Job

    private var profile: OnvifMediaProfile? = null

    private val streamClients = mutableListOf<Pair<String, Long>>()

    @OptIn(DelicateCoroutinesApi::class)
    override val scope = CoroutineScope(newFixedThreadPoolContext(2, "RepoOnvif")) + Dispatchers.IO

    fun run() {
        job = scope.launch {
            println("main loop start")

            while (isActive) {

                while (isActive && profile == null) {
                    profile = findOnvifProfile()
                    println("main loop ready")
                }

                if (!isActive) break

                kotlin.runCatching {
                    clientRestCam().use { client ->
                        val url = onvifManager.getSnapshotUrl(profile!!)
                        val ip = camDev!!.hostName.split("//")[1].split(":")[0]
                        val realUrl = url.split(":")
                            .toMutableList()
                            .let {
                                it[1] = "//$ip"
                                it
                            }.joinToString(":")
                        val response = client.get(realUrl)
                        response.takeIf { it.status.isSuccess() }
                            ?.body<ByteArray>()
                            ?.also { bytes ->
                                val timestamp = Repository.diskManager.storeImage(bytes)

                                val now = System.currentTimeMillis()

                                streamClients.filter { it.second > now }
                                    .forEach {
                                        Repository.direct.postVideoFrame(it.first, timestamp.toInt(), bytes)
                                    }

                            }
                            ?: throw Exception(response.toString())
                    }
                }.onFailure {
                    it.printStackTrace()
                    profile = null
                }
            }
        }
    }

    private suspend fun findOnvifProfile(): OnvifMediaProfile? {
        camDev = null

        while (camDev == null) {
            for (ip in 2..255) {
                val device = OnvifDevice(
                    /* hostName = */ "192.168.1.$ip:8899",
                    /* username = */ GlobalParams.camAuthName,
                    /* password = */ GlobalParams.camAuthPass
                )
                val profile = device.profile()
                if(profile != null){
                    camDev = device
                    return profile
                }
            }
        }
        return null
    }

    override fun close() {
        if (::job.isInitialized) job.runCatching { cancel() }
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

private val onvifManager by lazy {
    OnvifManager(respondListener)
}

private val errHolder = mutableListOf<((errorCode: Int, errorMessage: String?) -> Unit)>()

private val respondListener = object : OnvifResponseListener {

    override fun onResponse(onvifDevice: OnvifDevice?, response: OnvifResponse<*>?) = Unit

    override fun onError(onvifDevice: OnvifDevice?, errorCode: Int, errorMessage: String?) {
        while (errHolder.isNotEmpty()) {
            errHolder.removeFirst().invoke(errorCode, errorMessage)
        }
    }
}

private var camDev: OnvifDevice? = null

class OnvifException(code: Int, msg: String?) : Exception("$code : $msg")


@Throws(OnvifException::class)
private suspend fun OnvifDevice.profile():OnvifMediaProfile? =
    suspendCancellableCoroutine { continuation ->
        var manager: OnvifManager? = null
        val callback = object : OnvifResponseListener {
            override fun onResponse(onvifDevice: OnvifDevice?, response: OnvifResponse<*>?) =Unit

            override fun onError(onvifDevice: OnvifDevice?, errorCode: Int, errorMessage: String?) {
                manager?.destroy()
                continuation.resume(null)
            }

        }
        manager = OnvifManager(callback)
        manager.getMediaProfiles(this) { _ , profiles:List<OnvifMediaProfile?>? ->
            manager.destroy()
            continuation.resume(profiles?.firstOrNull())
        }
    }

@Throws(OnvifException::class)
private suspend fun OnvifManager.getProfiles() =
    suspendCancellableCoroutine<List<OnvifMediaProfile>> { continuation ->

        val errCallback = { errorCode: Int, errorMessage: String? ->
            continuation.resumeWith(Result.failure(OnvifException(errorCode, errorMessage)))
        }

        errHolder += errCallback

        getMediaProfiles(camDev) { _, mediaProfiles ->
            errHolder -= errCallback
            continuation.resumeWith(Result.success(mediaProfiles))
        }
    }

@Throws(OnvifException::class)
private suspend fun OnvifManager.getSnapshotUrl(profile: OnvifMediaProfile) =
    suspendCancellableCoroutine<String> { continuation ->

        val errCallback = { errorCode: Int, errorMessage: String? ->
            continuation.resumeWith(Result.failure(OnvifException(errorCode, errorMessage)))
        }

        errHolder += errCallback

        getSnapshotURI(camDev, profile) { _, _, url ->
            errHolder -= errCallback
            continuation.resumeWith(Result.success(url))
        }
    }
