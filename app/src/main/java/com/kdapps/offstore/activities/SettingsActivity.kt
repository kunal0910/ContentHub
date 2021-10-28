package com.kdapps.offstore.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.kdapps.offstore.R
import com.kdapps.offstore.firestore.FirestoreClass
import com.kdapps.offstore.models.User
import com.kdapps.offstore.utils.Constants
import com.kdapps.offstore.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_profile2.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : baseActivity(), View.OnClickListener {

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupActionBar()
        getUserDetails()

        settings_edit_profile.setOnClickListener(this@SettingsActivity)
        settings_logout.setOnClickListener(this@SettingsActivity)

    }

    private  fun setupActionBar(){
        setSupportActionBar(toolbar_settings)

        val actionBar = supportActionBar
        if(actionBar!= null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }
        toolbar_settings.setNavigationOnClickListener{ onBackPressed()}
    }

    override fun onClick(v: View?) {
        if(v!=null){
            when (v.id){
                R.id.settings_edit_profile ->{
                    intent = Intent(this@SettingsActivity, ProfileActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
                    startActivity(intent)
                }

                R.id.settings_logout ->{
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun getUserDetails(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getUserDetails(this)
    }

    fun userDetailsSuccess(user: User) {
        hideProgressDialog()
        mUserDetails = user
    }

}