 package com.kdapps.offstore.UI.Fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kdapps.offstore.R
import com.kdapps.offstore.activities.BaseFragment
import com.kdapps.offstore.activities.SettingsActivity
import com.kdapps.offstore.firestore.FirestoreClass
import com.kdapps.offstore.models.PostImagesModel
import com.kdapps.offstore.utils.Constants
import com.kdapps.offstore.utils.HomeFragmentCustomAdapter
import com.kdapps.offstore.utils.UploadedImagesCustomAdapter
import io.grpc.internal.LogExceptionRunnable
import kotlinx.android.synthetic.main.activity_show_images_from_home.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class HomeFragment : BaseFragment() {

    //private lateinit var homeViewModel: HomeViewModel

    val imageRefs = mutableListOf<StorageReference>()
    private lateinit var likeReference: DatabaseReference
    val imagesUrls = mutableListOf<String>()
    private lateinit var database: DatabaseReference
    private var postKey: String = ""


    val storageRef = FirebaseStorage.getInstance().reference


    //to create options menu
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
        //homeViewModel =ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        likeReference = FirebaseDatabase.getInstance().getReference("Likes")
        showProgressDialogBox("Loading Content")
        listFiles()

        return root
    }

    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.i("First Name", "trying")
            val folders = storageRef.child(Constants.CONTENT_IMAGE_ROOT_FOLDER).listAll().await()


            //for caption
            val imageTitles = mutableListOf<String>()
            val imageOwner = mutableListOf<String>()
            val imageCategories = mutableListOf<String>()
            //val likeCounts = mutableListOf<String>()

            for(folder in folders.prefixes){
                Log.e("folder", folder.name)
                val categories = folder.listAll().await()

                for(category in categories.prefixes){
                    val images = category.listAll().await()

                    for(image in images.items){
                        val url = image.downloadUrl.await()
                        val TitleRef = image.metadata.await()

                        if (TitleRef!= null){
                            val title = TitleRef.getCustomMetadata("Title")
                            if (title!=null){
                                imageTitles.add(title)
                                Log.e("title", title)
                            }else{
                                imageTitles.add("")
                            }
                        }
                        imagesUrls.add(url.toString())
                        imageOwner.add(folder.name)
                        imageCategories.add(category.name)
                    }
                }
            }


            withContext(Dispatchers.Main){
                val imageAdapter = HomeFragmentCustomAdapter(imagesUrls, imageTitles, imageOwner, imageCategories)
                home_recycler_view.apply {
                    adapter = imageAdapter
                    layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                }
            }
            hideProgressDialog()

        }catch (e: Exception){
            withContext(Dispatchers.Main) {
                Log.e("Error",e.toString())
                activity?.let{ Toast.makeText(it, "Something went wrong", Toast.LENGTH_LONG).show() }
                hideProgressDialog()
            }
        }
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
}