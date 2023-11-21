package com.example.iot_teamnova.Diary

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iot_teamnova.R

class DiaryViewPagerAdapter (var mContext: Context, var imageList: ArrayList<String> ) :
    RecyclerView.Adapter<DiaryViewPagerAdapter.PagerViewHolder>() {


    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.item_image_diary_view, parent, false)) {
        val imageView = itemView.findViewById<ImageView>(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder((parent))

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        Glide.with(mContext).load(imageList[position]).centerCrop().into(holder.imageView)

        holder.imageView.setOnClickListener {
            val i = Intent(mContext, DiaryDiaryViewImageDetailActivity::class.java)
            i.putExtra("imageList", imageList)
            i.putExtra("imgPosition", position)
            mContext.startActivity(i.addFlags(FLAG_ACTIVITY_NEW_TASK))
        }
    }
}