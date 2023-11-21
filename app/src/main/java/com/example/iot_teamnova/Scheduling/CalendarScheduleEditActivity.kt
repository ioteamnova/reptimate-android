package com.example.iot_teamnova.Scheduling

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iot_teamnova.R
import com.example.iot_teamnova.Retrofit.CalendarScheduleWriteModel
import com.example.iot_teamnova.Retrofit.PostResult
import com.example.iot_teamnova.databinding.ActivityScheduleCalendarWriteBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CalendarScheduleEditActivity : AppCompatActivity(){
    private val binding by lazy { ActivityScheduleCalendarWriteBinding.inflate(layoutInflater) }
    private val api = APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        binding.title.text = "일정 수정"
        binding.confirmBtn.text = "수정하기"

        val intent: Intent = getIntent()
        val getIdx = intent.getStringExtra("idx")
        val getTitle = intent.getStringExtra("title").toString()
        val getAlarmTime = intent.getStringExtra("alarmTime").toString()
        val getDate = intent.getStringExtra("date").toString()
        val getMemo = intent.getStringExtra("memo").toString()

        binding.date.text = getDate
        binding.titleEt.setText(getTitle)
        binding.memoEt.setText(getMemo)

        setTimeFromString(binding.timePicker, getAlarmTime)

        binding.timePicker.setIs24HourView(true)

        binding.confirmBtn.setOnClickListener {
            binding.confirmBtn.isEnabled = false

            val hour = String.format("%02d", binding.timePicker.hour)
            val minute  = String.format("%02d", binding.timePicker.minute)

            val date = binding.date.text.toString()
            val title = binding.titleEt.text.toString()
            var time = "$hour:$minute"
            var memo = binding.memoEt.text.toString()

            if(title == "") {
                Toast.makeText(applicationContext, "스케줄 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                binding.confirmBtn.isEnabled = true
            }
            else {
                val data =
                    CalendarScheduleWriteModel(date, title, time, memo)
                api.post_calendar_schedule_edit(getIdx, data).enqueue(object : Callback<PostResult> {
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
                                    "스케줄이 수정 되었습니다.",
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

    private fun setTimeFromString(timePicker: TimePicker, timeString: String) {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        calendar.time = sdf.parse(timeString)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        timePicker.setIs24HourView(true) // Set whether the TimePicker should use the 24-hour format
        timePicker.hour = hour // Set the hour
        timePicker.minute = minute // Set the minute
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}