package com.kdapps.offstore.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.kdapps.offstore.firestore.FirestoreClass

object Constants {

    const  val USERS: String = "users"
    const  val MYSHAREDPREFRENCES: String = "mySharedPrefs"
    const  val LOGGEDINUSERNAME: String = "logged_in_username"
    const  val EXTRA_USER_DETAILS : String = "extra_user_details"
    const  val READ_STORAGE_PERMISSION_CODE: Int = 2
    const  val PICK_IMAGE_REQUEST_CODE: Int = 2
    const  val USER_PROFILE_IMAGE: String = "user_profile_image"
    val CONTENT_IMAGE_FOLDER: String = "content_images/${FirestoreClass().getCurrentUserID()}/"
    val CONTENT_IMAGE_ROOT_FOLDER: String = "content_images/"
    const  val CONTENT_IMAGE_CAPTION: String = "Title"

    const val MALE: String = "male"
    const val FEMALE: String = "female"
    const val FULL_NAME: String = "fullName"
    const val USERNAME: String = "username"
    const val GENDER: String = "gender"
    const val MOBILE: String = "mobile"
    const val IMAGE: String = "image"
    const val PROFILE_COMPLETED: String = "profileCompleted"


    fun showImageChooser(activity: Activity){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        galleryIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String?{
        /*
        *MimeTypeMap: Two way map that maps MIME-type to file extensions and vice-versa.
        *
        * getSingleton(): Get the Singleton instance of the MimeTypeMap
        *
        * getExtensionFromMimeType: Return the registered extension for given MIME-type
        *
        * contentResolver.getType: Return the MIME type of the given content URL
        */
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}