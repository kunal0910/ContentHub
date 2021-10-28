package com.kdapps.offstore.utils

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.kdapps.offstore.R
import com.kdapps.offstore.activities.ShowImageFromUserProfileActivity
import com.kdapps.offstore.activities.ShowImagesFromHomeActivity
import com.kdapps.offstore.firestore.FirestoreClass
import kotlinx.android.synthetic.main.home_post_layout.view.*
import java.net.URL

class HomeFragmentCustomAdapter(private val imagesList: List<String>, private val imageTitles: List<String>,
                                private val imageOwner: List<String>, private val imageCategories: List<String>):
    RecyclerView.Adapter<HomeFragmentCustomAdapter.ColorViewHolder>(){
    //private lateinit var database: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_post_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val path = imagesList[position]
        val title = imageTitles[position]
        val owner = imageOwner[position]
        val category = imageCategories[position]
        Log.e("caption", title)
        Glide
                .with(holder.itemView)
                .load(path)
                .into(holder.image_holder)




        val userId = FirestoreClass().getCurrentUserID()
        // to get the key of the image



        holder.itemView.setOnClickListener(){
            //Toast.makeText(holder.itemView.context,"Yet not implemented",Toast.LENGTH_SHORT).show()

            val intent = Intent(holder.itemView.context, ShowImagesFromHomeActivity::class.java)
            intent.putExtra("imageUrl",path)
            intent.putExtra("imageTitle",title)
            intent.putExtra("imageOwner",  owner)
            intent.putExtra("imageCategory",category)
            holder.itemView.context.startActivity(intent)
        }
    }

    class ColorViewHolder(view: View): RecyclerView.ViewHolder(view){
        val image_holder = view.content_image_holder as ImageView
    }
}