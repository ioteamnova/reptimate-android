package com.reptimate.iot_teamnova.Diary

import android.content.Context
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.reptimate.iot_teamnova.R
import com.github.chrisbanes.photoview.PhotoView

class DiaryImageDetailAdapter (var mContext: Context, var imageList: ArrayList<String> ) :
    RecyclerView.Adapter<DiaryImageDetailAdapter.PagerViewHolder>() {

    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.item_image_diary_detail, parent, false)) {
        val imageView = itemView.findViewById<PhotoView>(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder((parent))

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        Glide.with(mContext).load(imageList[position]).into(holder.imageView)

    }
}