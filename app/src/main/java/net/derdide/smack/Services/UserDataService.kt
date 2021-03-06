package net.derdide.smack.Services

import android.graphics.Color
import android.util.Log
import net.derdide.smack.Controller.App
import java.util.*

object UserDataService {

    var id = ""
    var avatarName = ""
    var avatarColor = ""
    var email = ""
    var name = ""

    fun returnAvatarColor(components: String): Int {

        val strippedColor = components.replace("[", "").replace("]","").replace(",","")
        var r = 0
        var g = 0
        var b = 0

        val scanner = Scanner(strippedColor)
        if (scanner.hasNext()){
            r = (scanner.nextDouble() *255).toInt()
            Log.d("APP", "Red channel $r")
            g = (scanner.nextDouble() *255).toInt()
            Log.d("APP", "Green channel $g")
            b = (scanner.nextDouble() *255).toInt()
            Log.d("APP", "Blue channel $b")


        }

        return Color.rgb(r, g, b)

    }

    fun logout (){
        id = ""
        avatarColor = ""
        avatarName = ""
        email = ""
        name = ""
        App.prefs.user = ""
        App.prefs.authToken= ""
        App.prefs.isLoggedIn = false
        MessageService.clearMessages()
        MessageService.clearChannels()
    }
}