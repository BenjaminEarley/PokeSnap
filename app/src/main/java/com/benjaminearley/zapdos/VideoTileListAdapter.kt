package com.benjaminearley.zapdos

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import java.util.*

class VideoTileListAdapter(val videoList: ArrayList<Bitmap>) : RecyclerView.Adapter<VideoTileListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v = LayoutInflater.from(parent.context).inflate(R.layout.video_tile_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val listItem = videoList[position]
        holder.number.setImageBitmap(listItem)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val number = v.findViewById(R.id.videoThumbnail) as ImageView
    }
}
