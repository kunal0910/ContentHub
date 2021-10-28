package com.kdapps.offstore.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kdapps.offstore.R
import com.kdapps.offstore.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences(Constants.MYSHAREDPREFRENCES, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(Constants.LOGGEDINUSERNAME, "")!!

        tv_main.text = "Hello $username"
    }
}