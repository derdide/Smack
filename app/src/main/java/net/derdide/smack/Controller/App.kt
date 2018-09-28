package net.derdide.smack.Controller

import android.app.Application
import net.derdide.smack.Utilities.SharedPrefs

class App:Application() {

    companion object {
        lateinit var prefs: SharedPrefs

    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}