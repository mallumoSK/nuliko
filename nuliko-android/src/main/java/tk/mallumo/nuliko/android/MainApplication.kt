package tk.mallumo.nuliko.android

import android.app.Application

val app: MainApplication get() = MainApplication.instance

class MainApplication : Application() {

    companion object {
        lateinit var instance: MainApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

    }
}