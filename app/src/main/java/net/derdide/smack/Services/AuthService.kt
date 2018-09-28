package net.derdide.smack.Services

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import net.derdide.smack.Controller.App
import net.derdide.smack.Services.UserDataService.email
import net.derdide.smack.Utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

//    var isLoggedIn = false
//    var user = ""
//    var authToken = ""

    fun registerUser(email: String, password: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener {
            response ->

            Log.d("APP", "User $email registered")
            complete(true)
        }, Response.ErrorListener {
            error -> Log.d("ERROR", "Could not REGISTER user $email")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset= utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        App.prefs.requestQueue.add(registerRequest)

    }

    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener {
            response ->

                try {
                    App.prefs.user = response.getString("user")
                    App.prefs.authToken = response.getString("token")
                    App.prefs.isLoggedIn = true
                    Log.d("APP", "User $email logged in")
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " +e.localizedMessage)
                    complete(false)
                }

        }, Response.ErrorListener {
            error -> Log.d("ERROR", "Could not LOGIN user $email")
            complete(false)
        })
            {
                override fun getBodyContentType(): String {
                    return "application/json; charset= utf-8"
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray()
                }
            }

        App.prefs.requestQueue.add(loginRequest)

    }

    fun createUser(name: String, email: String, avatarName: String, avatarColor: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatarName)
        jsonBody.put("avatarColor", avatarColor)
        val requestBody = jsonBody.toString()

        val createRequest = object : JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener {
            response ->
            try {
                UserDataService.name= response.getString("name")
                UserDataService.email= response.getString("email")
                UserDataService.avatarName= response.getString("avatarName")
                UserDataService.avatarColor= response.getString("avatarColor")
                UserDataService.id= response.getString("_id")
                Log.d("APP", "User $email created")
                Log.d("APP", "Avatar Color $avatarColor")
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "EXC: " +e.localizedMessage)
                complete(false)
            }
        }, Response.ErrorListener {
            error -> Log.d("ERROR", "Could not CREATE user $email")
            complete(false)
        })
        {
            override fun getBodyContentType(): String {
                return "application/json; charset= utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }

        App.prefs.requestQueue.add(createRequest)

    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit){

        val findUserRequest = object: JsonObjectRequest(Method.GET, "$URL_GET_USER${App.prefs.user}", null, Response.Listener {
            response ->
            try {
                UserDataService.name= response.getString("name")
                UserDataService.email= response.getString("email")
                UserDataService.avatarName= response.getString("avatarName")
                UserDataService.avatarColor= response.getString("avatarColor")
                UserDataService.id= response.getString("_id")
                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                Log.d("APP", "User $email: data retrieved")
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "EXC: " +e.localizedMessage)
                complete(false)
            }
        }, Response.ErrorListener {error -> Log.d("ERR", "Could not find user: ${App.prefs.user}")
            complete(false)

        }){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }

        App.prefs.requestQueue.add(findUserRequest)

    }
}