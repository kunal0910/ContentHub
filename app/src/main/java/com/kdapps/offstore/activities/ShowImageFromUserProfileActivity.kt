package com.kdapps.offstore.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.storage.FirebaseStorage
import com.kdapps.offstore.R
import com.kdapps.offstore.UI.Fragments.UserProfileFragment
import com.kdapps.offstore.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_show_image_from_user_profile.*

class ShowImageFromUserProfileActivity : AppCompatActivity() {

    val storageRef = FirebaseStorage.getInstance().reference
    private var mImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image_from_user_profile)
        setSupportActionBar(toolbar_showing_image)
        val actionBar = supportActionBar
        actionBar?.title = "Post"
        getIncomingIntent()


    }

    private fun getIncomingIntent(){
        if (intent.hasExtra("imageUrl")){

            val imageUrl = intent.getStringExtra("imageUrl")
            mImageUrl = imageUrl
            val imageTitle = intent.getStringExtra("imageTitle")

            tv_title.text = imageTitle
            if (imageUrl != null) {
                GlideLoader(this).loadUserPicture(imageUrl, imageView)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.showing_image_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id){
            R.id.action_delete -> {
                mImageUrl?.let { showDialog(it) }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun showDialog(imageUrl: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(resources.getString(R.string.delete_image_confirm))
        dialogBuilder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, whichButton ->2

                val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                imageRef.delete()
                Toast.makeText(applicationContext,"Deleted post successfully", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@ShowImageFromUserProfileActivity, UserProfileFragment::class.java))
            })
        dialogBuilder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener{dialog, whichButton ->
        })
        val b = dialogBuilder.create()
        b.show()
    }
    private fun shareImage(context: Context){
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


