package com.kdapps.offstore.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import com.google.firebase.auth.FirebaseAuth
import com.kdapps.offstore.R
import com.kdapps.offstore.firestore.FirestoreClass
import com.kdapps.offstore.models.User
import com.kdapps.offstore.utils.Constants
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class LoginActivity : baseActivity() {

    fun userLoggedInSuccess(user: User){
        hideProgressDialog()

        //print the user details in log for now
        Log.i("Full Name", user.fullName)
        Log.i("Username", user.username)
        Log.i("Email Id", user.email)

        //Redirect the user to main screen after log in.
        if(user.profileCompleted == 0){
            val intent = (Intent(this@LoginActivity, ProfileActivity::class.java))
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        }
        else {
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        register.setOnClickListener(){
            val intent = Intent(this@LoginActivity, registerActivity::class.java)
            startActivity(intent)
        }
        forgot_password.setOnClickListener(){
            //showErrorSnackBar("Still in progress.", true)
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        btn_login.setOnClickListener(){
            loginRegisteredUser()
        }

    }

    private fun validateLoginDetails(): Boolean{
        return when{
            TextUtils.isEmpty(log_email.text.toString().trim() {it <= ' '}) ->{
                showErrorSnackBar(resources.getString(R.string.err_email), true)
                false
            }

            TextUtils.isEmpty(log_pass.text.toString().trim() { it <= ' ' }) ->{
                showErrorSnackBar(resources.getString(R.string.err_password),true)
                false
            }
            else ->{
                //showErrorSnackBar("You are registered user!!!",false)
                true
            }
        }
    }

    private fun loginRegisteredUser(){
        if(validateLoginDetails()){
            showProgressDialog(resources.getString(R.string.please_wait))

            val email = log_email.text.toString().trim(){ it <= ' '}
            val password = log_pass.text.toString().trim(){ it <= ' '}

            // login using firebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(){ task ->


                if(task.isSuccessful){
                        FirestoreClass().getUserDetails(this@LoginActivity)

                }else{
                    hideProgressDialog()
                    showErrorSnackBar(task.exception!!.message.toString(), true)
                }

            }


        }
    }
}