package com.kdapps.offstore.firestore

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.kdapps.offstore.R
import com.kdapps.offstore.UI.Fragments.UserProfileFragment
import com.kdapps.offstore.activities.*
import com.kdapps.offstore.models.PostImagesModel
import com.kdapps.offstore.models.User
import com.kdapps.offstore.utils.Constants

class FirestoreClass {
        private lateinit var database: DatabaseReference
        private val mFirestore = FirebaseFirestore.getInstance()
        private var userName_id: HashMap<String, String> = HashMap()
        fun registerUser(activity: registerActivity, userInfo: User){
                //the "users" is a collection name
                mFirestore.collection(Constants.USERS)
                //Document Id for users Fields
                        .document(userInfo.id)
                //Here th userInfo are the fields and setOption is set to merge. It is for if we want later to merge or replace fields
                        .set(userInfo, SetOptions.merge())
                        .addOnSuccessListener {
                                activity.userRegistrationSuccess()

                        }
                        .addOnFailureListener(){e ->
                                activity.hideProgressDialog()
                                Log.e(
                                        activity.javaClass.simpleName, "Error while registering the user.", e
                                )
                        }

        }
        
        fun getCurrentUserID(): String{
                val currentUser = FirebaseAuth.getInstance().currentUser

                var currentUserID = ""
                if(currentUser!= null){
                        currentUserID = currentUser.uid
                }
                return currentUserID
        }

