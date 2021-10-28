package com.kdapps.offstore.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.kdapps.offstore.R
import kotlinx.android.synthetic.main.activity_profile2.*
import java.io.IOException

class GlideLoader(val context: Context) {
    fun loadUserPicture(image: Any, imageView: ImageView){
        try {
            Glide
                .with(context)
                .load(image)
                .placeholder(R.drawable.profile_image)
                .into(imageView)
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

}