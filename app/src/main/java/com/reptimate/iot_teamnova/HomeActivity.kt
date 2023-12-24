package com.reptimate.iot_teamnova

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.reptimate.iot_teamnova.Diary.DiaryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

//        val intent = Intent(this, MqttService::class.java)
//        startService(intent)

        // 하단 탭이 눌렸을 때 화면을 전환하기 위해선 이벤트 처리하기 위해 BottomNavigationView 객체 생성
        var bnv_main = findViewById(R.id.bnv_main) as BottomNavigationView

        // OnNavigationItemSelectedListener를 통해 탭 아이템 선택 시 이벤트를 처리
        // navi_menu.xml 에서 설정했던 각 아이템들의 id를 통해 알맞은 프래그먼트로 변경하게 한다.
        bnv_main.run { setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.board -> {
                    // 다른 프래그먼트 화면으로 이동하는 기능
                    val boardFragment = BoardFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fl_container, boardFragment).commit()
                }
                R.id.auction -> {
                    // 다른 프래그먼트 화면으로 이동하는 기능
                    val auctionFragment = AuctionFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fl_container, auctionFragment).commit()
                }
                R.id.ai -> {
                    // 다른 프래그먼트 화면으로 이동하는 기능
                    val aiFragment = AiFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fl_container, aiFragment).commit()
                }
                R.id.diary -> {
                    val diaryFragment = DiaryFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fl_container, diaryFragment).commit()
                }
                R.id.mypage -> {
                    val accountFragment = AccountFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fl_container, accountFragment).commit()
                }
            }
            true
        }
            selectedItemId = R.id.mypage
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fl_container)

        if (currentFragment is BoardFragment) {
            if (currentFragment.goBack()) {
                // 처리 성공
            } else {
                super.onBackPressed()
            }
        } else if (currentFragment is AuctionFragment) {
            if (currentFragment.goBack()) {
                // 처리 성공
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}