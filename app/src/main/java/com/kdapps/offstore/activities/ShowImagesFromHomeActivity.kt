package com.kdapps.offstore.activities

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kdapps.offstore.R
import com.kdapps.offstore.firestore.FirestoreClass
import com.kdapps.offstore.models.User
import com.kdapps.offstore.utils.Constants
import com.kdapps.offstore.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_show_images_from_home.*
import kotlinx.android.synthetic.main.activity_show_images_from_home.btn_like
import kotlinx.android.synthetic.main.activity_show_images_from_home.imageView
import kotlinx.android.synthetic.main.activity_show_images_from_home.tv_title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.lang.Exception

class ShowImagesFromHomeActivity : baseActivity(),View.OnClickListener {


    private lateinit var likeReference: DatabaseReference
    private lateinit var likeCounterRef: DatabaseReference
    private lateinit var database: DatabaseReference
    private var mImageUrl: String? = null
    private var ImageOwnerId:String? = null
    private lateinit var imageOwnerDetails: User
    private var postKey: String = ""
    private var testClick: Boolean = false
    private lateinit var imageCategory: String
    private lateinit var imageTitle: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_images_from_home)

        showProgressDialog("")

        getIncomingIntent()
        getImageOwnerDetails()

        //set the username of user who uploaded the imaGe

        likeReference = FirebaseDatabase.getInstance().getReference("Likes")
        Log.e("init_postKey", postKey)


        getPostKey(mImageUrl!!)
        btn_like.setOnClickListener(this)
        btn_download.setOnClickListener(this)
        btn_share.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        if(v!= null){
            when(v.id){
                R.id.btn_like ->{

                    val UserId = FirestoreClass().getCurrentUserID()
                    testClick = true
                    val postListener = object: ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {

                            if(testClick){
                                if(snapshot.child(postKey).hasChild(UserId)){
                                    likeReference.child(postKey).child(UserId).removeValue()
                                    testClick = false
                                    btn_like.setImageResource(R.drawable.before_like)
                                }else{
                                    likeReference.child(postKey).child(UserId).setValue(true)
                                    testClick = false
                                    btn_like.setImageResource(R.drawable.like)
                                }
                            }
                        }

                    }
                    likeReference.addValueEventListener(postListener)
                    Log.e("User Id", UserId)

                }

                R.id.btn_download ->{
                    askPermission()

                }
                R.id.btn_share ->{
                    shareImage(this, mImageUrl)
                }
            }
        }
    }

    private fun getIncomingIntent(){
        if (intent.hasExtra("imageUrl")){

            val imageUrl = intent.getStringExtra("imageUrl")
            mImageUrl = imageUrl
            Log.e("imageUrl", imageUrl!!)
            ImageOwnerId = intent.getStringExtra("imageOwner")
            imageTitle = intent.getStringExtra("imageTitle").toString()
            tv_title.text = imageTitle
            imageCategory = intent.getStringExtra("imageCategory").toString()
            tv_image_category.text = imageCategory
            like_count.text = intent.getStringExtra("likeCount").toString()
            if (imageUrl != null) {
                GlideLoader(this).loadUserPicture(imageUrl, imageView)
            }
        }
    }

    private fun getLikesStatus(){

        val userId = FirestoreClass().getCurrentUserID()
        var likesCount: Long? = 0


        likeReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }


            override fun onDataChange(snapshot: DataSnapshot) {

//                likesCount = snapshot.child(postKey).childrenCount
//                like_count.text = likesCount.toString()
                Log.e("snap", snapshot.value.toString())

                if(snapshot.child(postKey).hasChild(userId)) {
                    btn_like.setImageResource(R.drawable.like)
                    likesCount = snapshot.child(postKey).childrenCount
                    like_count.text = likesCount.toString()
                }else{
                    btn_like.setImageResource(R.drawable.before_like)
                    likesCount = snapshot.child(postKey).childrenCount
                    like_count.text = likesCount.toString()
                }

            }

        })

        hideProgressDialog()
    }



    private fun getPostKey(url: String): String{
        database = FirebaseDatabase.getInstance().reference


        database.child("post_images")
                .orderByChild("image_uri")
                .equalTo(url)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext,"Something went wrong",Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            //"it" is the snapshot
                            postKey = it.key.toString()
                            likeCounterRef = FirebaseDatabase.getInstance().getReference("Likes").child( postKey).child("likeCounter")
                            //getLikesCount()
                            getLikesStatus()

                            Log.e("postKey", postKey)

                        }
                    }
                })
        return postKey
    }


    // Alternative to count likes
//    private fun getLikesCount(){
//        try{
//            likeCounterRef.addValueEventListener(object : ValueEventListener{
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val likes = snapshot.value
//                    like_count.text = likes.toString()
//                    Log.e("finalLikes",likes.toString())
//                }
//
//            })
//        }catch (e:Exception){
//            Log.e("error", e.toString())
//        }
//    }



    //to get the details of the image owner
    private fun getImageOwnerDetails(){
            FirestoreClass().getImageOwnerDetails(this, ImageOwnerId!!)
    }

    fun ImageOwnerDetailsSuccess(user: User){
        imageOwnerDetails = user
        image_uploaded_by.text = "@" + imageOwnerDetails.username

    }



    private fun downloadImage(url: String){

        val directory = File(Environment.DIRECTORY_DOWNLOADS)
        if(!directory.exists()){
            directory.mkdirs()
        }

        val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(url.substring(url.lastIndexOf("/")+ 1))
                    .setDescription("Downloading file")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(directory.toString(),imageTitle+ ".jpg")
        Log.e("path",url.substring(url.lastIndexOf("/") + 1))

        }
        downloadManager.enqueue(request)

    }

    private fun askPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
               AlertDialog.Builder(this)
                        .setTitle("Permission required")
                        .setMessage("Permission required to save photos from the Web.")
                        .setPositiveButton("Accept"){dialog, id ->
                            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.READ_STORAGE_PERMISSION_CODE)
                            finish()
                        }
                        .setNegativeButton("Deny"){dialog, id -> dialog.cancel()}
                        .show()
            }
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.READ_STORAGE_PERMISSION_CODE)

        }else{
            downloadImage(mImageUrl!!)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            Constants.READ_STORAGE_PERMISSION_CODE ->{
                if((grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED)){
                    downloadImage(mImageUrl!!)
                }else{
                    Toast.makeText(applicationContext,"Permission is required to Download", Toast.LENGTH_LONG).show()
                }
                return
            }
            else ->{
                //ignore all other permissions
            }
        }
    }

    private fun shareImage(context: Context, imageUrl: String?){
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            val invite = "Downnload ContentHUb to access this image. Here is link for app: https://play.google.com/ContentHub"
            putExtra(Intent.EXTRA_TEXT, invite)
            type = "text/*"

//            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

}