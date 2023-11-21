package com.reptimate.iot_teamnova.Diary

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.reptimate.iot_teamnova.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

class DiaryWriteImageAdapter(var mContext: Context, var mData: ArrayList<Uri>, private val recyclerView: RecyclerView, private val listener: OnItemClickListener, private val mlistener: onItemMoveListener ) :
    RecyclerView.Adapter<DiaryWriteImageAdapter.ViewHolder>(), ItemTouchHelperListener {

    override fun onItemMove(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                Collections.swap(mData, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(mData, i, i - 1)
            }
        }

        notifyItemMoved(from, to)

        for (i in min(from, to)..max(from, to)) {
            onBindViewHolder(getViewHolderForPosition(i), i)
        }

        mlistener.onItemMove(from, to, mData)
    }

    private fun getViewHolderForPosition(position: Int): ViewHolder {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
        return viewHolder as ViewHolder
    }

    override fun onItemSwipe(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onLeftClick(position: Int, viewHolder: RecyclerView.ViewHolder?) {
        TODO("Not yet implemented")
    }

    override fun onRightClick(position: Int, viewHolder: RecyclerView.ViewHolder?) {
        TODO("Not yet implemented")
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, mData: ArrayList<Uri>)
    }

    interface onItemMoveListener {
        fun onItemMove(from: Int, to: Int, mData: ArrayList<Uri>)

    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    class ViewHolder internal constructor(var rowView: View) : RecyclerView.ViewHolder(
        rowView
    ) {

        var image: ImageView = itemView.findViewById<ImageView>(R.id.image)
        var deleteBtn: ImageButton = itemView.findViewById<ImageButton>(R.id.delete_button)

        init {

            // 뷰 객체에 대한 참조.
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater // context에서 LayoutInflater 객체를 얻는다.
        val view: View = inflater.inflate(
            R.layout.item_image_diary_write,
            parent,
            false
        ) // 리사이클러뷰에 들어갈 아이템뷰의 레이아웃을 inflate.
        return ViewHolder(view)
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUri = mData!![position]

//        Log.d("url", imageUri.toString())

        if (imageUri.toString().startsWith("https")) {
            Glide.with(mContext!!).load(imageUri).into(holder.image)
        } else {
            Glide.with(mContext!!).load(imageUri).into(holder.image)
        }
        holder.deleteBtn.setOnClickListener { v ->
            val pos = holder.adapterPosition
            mData!!.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, mData!!.size)
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position, mData)
            }

        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    override fun getItemCount(): Int {
        return mData!!.size
    }
}