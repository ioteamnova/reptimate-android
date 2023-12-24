package com.reptimate.iot_teamnova.customAlbum

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reptimate.iot_teamnova.Diary.DiaryWriteImageAdapter
import com.reptimate.iot_teamnova.databinding.ItemPhotoBinding

class CustomAlbumAdapter(private var itemList: ArrayList<ItemGallery>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<CustomAlbumAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(isChecked: Boolean, item: ItemGallery)
    }

    inner class MyViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemGallery) {
            binding.tag = item
            binding.layoutCL.setOnClickListener {
                item.isSelected = !binding.selectRatioBT.isChecked
                binding.selectRatioBT.isChecked = item.isSelected
                listener.onItemClick(item.isSelected, item)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val listItemBinding = ItemPhotoBinding.inflate(inflater, parent, false)
        return MyViewHolder(listItemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setData(itemList: ArrayList<ItemGallery>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }
}