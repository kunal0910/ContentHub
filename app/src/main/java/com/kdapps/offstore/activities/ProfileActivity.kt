package com.kdapps.offstore.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.kdapps.offstore.R
import com.kdapps.offstore.firestore.FirestoreClass
import com.kdapps.offstore.models.User
import com.kdapps.offstore.utils.Constants
import com.kdapps.offstore.utils.GlideLoader
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_profile2.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.IOException
import java.util.jar.Manifest


class ProfileActivity : baseActivity(), View.OnClickListener {


    private lateinit var userDetails: User
    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageUrl: String = ""
    private var finalImageUri: Uri? =null
    private val TAG = "AppDebug"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile2)


        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)){
            userDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!

        }

        et_full_name.setText(userDetails.fullName)
        et_user_name.setText(userDetails.username)
        Log.e("username", userDetails.username)

        et_email.isEnabled = false
        et_email.setText(userDetails.email)

        if (userDetails.profileCompleted == 0){

            profile_title.text = resources.getString(R.string.complete_profile)
            et_full_name.isEnabled = false
            et_user_name.isEnabled = false

        }
        else{
            setupActionBar()
            profile_title.text = resources.getString(R.string.edit_profile)
            et_full_name.isEnabled = true
            et_user_name.isEnabled = true
            et_phone.setText(userDetails.mobile.toString())
            if(userDetails.gender == Constants.MALE){
                radio_button_male.isChecked = true
            }else{
                radio_button_female.isChecked = true
            }
            GlideLoader(this).loadUserPicture(userDetails.image, profile_image)

        }




        profile_image.setOnClickListener(this@ProfileActivity)
        btn_save.setOnClickListener(this@ProfileActivity)

    }

    override fun onClick(v: View?) {
        if(v != null){
            when (v.id) {
                R.id.profile_image -> {


                    //Here we will check whether we have the permission to EXTERNAL_STORAGE or we need to request
                    if (ContextCompat.checkSelfPermission(
                            this@ProfileActivity,
                           android.Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        //showErrorSnackBar("you already have the storage permission.", false)
                        Constants.showImageChooser(this)

                    } else {
                        //requests permission to be granted to this application
                        ActivityCompat.requestPermissions(
                            this@ProfileActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }

                R.id.btn_save ->{

                    if(validateUserDetails()){

                        showProgressDialog(resources.getString(R.string.please_wait))

                        if (mSelectedImageFileUri!= null) {
                            FirestoreClass().uploadImageToCloudStorage(this@ProfileActivity, finalImageUri)
                        }
                        else{
                            userProfileUpdateDetails()
                        }
                    }
                    //showErrorSnackBar("Details are valid.", false)
                    
                }
            }
        }
    }

    private fun userProfileUpdateDetails(){
        val userHashMap = HashMap<String,Any>()

        val fullName = et_full_name.text.toString().trim{it <= ' '}
        val username = et_user_name.text.toString().trim{it <= ' '}
        val mobileNumber = et_phone.text.toString().trim{it <= ' '}


        val gender = if (radio_button_male.isChecked){
            Constants.MALE
        }else{
            Constants.FEMALE
        }

        if(fullName.isNotEmpty() && fullName!= userDetails.fullName){
            userHashMap[Constants.FULL_NAME]= fullName
        }

        if(username.isNotEmpty() && username!= userDetails.username){
            userHashMap[Constants.USERNAME]= username
        }

        if(mobileNumber.isNotEmpty() && mobileNumber!= userDetails.mobile.toString()){
            userHashMap[Constants.MOBILE]= mobileNumber.toLong()
        }

        if(mProfileImageUrl.isNotEmpty()){
            userHashMap[Constants.IMAGE] = mProfileImageUrl
        }

        if(gender!= userDetails.gender) {
            userHashMap[Constants.GENDER] = gender
        }
        userHashMap[Constants.PROFILE_COMPLETED] = 1

        //showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this@ProfileActivity, userHashMap)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions,grantResults)
        if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
            //if permission is granted
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //showErrorSnackBar("The storage permission is granted.", false)
                Constants.showImageChooser(this)

            }
            else{
                Toast.makeText(this,"Storage permission is not granted. allow it from Settings", Toast.LENGTH_LONG).show()
            }

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            Constants.PICK_IMAGE_REQUEST_CODE ->{
                if(resultCode == Activity.RESULT_OK){
                    if(data!= null) {
                        mSelectedImageFileUri = data.data!!
                    }

//                        mImageFileExtension = Constants.getFileExtension(this,mSelectedImageFileUri)
                    data?.data?.let { uri ->
                        launchCropImage(uri)
                        }
                }else{
                    Log.e(TAG, "Image selection error")
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->{
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK){
                    result.uri?.let {
                        finalImageUri = result.uri
                        GlideLoader(this).loadUserPicture(result.uri, profile_image)

                    }
                }else if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Log.e(TAG, "Crop error ${result.error}" )

                }

            }
        }
    }

    fun userProfileUpdateSuccess(){
        hideProgressDialog()
        Toast.makeText(this,resources.getString(R.string.profile_updated),Toast.LENGTH_LONG).show()

        startActivity(Intent(this@ProfileActivity, DashboardActivity::class.java))
        finish()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when(requestCode) {
//            Constants.PICK_IMAGE_REQUEST_CODE -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    if (data != null) {
//                        try {
//                            mSelectedImageFileUri = data.data!!
//
//                            //profile_image.setImageURI(Uri.parse(selectedImageFileUri.toString()))
//                            mImageFileExtension = Constants.getFileExtension(this, mSelectedImageFileUri)
//                            data?.data?.let { uri ->
//                                launchCropImage(uri)
//                            }
//
//                            GlideLoader(this).loadUserPicture(mSelectedImageFileUri!!, profile_image)
//
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                            Toast.makeText(this, "Unable to set profile picture", Toast.LENGTH_LONG).show()
//                        }
//
//                    }
//                }
//
//            }
//            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->{
//
//            }
//        }
//    }
//





    private fun validateUserDetails(): Boolean{
        return when{
            TextUtils.isEmpty(et_phone.text.toString().trim{it <= ' '}) ->{
                showErrorSnackBar(resources.getString(R.string.err_mobile_number),true)
                false
            }
            else ->{
                true
            }
        }
    }

    fun imageUploadSuccess(imageURL: String){
        //Toast.makeText(this@ProfileActivity, "Image is uploaded successfully. Image url is: $imageURL",Toast.LENGTH_LONG).show()
        mProfileImageUrl = imageURL
        userProfileUpdateDetails()
    }
    private  fun setupActionBar(){
        setSupportActionBar(toolbar_profile)

        val actionBar = supportActionBar
        if(actionBar!= null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }
        toolbar_profile.setNavigationOnClickListener{ onBackPressed()}
    }

    private fun launchCropImage(uri: Uri){
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(this)
    }

}