        fun getImageOwnerDetails(activity: Activity, userId: String){
            mFirestore.collection(Constants.USERS)

                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        Log.i(activity.javaClass.simpleName, document.toString())

                        // Here we have received thee document snapshot which is converted into thr User data model object.
                        val user = document.toObject(User::class.java)!!

                        when(activity){
                            is ShowImagesFromHomeActivity ->{
                                activity.ImageOwnerDetailsSuccess(user)
                            }
                        }
                    }.addOnFailureListener{e ->
                        when(activity){
                            is ShowImagesFromHomeActivity ->{
                                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_LONG).show()
                                activity.hideProgressDialog()
                            }
                        }
                    }

        }

        fun getUserDetails(activity: Activity){
                mFirestore.collection(Constants.USERS)

                        .document(getCurrentUserID())
                        .get()
                        .addOnSuccessListener { document ->
                                Log.i(activity.javaClass.simpleName,document.toString())

                                //Here we have received the document snapshot which is converted into the User Data model object.

                                val user = document.toObject(User::class.java)!!

                                val sharedPreferences = activity.getSharedPreferences(Constants.MYSHAREDPREFRENCES, Context.MODE_PRIVATE)
                                // Key:value  logged_in_username : firstName lastName

                                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                editor.putString(Constants.LOGGEDINUSERNAME, user.fullName)
                                editor.apply()
                                //START
                                when(activity) {
                                        is LoginActivity -> {
                                                activity.userLoggedInSuccess(user)
                                        }

                                        is SettingsActivity ->{
                                            activity.userDetailsSuccess(user)
                                        }

                                }


                        }
                        .addOnFailureListener(){e ->
                                when(activity){
                                        is LoginActivity->{
                                                Toast.makeText(activity, "Unable to Login", Toast.LENGTH_LONG).show()
                                                activity.hideProgressDialog()
                                        }
                                }
                        }
        }

        //For fragment defining same function
        fun getUserDetails(fragment: Fragment){
            mFirestore.collection(Constants.USERS)

                    .document(getCurrentUserID())
                    .get()
                    .addOnSuccessListener { document ->
                        Log.i(fragment.javaClass.simpleName,document.toString())

                        //Here we have received the document snapshot which is converted into the User Data model object.

                        val user = document.toObject(User::class.java)!!

                        /*
                        val sharedPreferences = fragment.getSharedPreferences(Constants.MYSHAREDPREFRENCES, Context.MODE_PRIVATE)
                        // Key:value  logged_in_username : firstName lastName

                        val editor: SharedPreferences.Editor = sharedPreferences.edit()
                        editor.putString(Constants.LOGGEDINUSERNAME,"${user.firstName} ${user.lastName}")
                        editor.apply()
                        */

                        //START
                        when(fragment) {
                            is UserProfileFragment ->{
                                fragment.userDetailsSuccess(user)
                            }

                        }


                    }
                    .addOnFailureListener(){e ->
                       when(fragment){
                            is UserProfileFragment ->{
                                fragment.unableToGetData()
                            }
                        }
                    }
        }

        fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>){
            mFirestore.collection(Constants.USERS)
                    .document(getCurrentUserID())
                    .update(userHashMap)
                    .addOnSuccessListener {
                        when(activity){
                            is ProfileActivity ->{

                                activity.userProfileUpdateSuccess()
                            }
                        }
                    }
                    .addOnFailureListener{e->
                        when(activity){
                            is ProfileActivity ->{
                                activity.hideProgressDialog()
                            }
                        }
                        Log.e(
                                activity.javaClass.simpleName,"Error while updating the details",e
                        )
                    }
        }

        fun uploadImageToCloudStorage(activity: Activity, ImageFileUri: Uri?){
            
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(Constants.USER_PROFILE_IMAGE +
            System.currentTimeMillis() + "." + Constants.getFileExtension(activity, ImageFileUri))

            sRef.putFile(ImageFileUri!!).addOnSuccessListener { taskSnapshot->
                //  Image upload is success
                Log.e("Firebase image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                // get the downloadable url from task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.e("Downloadable image URL", uri.toString())
                    when(activity){
                        is ProfileActivity ->{
                            activity.imageUploadSuccess(uri.toString())
                        }
                        is UploadImageActivity ->{
                            activity.imageUploadSuccess()
                        }
                    }
                }


            }.addOnFailureListener{exception ->
                when (activity){
                    is ProfileActivity ->{
                        activity.hideProgressDialog()
                    }
                    is UploadImageActivity ->{
                        activity.hideProgressDialog()
                        Toast.makeText(activity,"Something went wrong",Toast.LENGTH_SHORT)
                                .show()
                    }
                }
                Log.e(activity.javaClass.simpleName, exception.message, exception)
            }
        }

        fun uploadContentImagesToCloud(activity: UploadImageActivity, imageUri: Uri?,mCustomProperty: String, mImageTitle: String, category: String){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(Constants.CONTENT_IMAGE_FOLDER +
                    category+ "/"+ mImageTitle + "." + activity.imageFileExtension())

            sRef.putFile(imageUri!!).addOnSuccessListener { taskSnapshot->
                //  Image upload is success
                Log.e("downloadUrl", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                //get reference to file to upload custom data i.e caption
                val captionRef = FirebaseStorage.getInstance().reference.child(sRef.path)
                val metaData = storageMetadata {
                    setCustomMetadata(mCustomProperty, mImageTitle)
                }

                // Update metadata properties
                captionRef.updateMetadata(metaData).addOnSuccessListener { updatedMetadata ->
                    // Updated metadata is in updatedMetadata
                }.addOnFailureListener {
                    // Uh-oh, an error occurred!
                }



                // get the downloadable url from task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.e("finalUrl", uri.toString())
                    //linking the image to the Realtime database

                    val postImageModel = PostImagesModel(uri.toString(), mImageTitle)
                    database = FirebaseDatabase.getInstance().reference
                    val id = database.push().key
                    database.child("post_images").child(id!!).setValue(postImageModel)
                    when(activity){
                        is UploadImageActivity ->{
                            activity.imageUploadSuccess()
                        }
                    }
                }


            }.addOnFailureListener{exception ->
                when (activity){
                    is UploadImageActivity ->{
                        activity.hideProgressDialog()
                        Toast.makeText(activity,"Something went wrong",Toast.LENGTH_SHORT)
                                .show()
                    }
                }
                Log.e(activity.javaClass.simpleName, exception.message, exception)
            }


        }




}