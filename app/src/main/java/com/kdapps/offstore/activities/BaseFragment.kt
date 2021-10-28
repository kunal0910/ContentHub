package com.kdapps.offstore.activities

import android.app.Dialog
import androidx.fragment.app.Fragment
import com.kdapps.offstore.R
import kotlinx.android.synthetic.main.progress_dialog.*

open class BaseFragment: Fragment() {
    private lateinit var mProgressDialog: Dialog

    fun showProgressDialogBox(text: String){
        mProgressDialog = activity?.let { Dialog(it) }!!
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
}