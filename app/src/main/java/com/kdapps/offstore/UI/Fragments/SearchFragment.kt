package com.kdapps.offstore.UI.Fragments

import android.os.Bundle
import android.os.Looper
import android.provider.SyncStateContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
import com.kdapps.offstore.R
import com.kdapps.offstore.activities.BaseFragment
import com.kdapps.offstore.utils.Constants
import com.kdapps.offstore.utils.HomeFragmentCustomAdapter
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*


class SearchFragment : BaseFragment() {

    //private lateinit var dashboardViewModel: DashboardViewModel
    private var imageUrls = mutableListOf<String>()
    private var imageTitles = mutableListOf<String>()
    private var imageOwners = mutableListOf<String>()
    private var imageCategories = mutableListOf<String>()
    val sRef= FirebaseStorage.getInstance().reference

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //dashboardViewModel =ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        //dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
        val search_view = root.findViewById<SearchView>(R.id.search_view)

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                //clearing previous search
                if(query!= null) {
                    showProgressDialogBox("Searching Images")
                    imageUrls.clear()
                    imageTitles.clear()
                    imageOwners.clear()
                    imageCategories.clear()
                    searchQuery(query)
                }
                search_view.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }


        })


        return root
    }

    private fun searchQuery(query: String?) = CoroutineScope(Dispatchers.IO).launch{
        try {
            val users = sRef.child(Constants.CONTENT_IMAGE_ROOT_FOLDER).listAll().await()
            

            userLoop@for (user in users.prefixes){
                Log.e("user", users.prefixes.toString())
                val categories = user.listAll().await()

                categoryLoop@for(category in categories.prefixes){

                    val images = category.listAll().await()
                    if(category.name.toLowerCase(Locale.ROOT) == query?.toLowerCase(Locale.ROOT)) {
                        // return images from this folder to recyclerView and break this current loop
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
                            imageUrls.add(url.toString())
                            imageCategories.add(category.name)
                            imageOwners.add(user.name)
                        }
                        break@categoryLoop
//                        Looper.prepare()
//                        activity?.let { Toast.makeText(it,"category available", Toast.LENGTH_SHORT).show() }
//                        Looper.loop()
                    }else{

                        for(image in images.items){
                            val imageNameWithExt = image.name
                            val imageName = imageNameWithExt.substringBeforeLast(".")
                            Log.e("FinalImageName", imageName)
                            Log.e("imageName", image.name)
                                if(imageName.toLowerCase(Locale.ROOT) == query?.toLowerCase(Locale.ROOT)){
                                    // return  the particular image and continue the loop
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
                                    imageUrls.add(url.toString())
                                    imageCategories.add(category.name)
                                    imageOwners.add(user.name)
                                    break
                    //                                Looper.prepare()
                    //                                activity?.let { Toast.makeText(it,"image available", Toast.LENGTH_SHORT).show() }
                    //                                Looper.loop()

                                }else{
                    //                                Looper.prepare()
                    //                                activity?.let { Toast.makeText(it,"no images", Toast.LENGTH_SHORT).show() }
                    //                                Looper.loop()
                                }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main){

                val imageAdapter = HomeFragmentCustomAdapter(imageUrls, imageTitles,imageOwners, imageCategories)
                search_recyclerview.apply{
                    adapter = imageAdapter
                    layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    hideProgressDialog()
                    if(imageUrls.isEmpty()){
                        activity?.let { Toast.makeText(it, "No images found",Toast.LENGTH_LONG).show() }
                    }

                }
            }



        }catch (e: Exception){
            Log.e("search_error", e.toString())
            hideProgressDialog()
        }
    }


}