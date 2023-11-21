package com.reptimate.iot_teamnova.Diary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.databinding.FragDiaryViewBinding
import com.google.android.material.tabs.TabLayout

class PetViewActivity : AppCompatActivity() {

    private val binding by lazy { FragDiaryViewBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        val intent: Intent = getIntent()
        val getIdx = intent.getStringExtra("idx")
        val getName = intent.getStringExtra("name")
        val getType = intent.getStringExtra("type")
        val getGender = intent.getStringExtra("gender")
        val getBirthDate = intent.getStringExtra("birthDate")
        val getAdoptionDate = intent.getStringExtra("adoptionDate")
        val getProfile = intent.getStringExtra("profile")

        val bundle = Bundle()
        bundle.putString("idx", getIdx)
        val fragment = DiaryViewTab1()
        fragment.arguments = bundle
        val fragment2 = DiaryViewTab2()
        fragment2.arguments = bundle

        binding.name.text = getName
        binding.type.text = getType
        if(getGender == "MALE") {
            binding.gender.text = "수컷"
            binding.gender.setBackgroundResource(R.drawable.male_background)
        }
        if(getGender == "FEMALE") {
            binding.gender.text = "암컷"
            binding.gender.setBackgroundResource(R.drawable.female_background)
        }
        if(getGender == "NONE") {
            binding.gender.text = "미구분"
            binding.gender.setBackgroundResource(R.drawable.neutral_background)
        }

        //split 분리된 문자를 담을 배열 선언 실시
        var birthStr = getBirthDate?.split("T")
        var adoptStr = getAdoptionDate?.split("T")

        //for 반복문 수행 실시 (i변수는 0번 인덱스부터 str_data 문자열 길이까지 반복을 수행)
        if (birthStr != null) {
            binding.birthDate.text = "출생일 : " + birthStr.get(0)
        }
        if(adoptStr != null) {
            binding.adoptDate.text = "입양일 : " + adoptStr.get(0)
        }

//        binding.birthDate.text = "출생일 : " + getBirthDate
//        binding.adoptDate.text = "입양일 : " + getAdoptionDate

        if(getProfile != "" && getProfile != "null"){ // 프로필이 존재할 때
            Glide.with(applicationContext)
                .load(getProfile)
                .centerCrop()
                .override(130, 130)
                .into(binding.profile)
            binding.profile.clipToOutline = true
        } else {
            Glide.with(applicationContext)
                .load(R.drawable.reptimate_logo)
                .centerCrop()
                .override(130, 130)
                .into(binding.profile)
            binding.profile.clipToOutline = true
        }

        supportFragmentManager.beginTransaction().add(R.id.frameLayout, fragment).commit()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {
                        //Tab1
                        replaceView(fragment)
                    }
                    1 -> {
                        //Tab2
                        replaceView(fragment2)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

    private fun replaceView(tab: Fragment) {
        //화면 변경
        var selectedFragment: Fragment? = null
        selectedFragment = tab
        selectedFragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, it).commit()
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}