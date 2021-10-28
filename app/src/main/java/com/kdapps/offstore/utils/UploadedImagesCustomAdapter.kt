package com.kdapps.offstore.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.google.firebase.storage.StorageReference
import com.kdapps.offstore.R
import com.kdapps.offstore.activities.MainActivity
import com.kdapps.offstore.activities.ShowImageFromUserProfileActivity
import kotlinx.android.synthetic.main.content_images_grid_layout.view.*

class UploadedImagesCustomAdapter(private val imagesList: List<String>, private val imageTitles: List<String>, private val imageRefs: List<StorageReference>):
        RecyclerView.Adapter<UploadedImagesCustomAdapter.ColorViewHolder>() {


    override fun getItemCount(): Int {
        return imagesList.size
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_images_grid_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val path = imagesList[position]
        val title = imageTitles[position]
        val imageRef = imageRefs[position]
        Glide
                .with(holder.itemView)
                .load(path)
                .centerCrop()
                .into(holder.image_holder)

        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,ShowImageFromUserProfileActivity::class.java)
            intent.putExtra("imageUrl",path)
            intent.putExtra("imageTitle",title)
            intent.putExtra("imageRef",imageRef.path)
            holder.itemView.context.startActivity(intent)
        }
        
    }




    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image_holder = view.content_image as ImageView

    }
}