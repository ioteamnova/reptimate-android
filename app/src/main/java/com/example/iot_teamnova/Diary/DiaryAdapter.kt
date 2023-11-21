package com.example.iot_teamnova.Diary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.iot_teamnova.R

class DiaryAdapter(val context: Context, val itemList: ArrayList<DiaryItem>) :
    RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val idx = itemList[position].idx
        val title = itemList[position].title
        val content = itemList[position].content
        val imagePath = itemList[position].imagePath
        val year = itemList[position].year
        val date = itemList[position].date
        val petIdx = itemList[position].petIdx

        holder.year.text = year
        holder.date.text = date

        holder.title.text = title // 이름

        // 썸네일 이미지
        if(imagePath != "" && imagePath != "null"){
            Glide.with(context).load(imagePath).centerCrop().into(holder.image)
            holder.image.clipToOutline = true
        }
        else {
            Glide.with(context).load(R.drawable.null_image).into(holder.image)
        }

        holder.item.setOnClickListener {
            val i = Intent(context, DiaryDiaryViewAcitivity::class.java)
            i.putExtra("idx", idx)
            i.putExtra("title", title)
            i.putExtra("content", content)
            i.putExtra("imagePaths", imagePath)
            i.putExtra("petIdx", petIdx)
            context.startActivity(i)

        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image = itemView.findViewById<ImageView>(R.id.image)
        val title = itemView.findViewById<TextView>(R.id.title)
        val year = itemView.findViewById<TextView>(R.id.year)
        val date = itemView.findViewById<TextView>(R.id.date)

        val item = itemView.findViewById<LinearLayout>(R.id.diary_item)

        fun bind(imageUrl: String) {
            Glide.with(itemView)
                .load(imageUrl)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image)
        }
    }
}