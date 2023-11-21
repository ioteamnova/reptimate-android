package com.reptimate.iot_teamnova.Diary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.reptimate.iot_teamnova.databinding.FragDiaryDiaryViewImageDetailBinding

class DiaryDiaryViewImageDetailActivity : AppCompatActivity() {
    private lateinit var photoAdapter: DiaryImageDetailAdapter
    var imageList = ArrayList<String>()
    private val binding by lazy { FragDiaryDiaryViewImageDetailBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    var imgPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent: Intent = intent
        imgPosition = intent.getIntExtra("imgPosition", 0)
        imageList = intent.getSerializableExtra("imageList") as ArrayList<String>

        //뒤로가기(back) 버튼 클릭 시
        binding.backBtn.setOnClickListener {
            finish()
        }

        photoAdapter = DiaryImageDetailAdapter(applicationContext, imageList)
        binding.viewPager.adapter = photoAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.imagesCount.text = (imgPosition + 1).toString() + "/" + imageList.size
        binding.viewPager.setCurrentItem(imgPosition, false)

        // Set up the ViewPager callback to update the circles
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                imgPosition = position
                binding.imagesCount.text = (imgPosition + 1).toString() + "/" + imageList.size
            }
        })

    }

}