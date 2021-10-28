package com.kdapps.offstore.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kdapps.offstore.R
import com.kdapps.offstore.firestore.FirestoreClass
import com.kdapps.offstore.models.PostImagesModel
import com.kdapps.offstore.utils.Constants
import com.kdapps.offstore.utils.GlideLoader
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_upload_image.*

class UploadImageActivity : baseActivity(), View.OnClickListener {

    private lateinit var database: DatabaseReference
    private var mSelectedImageFileUri: Uri? = null
    private var mImageFileExtension: String? = null
    private val TAG = "AppDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_image)

        show_selected_image.setOnClickListener(this)
        val categories = resources.getStringArray(R.array.image_categories)

        if(image_category_spinner!=null) {
            val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, categories)
            image_category_spinner.adapter = adapter

            image_category_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    Log.e("category", image_category_spinner.selectedItemPosition.toString())
                }

            }

        }

           


        if (ContextCompat.checkSelfPermission(
                this@UploadImageActivity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            //showErrorSnackBar("you already have the storage permission.", false)
            Constants.showImageChooser(this)

        } else {
            //requests permission to be granted to this application
            ActivityCompat.requestPermissions(
                this@UploadImageActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.READ_STORAGE_PERMISSION_CODE
            )
        }
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
        when(requestCode) {
            Constants.PICK_IMAGE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {

                    if (data != null) {
                        mSelectedImageFileUri = data.data!!
                    }
                    mImageFileExtension = Constants.getFileExtension(this,mSelectedImageFileUri)
                    data?.data?.let { uri ->
                        launchCropImage(uri)
                    }
                }else{
                    Log.e(TAG, "Image selection error" )
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE->{
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK){
                    result.uri?.let {
                        GlideLoader(this).loadUserPicture(result.uri,show_selected_image)
                        btn_upload_final.setOnClickListener{
                            if (validateImageDetails()) {
                                showProgressDialog(resources.getString(R.string.uploading_image))
                                val title = et_add_title.text.toString()
                                val category = image_category_spinner.selectedItem.toString()

                                FirestoreClass().uploadContentImagesToCloud(this@UploadImageActivity, result.uri,
                                        Constants.CONTENT_IMAGE_CAPTION, et_add_title.text.toString(), category)

//
                            }
                        }

                    }

                }
                else if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Log.e(TAG, "Crop error ${result.error}" )

                }
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.show_selected_image ->{
                if (ContextCompat.checkSelfPermission(
                                this@UploadImageActivity,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        == PackageManager.PERMISSION_GRANTED
                ) {
                    //showErrorSnackBar("you already have the storage permission.", false)
                    Constants.showImageChooser(this)

                } else {
                    //requests permission to be granted to this application
                    ActivityCompat.requestPermissions(
                            this@UploadImageActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                    )
                }
            }
        }
    }

    private fun launchCropImage(uri: Uri){
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(this)
    }

    private fun validateImageDetails():Boolean{
        return when{
            TextUtils.equals(image_category_spinner.selectedItemPosition.toString(),"0") ->{
                showErrorSnackBar("Please select the image category", true)
                false
            }
            TextUtils.isEmpty(et_add_title.text.toString().trim{it<=' '})->{
                showErrorSnackBar("Please enter the image title", true)
                false
            }

            else -> {
                true
            }
        }
    }

    fun imageUploadSuccess(){
        hideProgressDialog()
        Toast.makeText(applicationContext,resources.getString(R.string.image_upload_success),Toast.LENGTH_SHORT)
                .show()
        startActivity(Intent(this@UploadImageActivity, DashboardActivity::class.java))

    }

    fun imageFileExtension(): String?{
        return mImageFileExtension
    }

}


