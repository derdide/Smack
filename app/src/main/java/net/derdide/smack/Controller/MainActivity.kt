package net.derdide.smack.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import net.derdide.smack.Model.Channel
import net.derdide.smack.Model.Message
import net.derdide.smack.R
import net.derdide.smack.Services.AuthService
import net.derdide.smack.Services.MessageService
import net.derdide.smack.Services.UserDataService
import net.derdide.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import net.derdide.smack.Utilities.SOCKET_URL

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)

    lateinit var channelAdapter: ArrayAdapter<Channel>

    var selectedChannel: Channel? = null

    private fun setupAdapters(){
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        setupAdapters()

        channel_list.setOnItemClickListener {_, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if (App.prefs.isLoggedIn){
            AuthService.findUserByEmail(this){}
        }

//        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))

        super.onResume()
    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        super.onDestroy()
    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn){
                userNameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userAvatarNavHeader.setImageResource(resourceId)
                loginBtnNavHeader.text = "LOGOUT"
                userAvatarNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))

                MessageService.getChannels{complete ->
                    if (complete){

                        if (MessageService.channels.count() > 0) {
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                    }

                }
            }
        }

    }

    fun updateWithChannel(){
        mainChannelName.text = "#${selectedChannel?.name}"
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginBtnNavClicked (view: View) {

        if (App.prefs.isLoggedIn){
            UserDataService.logout()
            userNameNavHeader.text = "please login"
            userEmailNavHeader.text = ""
            userAvatarNavHeader.setImageResource(R.drawable.profiledefault)
            userAvatarNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNavHeader.text = "LOGIN"
        }
        else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }


    }

    fun addChannelBtnClicked (view: View) {
        if (App.prefs.isLoggedIn){
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                    .setPositiveButton("Add") {_, _ ->
                        val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                        val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescTxt)
                        val channelName = nameTextField.text.toString()
                        val channelDesc = descTextField.text.toString()

                        socket.emit("newChannel", channelName, channelDesc)

                    }
                    .setNegativeButton("Cancel") {dialogInterface, i ->

                    }
                    .show()
        }
    }

    private  val onNewChannel = Emitter.Listener { args ->

        runOnUiThread {
            val channelName = args[0] as String
            val channelDesc = args[1] as String
            val channelId = args[2] as String
            Log.d("APP", "Channel $channelName")

            val newChannel = Channel(channelName, channelDesc, channelId)
            MessageService.channels.add(newChannel)
            channelAdapter.notifyDataSetChanged()
        }
    }

    private  val onNewMessage = Emitter.Listener { args ->

        runOnUiThread {
            val messageBody = args[0] as String
            val channelId = args[2] as String
            val userName = args [3] as String
            val userAvatar = args[4] as String
            val userAvatarColor = args[5] as String
            val id = args[6] as String
            val timeStamp = args[7] as String
            Log.d("APP", "Message $messageBody")

            val newMessage = Message(messageBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
            MessageService.messages.add(newMessage)

//            channelAdapter.notifyDataSetChanged()
        }
    }

    fun sendMsgBtnClicked (view: View) {

        if (App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null){
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", messageTextField.text.toString(), userId, channelId,
                    UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)
            messageTextField.text.clear()
            hideKeyboard()
        }



    }
    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }

    }


}
