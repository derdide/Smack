package net.derdide.smack.Controller

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_user.*
import net.derdide.smack.R
import net.derdide.smack.Services.AuthService
import net.derdide.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        Log.d("APP", "Activity CreateUser created")
        createSpinner.visibility = View.INVISIBLE
    }

    fun generateAvatar (view: View){

        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        if (color==0) {
            userAvatar = "light$avatar"
        } else {
            userAvatar = "dark$avatar"
        }

        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarImgView.setImageResource(resourceId)

    }

    fun createUserBtnClicked(view: View) {
        val userEmail = createUserEmailTxt.text.toString()
        val userPwd = createUserPwdTxt.text.toString()
        val userName = createUserNameTxt.text.toString()
        enableSpinner(true)
        Log.d("APP", "Create User button clicked")
        Log.d("APP", "user e-mail $userEmail")
        Log.d("APP", "user name $userName")

        AuthService.registerUser(userEmail, userPwd){
            registerSuccess -> if (registerSuccess){
                AuthService.loginUser( userEmail, userPwd){
                    loginSuccess -> if (loginSuccess){
                        AuthService.createUser(userName, userEmail, userAvatar, avatarColor){
                        createSuccess -> if (createSuccess){

                            val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)

                            LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                            enableSpinner(false)
                            finish()
                            }
                            else {errorToast()}
                        }

                    }
                    else {errorToast()}
                }

            } else {errorToast()}
        }

    }

    fun errorToast(){
        Toast.makeText(this, "NONONONONONO", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean){
       if (enable){
           createSpinner.visibility = View.VISIBLE
           createUserBtn.isEnabled = false
           createAvatarImgView.isEnabled = false
           createBackgroundBtn.isEnabled = false

       }
       else {
           createSpinner.visibility = View.INVISIBLE
           createUserBtn.isEnabled = true
           createAvatarImgView.isEnabled = true
           createBackgroundBtn.isEnabled = true
       }

    }

    fun createBackgroundBtnClicked(view: View){
        val random = Random()
        val r = random.nextInt(256)
        val g = random.nextInt(256)
        val b = random.nextInt(256)

        createAvatarImgView.setBackgroundColor(Color.rgb(r, g, b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"
   }
}
