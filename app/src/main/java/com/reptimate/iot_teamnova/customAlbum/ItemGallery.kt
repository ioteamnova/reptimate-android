package com.reptimate.iot_teamnova.customAlbum

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

class ItemGallery {

    var mediaData: String = ""
    var uri: Uri? = null
    var id: Long = -1
    var isSelected = false
    var mediaType: Int// 사진: 1, 동영상: 3
    var duration: Int// 동영상 재생시간


    companion object {

        @JvmStatic
        @BindingAdapter("imageUrl")
        fun loadImage(view: ImageView, url: String) {
            Glide.with(view.context).load(url).into(view)
        }

        @JvmStatic
        @BindingAdapter("duration")
        fun setDuration(view: TextView, duration: Long) {
            if (duration == 0L) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
                view.text = Utils.formatDuration(duration)
            }
        }

        @JvmStatic
        @BindingAdapter("radioButton")
        fun setRadioButton(view: RadioButton, isSelected: Boolean) {
            view.isChecked = isSelected
        }
    }

    constructor() {
        mediaData = ""
        uri = null
        id = -1
        isSelected = false
        mediaType = -1
        duration = 0
    }

}
