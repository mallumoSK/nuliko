package tk.mallumo.ext

import be.teletask.onvif.OnvifManager
import be.teletask.onvif.listeners.OnvifResponseListener
import be.teletask.onvif.models.OnvifDevice
import be.teletask.onvif.models.OnvifMediaProfile
import be.teletask.onvif.responses.OnvifResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import tk.mallumo.io.OnvifException
import tk.mallumo.log.logERROR
import kotlin.coroutines.resume

@Throws(OnvifException::class)
suspend fun OnvifDevice.profile(): OnvifMediaProfile? =
    suspendCancellableCoroutine { continuation ->
        var manager: OnvifManager? = null
        val callback = object : OnvifResponseListener {
            override fun onResponse(onvifDevice: OnvifDevice?, response: OnvifResponse<*>?) {
                logERROR("$onvifDevice")
            }

            override fun onError(onvifDevice: OnvifDevice?, errorCode: Int, errorMessage: String?) {
                manager?.destroy()
                logERROR(errorMessage)
                continuation.resume(null)
            }

        }
        manager = OnvifManager(callback)
        manager.getMediaProfiles(this) { _, profiles: List<OnvifMediaProfile?>? ->
            manager.destroy()
            continuation.resume(profiles?.firstOrNull())
        }
    }

@Throws(OnvifException::class)
suspend fun OnvifDevice.streamUrl(profile: OnvifMediaProfile): String? =
    suspendCancellableCoroutine { continuation ->
        var manager: OnvifManager? = null
        val callback = object : OnvifResponseListener {
            override fun onResponse(onvifDevice: OnvifDevice?, response: OnvifResponse<*>?) = Unit

            override fun onError(onvifDevice: OnvifDevice?, errorCode: Int, errorMessage: String?) {
                manager?.destroy()
                continuation.resume(null)
            }

        }
        manager = OnvifManager(callback)
        manager.getMediaStreamURI(this, profile) { _, _, uri: String? ->
            manager.destroy()
            continuation.resume(uri)
        }
    }

@Throws(OnvifException::class)
suspend fun OnvifDevice.snapshotUrl(profile: OnvifMediaProfile): String? =
    suspendCancellableCoroutine { continuation ->
        var manager: OnvifManager? = null
        val callback = object : OnvifResponseListener {
            override fun onResponse(onvifDevice: OnvifDevice?, response: OnvifResponse<*>?) = Unit

            override fun onError(onvifDevice: OnvifDevice?, errorCode: Int, errorMessage: String?) {
                manager?.destroy()
                continuation.resume(null)
            }

        }
        manager = OnvifManager(callback)
        manager.getSnapshotURI(this, profile) { _, _, uri: String? ->
            manager.destroy()
            continuation.resume(uri)
        }
    }