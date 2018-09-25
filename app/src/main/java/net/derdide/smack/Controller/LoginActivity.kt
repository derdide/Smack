package net.derdide.smack.Controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import net.derdide.smack.R
import net.derdide.smack.Services.AuthService

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginCreateUserBtnClicked (view: View) {

        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

    fun loginLoginBtnClicked (view: View) {

        val email = loginEmailTxt.text.toString()
        val password = loginPwdTxt.text.toString()
        AuthService.loginUser(this, email, password){
            loginSuccess ->

            if (loginSuccess) {
                AuthService.findUserByEmail(this){
                    findSuccess -> if (findSuccess){
                        finish()
                    }
                }
            }
        }

    }
}
