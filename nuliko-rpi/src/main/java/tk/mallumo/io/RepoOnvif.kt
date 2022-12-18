package tk.mallumo.io

import be.teletask.onvif.*
import be.teletask.onvif.listeners.*
import be.teletask.onvif.models.*
import be.teletask.onvif.responses.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import tk.mallumo.*
import tk.mallumo.utils.*
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes


class RepoOnvif : ImplRepo() {

    private lateinit var job: Job

    var profile: OnvifMediaProfile? = null

    private val streamClients = mutableListOf<Pair<String, Long>>()

    @OptIn(DelicateCoroutinesApi::class)
    override val scope = CoroutineScope(newFixedThreadPoolContext(2, "RepoOnvif")) + Dispatchers.IO

    init {
        println("profile init start")
        scope.launch {
            do {
                runCatching {
                    profile = onvifManager.getProfiles()
                        .let { it.getOrNull(1) ?: it[0] }
                }.onFailure {
                    it.printStackTrace()
                    delay(1.minutes)
                }
            } while (profile == null && isActive)
            println("profile init OK")
        }
    }

    fun run() {
        job = scope.launch {
            println("main loop start")
            do {
                delay(5.second)
            } while (profile == null)
            println("main loop ready")
            while (isActive) {
                kotlin.runCatching {
                    clientRestCam().use { client ->
                        val response = client.get(onvifManager.getSnapshotUrl(profile!!))
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
                    delay(10.second)
                }
            }
        }
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

private val camDev by lazy {
    OnvifDevice(
        /* hostName = */ GlobalParams.camIp,
        /* username = */ GlobalParams.camAuthName,
        /* password = */ GlobalParams.camAuthPass
    )
}

class OnvifException(code: Int, msg: String?) : Exception("$code : $msg")


@Throws(OnvifException::class)
suspend fun OnvifManager.getProfiles() =
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
suspend fun OnvifManager.getSnapshotUrl(profile: OnvifMediaProfile) =
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
