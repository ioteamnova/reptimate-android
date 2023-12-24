package com.reptimate.iot_teamnova.customAlbum

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.reptimate.iot_teamnova.databinding.ActivityCustomAlbumBinding

class CustomAlbumActivity2 : AppCompatActivity() , CustomAlbumAdapter.OnItemClickListener{

    lateinit var getImagePath: String

    var listOfPhotos = ArrayList<ItemGallery>()

    var selectedItems = ArrayList<String>()

    private lateinit var adapter: CustomAlbumAdapter

    private val binding by lazy { ActivityCustomAlbumBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        adapter = CustomAlbumAdapter(listOfPhotos, this)
        binding.photoRV.adapter = adapter

        initData()


        binding.closeTV.setOnClickListener { // 취소
            finish()
        }
        binding.confirmTV.setOnClickListener { // 확인
            val resultIntent = Intent()
            resultIntent.putExtra("data", selectedItems)
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Finish Activity B
        }

    }

    @SuppressLint("Range")
    private fun initData() {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DURATION
        )
        var selection: String?
        val queryUri = MediaStore.Files.getContentUri("external")

        selection =
            (MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        var cursor: Cursor? = this@CustomAlbumActivity2.contentResolver.query(
            queryUri,
            projection,
            selection,
            null,
            MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        )

        var listOfPhotos = ArrayList<ItemGallery>() // Create a new list for data

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val data = ItemGallery()
                data.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                data.mediaType = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE))
                data.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.DURATION))

                if (data.mediaType == 1) {
                    data.mediaData = "content://media/external/images/media/" + data.id
                } else if (data.mediaType == 3) {
                    data.mediaData = "content://media/external/video/media/${data.id}"
                }
                listOfPhotos.add(data)
            } while (cursor.moveToNext())
        }

        cursor?.close()

        // Update the RecyclerView with the new data
        adapter.setData(listOfPhotos)
    }

    override fun onItemClick(isChecked: Boolean, item: ItemGallery) {
        if(isChecked) {
            selectedItems.add(item.mediaData)
        } else {
            selectedItems.remove(item.mediaData)
        }
    }
}