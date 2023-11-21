package com.reptimate.iot_teamnova.Scheduling

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.databinding.ActivityScheduleBinding
import com.google.android.material.tabs.TabLayout

class ScheduleActivity : AppCompatActivity() {

    lateinit var tab1: ScheduleViewTab1        //프레그먼트 1
    lateinit var tab2: ScheduleViewTab2        //프레그먼트 2

    private val binding by lazy { ActivityScheduleBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //뒤로가기(back) 버튼 클릭 시
        binding.backBtn.setOnClickListener{
            finish()
        }

        tab1 = ScheduleViewTab1()        //프레그먼트 1 객체화
        tab2 = ScheduleViewTab2()        //프레그먼트 2 객체화

        supportFragmentManager.beginTransaction().add(R.id.frameLayout, tab1).commit()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        //Tab1
                        replaceView(tab1)
                    }
                    1 -> {
                        //Tab2
                        replaceView(tab2)
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