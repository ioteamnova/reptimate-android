package com.example.iot_teamnova.Diary

import APIS
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.iot_teamnova.PreferenceUtil
import com.example.iot_teamnova.R
import com.example.iot_teamnova.Retrofit.GetResult
import com.example.iot_teamnova.Retrofit.GetUserResult
import com.example.iot_teamnova.User.MainActivity
import com.example.iot_teamnova.databinding.FragDiaryDiaryViewBinding
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiaryDiaryViewAcitivity : AppCompatActivity() {
    private lateinit var photoAdapter: DiaryViewPagerAdapter
    var imageList = ArrayList<String>()
    private val binding by lazy { FragDiaryDiaryViewBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    var imgPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent: Intent = getIntent()
        val getPetIdx = intent.getStringExtra("petIdx")
        val getDiaryIdx = intent.getStringExtra("idx")

        //뒤로가기(back) 버튼 클릭 시
        binding.backBtn.setOnClickListener{
            finish()
        }

        binding.menuBtn.setOnClickListener{
            val popupMenu = PopupMenu(applicationContext, it)
            popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.action_menu1) {
                    val i = Intent(applicationContext, DiaryEditActivity::class.java)
                    i.putExtra("idx", getDiaryIdx)
                    i.putExtra("petIdx", getPetIdx)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else if (menuItem.itemId == R.id.action_menu2) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("다이어리 삭제")
                        .setMessage("해당 다이어리를 삭제하시겠습니까?")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, id ->
                                api.DiaryDelete(getDiaryIdx).enqueue(object :
                                    Callback<GetUserResult> {
                                    override fun onResponse(call: Call<GetUserResult>, response: Response<GetUserResult>) {
                                        Log.d("log",response.toString())
                                        Log.d("body_log", response.body().toString())
                                        try {
                                            val message = response.body()?.message

                                            if(message == "Success"){
                                                Toast.makeText(applicationContext, "다이어리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                                finish()
                                            } else {
                                                Toast.makeText(applicationContext, "서버와의 오류가 발생하였습니다", Toast.LENGTH_SHORT).show()
                                            }


                                        } catch(e: JSONException){
                                            e.printStackTrace()
                                        }
                                    }

                                    override fun onFailure(call: Call<GetUserResult>, t: Throwable) {
                                        // 실패
                                        Log.d("log",t.message.toString())
                                        Log.d("log","fail")
                                    }
                                })
                            })
                        .setNegativeButton("취소",
                            DialogInterface.OnClickListener{ dialogInterface, i ->
                                return@OnClickListener
                            })
                    // 다이얼로그를 띄워주기
                    builder.show()
                }
                false
            }
            popupMenu.show()
        }
    }

    override fun onResume() {
        super.onResume()

        val intent: Intent = getIntent()
        val getPetIdx = intent.getStringExtra("petIdx")
        val getDiaryIdx = intent.getStringExtra("idx")

        loadContent(getPetIdx, getDiaryIdx)
    }

    fun loadContent(getPetIdx : String?, getDiaryIdx : String?) {
        imageList = ArrayList<String>()

        api.get_diary_view(getPetIdx, getDiaryIdx).enqueue(object : Callback<GetResult> {
            override fun onResponse(call: Call<GetResult>, response: Response<GetResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                if (response.isSuccessful) {
                    // Request was successful
                    // Handle the response body here
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            var jsonObject = response.body()?.result
                            var getIdx = jsonObject?.get("idx").toString().replace("\"", "") // idx
                            var getTitle = jsonObject?.get("title").toString().replace("\"", "") // 타이틀
                            var getContent =jsonObject?.get("content").toString().replace("\"", "") // 내용
                            var getCreatedAt = jsonObject?.get("createdAt").toString().replace("\"","").split("T")
                            var imagePaths = jsonObject?.get("images").toString().replace("^\"|\"$".toRegex(), "") // 펫 목록 배열

                            binding.title.setText(getTitle)
                            binding.date.setText(getCreatedAt[0])
                            binding.content.setText(getContent)

                            Log.d("imagePaths : ", imagePaths.toString())

                            val array = JSONArray(imagePaths)

                            //traversing through all the object
                            for (i in 0 until array.length()) {
                                val item = array.getJSONObject(i)
                                val idx = item.getString("idx")
                                val createdAt = item.getString("createdAt")
                                val updatedAt = item.getString("updatedAt")
                                val deletedAt = item.getString("deletedAt")
                                val imagePath = item.getString("imagePath")

                                imageList.add(imagePath)
                            }

                            photoAdapter = DiaryViewPagerAdapter(applicationContext, imageList)
                            binding.viewPager.adapter = photoAdapter
                            binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                            // Set up the ViewPager callback to update the circles
                            binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                override fun onPageSelected(position: Int) {
                                    imgPosition = position
                                    updateCircles(position)
                                }
                            })

                            // Initial circle setup
                            updateCircles(binding.viewPager.currentItem)

                            binding.viewPager.post {
                                val width = binding.viewPager.width

                                // Set the height of the ImageView to match its width
                                val layoutParams = binding.viewPager.layoutParams
                                layoutParams.height = width
                                binding.viewPager.layoutParams = layoutParams
                            }

                            Log.d("body_log", getIdx)
                        } catch(e: JSONException){
                                    e.printStackTrace()
                        }
                    }
                    else {
                        // Handle the case where response.body() is null
                        Log.d("토큰 결과 : ", "서버와의 오류가 발생하였습니다.")
                    }
                }
                else if (response.code() == 401) {
                    Log.d("토큰 결과 : ", "401에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            loadContent(getPetIdx, getDiaryIdx)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
                else if (response.code() == 404) {
                    Log.d("토큰 결과 : ", "404에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            loadContent(getPetIdx, getDiaryIdx)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetResult>, t: Throwable) {
                // 실패
                Log.d("log", t.message.toString())
                Log.d("log", "fail")
            }
        })
    }

    private fun updateCircles(currentPosition: Int) {
        val photoCount = photoAdapter.itemCount

        // Clear the existing circles
        binding.circleContainer.removeAllViews()

        // Add circles based on the photo count
        for (i in 0 until photoCount) {
            val circleImageView = ImageView(this)
            val circleSize = resources.getDimensionPixelSize(R.dimen.circle_size)

            val layoutParams = LinearLayout.LayoutParams(circleSize, circleSize)
            layoutParams.marginStart = resources.getDimensionPixelSize(R.dimen.circle_margin)

            // Set the circle drawable
            if (i == currentPosition) {
                circleImageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.circle_filled)
                )
            } else {
                circleImageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.circle_empty)
                )
            }

            circleImageView.layoutParams = layoutParams
            binding.circleContainer.addView(circleImageView)
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}