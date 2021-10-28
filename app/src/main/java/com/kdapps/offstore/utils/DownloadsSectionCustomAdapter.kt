package com.kdapps.offstore.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kdapps.offstore.R

class DownloadsSectionCustomAdapter(private val downloads_list: List<String>)
    :RecyclerView.Adapter<DownloadsSectionCustomAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ):ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.download_item_layout,parent, false))
    }

    override fun getItemCount(): Int {
        return downloads_list.size
    }

    override fun onBindViewHolder(holder: DownloadsSectionCustomAdapter.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    // ViewHolder class
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {


    }
}