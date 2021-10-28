package com.kdapps.offstore.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.kdapps.offstore.R
import kotlinx.android.synthetic.main.progress_dialog.*

open class baseActivity : AppCompatActivity() {

    private lateinit var mProgressDialog: Dialog

    private var  doubleBackToExitPressedOnce = false

    fun showErrorSnackBar(message: String, errorMessage: Boolean){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)
        val snackBarView= snackBar.view

        if(errorMessage){
            snackBarView.setBackgroundColor(
                    ContextCompat.getColor(this@baseActivity,R.color.colorSnackError)
            )
        }else{
            snackBarView.setBackgroundColor(
                    ContextCompat.getColor(this@baseActivity,R.color.colorSnack)
            )
        }
        snackBar.show()
    }

    fun showProgressDialog(text: String){
        mProgressDialog = Dialog(this)

        mProgressDialog.setContentView(R.layout.progress_dialog)

        mProgressDialog.progress_text.text = text

        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)

        //start progress dialog
        mProgressDialog.show()
    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun doubleBackToExit(){
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this,"Click again to Exit.", Toast.LENGTH_SHORT).show()

        @Suppress("DEPRECATION")
        Handler().postDelayed({doubleBackToExitPressedOnce = false}, 2000)
    }
}