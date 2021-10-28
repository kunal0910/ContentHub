package com.kdapps.offstore.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kdapps.offstore.R

class splashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen2)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler().postDelayed(
                {
                    val auth = FirebaseAuth.getInstance()
                    if(auth.currentUser != null){
                        startActivity(Intent(this@splashScreen, DashboardActivity::class.java))
                        finish()
                    }
                    else {
                        startActivity(Intent(this@splashScreen, LoginActivity::class.java))
                        finish()
                    }
                },
                2500
        )
    }
}