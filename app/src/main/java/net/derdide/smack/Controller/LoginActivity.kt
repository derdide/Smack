package net.derdide.smack.Controller

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_login.*
import net.derdide.smack.R
import net.derdide.smack.Services.AuthService

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginSpinner.visibility = View.INVISIBLE
    }

    fun loginCreateUserBtnClicked (view: View) {

        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

    fun loginLoginBtnClicked (view: View) {
        enableSpinner(true)
        val email = loginEmailTxt.text.toString()
        val password = loginPwdTxt.text.toString()
        hideKeyboard()
        AuthService.loginUser(email, password){
            loginSuccess ->

            if (loginSuccess) {
                AuthService.findUserByEmail(this){
                    findSuccess -> if (findSuccess){
                        enableSpinner(false)
                        finish()
                    } else {errorToast()}
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
            loginSpinner.visibility = View.VISIBLE
                    }
        else {
            loginSpinner.visibility = View.INVISIBLE
            }
        loginLoginBtn.isEnabled = !enable
        loginCreateUserBtn.isEnabled = !enable
    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }

    }

}
