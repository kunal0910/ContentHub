package com.kdapps.offstore.UI.Fragments


import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kdapps.offstore.R
import com.kdapps.offstore.activities.BaseFragment
import com.kdapps.offstore.activities.SettingsActivity
import com.kdapps.offstore.activities.UploadImageActivity
import com.kdapps.offstore.activities.baseActivity
import com.kdapps.offstore.firestore.FirestoreClass
import com.kdapps.offstore.models.User
import com.kdapps.offstore.utils.Constants
import com.kdapps.offstore.utils.GlideLoader
import com.kdapps.offstore.utils.UploadedImagesCustomAdapter
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.progress_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception


class UserProfileFragment : BaseFragment(),View.OnClickListener {

    private lateinit var mProgressDialog: Dialog

    //private lateinit var notificationsViewModel: NotificationsViewModel
    val imageRefs = mutableListOf<StorageReference>()
    val imagesUrls = mutableListOf<String>()


    val storageRef = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // if we want to use option menu in fragment we need to add it
        setHasOptionsMenu(true)


    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        getUserDetails()
        btn_upload?.setOnClickListener(){
            activity?.let { Toast.makeText(it,"okkk",Toast.LENGTH_SHORT).show() }
        }
        //notificationsViewModel =ViewModelProvider(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_user_profile, container, false)
        //val textView: TextView = root.findViewById(R.id.text_notifications)
        //notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = "This is Notification Fragment."
        val btn_upload: Button = root.findViewById(R.id.btn_upload)
        btn_upload.setOnClickListener(this)
        showProgressDialogBox("Loading your profile")

        listFiles()

        return root

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id){
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getUserDetails(){
        FirestoreClass().getUserDetails(this)

    }

    fun userDetailsSuccess(user: User) {
        activity?.let { GlideLoader(it).loadUserPicture(user.image, user_profile_image) }
        val user_name = user.username
        user_profile_name.text = user_name
        pop_level_int.text = "0"
        hideProgressDialog()

    }

    fun unableToGetData(){
        Log.e("Error mesaage","Unable to get data from firestore")
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_upload ->{
                val intent = activity?.let { Intent(it, UploadImageActivity::class.java) }
                startActivity(intent)
            }
        }
    }

    //to show uploaded content images
    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val categories = storageRef.child(Constants.CONTENT_IMAGE_FOLDER).listAll().await()

            //for caption
            val imageTitles = mutableListOf<String>()

            for(category in categories.prefixes){

                val images = category.listAll().await()

                for(image in images.items){
                    Log.i( "Image", image.toString())
                    imageRefs.add(image)
                    val url = image.downloadUrl.await()
                    val titleRef = image.metadata.await()
                    if (titleRef != null) {
                        val title =titleRef.getCustomMetadata("Title")
                        if (title != null) {
                            imageTitles.add(title)
                        }else{
                            imageTitles.add("")
                        }
                    }

                    imagesUrls.add(url.toString())
                }
            }


            withContext(Dispatchers.Main){
                val imageAdapter = UploadedImagesCustomAdapter(imagesUrls, imageTitles, imageRefs)
                recycler_view_images.apply {
                    adapter = imageAdapter
                    layoutManager = StaggeredGridLayoutManager(3,LinearLayoutManager.VERTICAL)
                }
            }
            hideProgressDialog()

        }catch (e: Exception){
            withContext(Dispatchers.Main) {
                activity?.let{ Toast.makeText(it, e.message, Toast.LENGTH_LONG).show() }
            }
            hideProgressDialog()
        }
    }



}