package com.example.iot_teamnova.Scheduling

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.iot_teamnova.R
import com.example.iot_teamnova.Retrofit.PostResult
import com.example.iot_teamnova.Retrofit.ScheduleWriteModel
import com.example.iot_teamnova.databinding.ActivityScheduleScheduleWriteBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleWriteActivity : AppCompatActivity() {
    var sunday = "0"
    var monday = "0"
    var tuesday = "0"
    var wednesday = "0"
    var thursday = "0"
    var friday = "0"
    var saturday = "0"

    var repeat = "$sunday,$monday,$tuesday,$wednesday,$thursday,$friday,$saturday"

    private val binding by lazy { ActivityScheduleScheduleWriteBinding.inflate(layoutInflater) }
    private val api = APIS.create()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val getRepeat = data?.getStringExtra("repeat")
            // Handle the returned data from Activity B
            if (getRepeat != null) {
                // Use the resultData as needed in Activity A
                repeat = getRepeat
                val formattedDays = getFormattedDaysOfWeek(repeat)
                binding.repeatWeek.text = formattedDays
            }
        }
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //뒤로가기(back) 버튼 클릭 시
        binding.backBtn.setOnClickListener{
            finish()
        }

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        binding.timePicker.setIs24HourView(true)

        binding.repeat.setOnClickListener {
            val intent = Intent(this, RepeatActivity::class.java)
            intent.putExtra("repeat", repeat)
            resultLauncher.launch(intent) // Start Activity B
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.confirmBtn.setOnClickListener {
            binding.confirmBtn.isEnabled = false

            val hour = String.format("%02d", binding.timePicker.hour)
            val minute  = String.format("%02d", binding.timePicker.minute)

            val title = binding.titleEt.text.toString()
            var time = "$hour:$minute"
            var memo = binding.memoEt.text.toString()

            if(title == "") {
                Toast.makeText(applicationContext, "스케줄 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                binding.confirmBtn.isEnabled = true
            }
            else {
                val data =
                    ScheduleWriteModel(title, time, repeat, memo)
                api.post_schedule_write(data).enqueue(object : Callback<PostResult> {
                    override fun onResponse(
                        call: Call<PostResult>,
                        response: Response<PostResult>
                    ) {
                        Log.d("log", response.toString())
                        Log.d("body_log", response.body().toString())
                        Log.d("request body : ", data.toString())
                        if (!response.body().toString().isEmpty()) {
                            if (response.body().toString() != "null") {
                                Toast.makeText(
                                    applicationContext,
                                    "스케줄이 등록 되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "서버와의 오류가 발생하였습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.confirmBtn.isEnabled = true
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<PostResult>,
                        t: Throwable
                    ) {
                        // 실패
                        Log.d("log", t.message.toString())
                        Log.d("log", "fail")
                        binding.confirmBtn.isEnabled = true
                    }
                })
            }
        }

    }

    fun getFormattedDaysOfWeek(daysOfWeek: String): String {
        val days = listOf("일", "월", "화", "수", "목", "금", "토")
        val selectedDays = mutableListOf<String>()

        // Split the input string into an array of integers
        val dayArray = daysOfWeek.split(",").map { it.toInt() }

        // Loop through the dayArray and identify selected days
        for (i in dayArray.indices) {
            if (dayArray[i] == 1) {
                selectedDays.add(days[i])
            }
        }

        return when {
            selectedDays.isEmpty() -> "안 함"
            selectedDays.size == 7 -> "매일"
            selectedDays.size == 5 && selectedDays.containsAll(listOf("월", "화", "수", "목", "금")) -> "주중"
            selectedDays.size == 2 && selectedDays.containsAll(listOf("토", "일")) -> "주말"
            selectedDays.size == 1 && selectedDays.contains("일") -> "일요일마다"
            selectedDays.size == 1 && selectedDays.contains("월") -> "월요일마다"
            selectedDays.size == 1 && selectedDays.contains("화") -> "화요일마다"
            selectedDays.size == 1 && selectedDays.contains("수") -> "수요일마다"
            selectedDays.size == 1 && selectedDays.contains("목") -> "목요일마다"
            selectedDays.size == 1 && selectedDays.contains("금") -> "금요일마다"
            selectedDays.size == 1 && selectedDays.contains("토") -> "토요일마다"
            else -> selectedDays.joinToString(separator = ", ")
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}