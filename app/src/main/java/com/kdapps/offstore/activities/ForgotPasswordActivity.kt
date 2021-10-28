package com.kdapps.offstore.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.kdapps.offstore.R
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_register.*

class ForgotPasswordActivity : baseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        setupActionBar()

        btn_submit.setOnClickListener(){
            val email = forgot_pw_email.text.toString().trim{it <= ' '}
            if(email.isEmpty()){
                showErrorSnackBar("Enter valid email Id", true)
            }
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(){task ->
                        hideProgressDialog()
                        Toast.makeText(this@ForgotPasswordActivity,"Email sent Successfully!",Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private  fun setupActionBar(){
        setSupportActionBar(toolbar_forgot)

        val actionBar = supportActionBar
        if(actionBar!= null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }
        toolbar_forgot.setNavigationOnClickListener{ onBackPressed()}
    }
}