package net.derdide.smack.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import net.derdide.smack.Controller.App
import net.derdide.smack.Model.Channel
import net.derdide.smack.Model.Message
import net.derdide.smack.Services.UserDataService.name
import net.derdide.smack.Utilities.URL_GET_CHANNELS
import net.derdide.smack.Utilities.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(complete: (Boolean) -> Unit) {
        val channelsRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null,
                Response.Listener {response ->
                    try {
                        for (x in 0 until response.length()){
                            val channel = response.getJSONObject(x)
                            val name = channel.getString("name")
                            val desc = channel.getString("description")
                            val id = channel.getString("_id")

                            val newChannel = Channel(name, desc, id)
                            this.channels.add(newChannel)
                            Log.d("APP", "Channel $name retrieved")
                        }
                        complete(true)
                    }
                    catch(e: JSONException) {
                        Log.d("JSON", "EXC: " + e.localizedMessage)
                        complete(false)
                    }

                },
                Response.ErrorListener {error ->
                    Log.d("ERROR", "Could not retrieve channels")
                    complete(false)
                }){

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }
        App.prefs.requestQueue.add(channelsRequest)


    }

    fun getMessages(channelId: String, complete: (Boolean) -> Unit){
        val url = "$URL_GET_MESSAGES$channelId"
        val messagesRequest = object : JsonArrayRequest(Method.GET, url, null,
                Response.Listener {response ->
                    clearMessages()
                    try {
                        for (x in 0 until response.length()){
                            val message = response.getJSONObject(x)
                            val messageBody = message.getString("messageBody")
                            val channelId = message.getString("channelId")
                            val id = message.getString("_id")
                            val userName = message.getString("userName")
                            val userAvatar = message.getString("userAvatar")
                            val userAvatarColor = message.getString("userAvatarColor")
                            val timeStamp = message.getString("timeStamp")

                            val newMessage = Message(messageBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                            this.messages.add(newMessage)
                            Log.d("APP", "Message $messageBody retrieved")
                        }
                        complete(true)
                    }
                    catch(e: JSONException) {
                        Log.d("JSON", "EXC: " + e.localizedMessage)
                        complete(false)
                    }

                },
                Response.ErrorListener {error ->
                    Log.d("ERROR", "Could not retrieve messages")
                    complete(false)
                }){

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }
        App.prefs.requestQueue.add(messagesRequest)
    }

    fun clearMessages(){
        messages.clear()
    }
    fun clearChannels(){
        channels.clear()
    }
}