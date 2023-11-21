package com.reptimate.iot_teamnova.Diary

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.PostResult
import com.reptimate.iot_teamnova.Retrofit.WeightWriteModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class WeightEditActivity : Activity() {
    var dateString = ""

    val api = APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_weight_write)
        var dateBtn = findViewById<TextView>(R.id.date)
        var weightEt = findViewById<EditText>(R.id.weight)
        var confirmBtn = findViewById<TextView>(R.id.confirm_btn)

        val intent: Intent = intent
        val getIdx = intent.getStringExtra("idx")
        val getWeight = intent.getStringExtra("weight")
        val getDate = intent.getStringExtra("date")
        val getGap = intent.getStringExtra("gap")
        val getPetIdx = intent.getStringExtra("petIdx")

        dateString = getDate.toString()

        dateBtn.text = dateString
        weightEt.setText(getWeight)

        dateBtn.setOnClickListener {
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                var monthStr = (month+1).toString()
                var dayStr = dayOfMonth.toString()
                if((month+1) < 10) {
                    monthStr = "0$monthStr"
                }
                if(dayOfMonth < 10) {
                    dayStr = "0$dayStr"
                }
                dateString = "${year}-$monthStr-$dayStr"
                dateBtn.text = dateString
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(
                Calendar.DAY_OF_MONTH)).show()
        }

        confirmBtn.setOnClickListener {
            confirmBtn.isEnabled = false

            var date = dateBtn.text.toString()
            var weight = weightEt.text.toString()

            if(dateString == "") {
                Toast.makeText(applicationContext, "날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                confirmBtn.isEnabled = true
            }
            if(weight == "") {
                Toast.makeText(applicationContext, "체중을 입력해주세요,", Toast.LENGTH_SHORT).show()
                confirmBtn.isEnabled = true
            }
            if(weight == "0") {
                Toast.makeText(applicationContext, "체중은 0 이상을 입력해야합니다.", Toast.LENGTH_SHORT).show()
                confirmBtn.isEnabled = true
            }
            else {
                val data =
                    WeightWriteModel(date, weight.toFloat())
                api.post_weight_edit(getIdx, data).enqueue(object : Callback<PostResult> {
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
                                    "체중이 등록 되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "이미 등록된 날짜입니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                confirmBtn.isEnabled = true
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
                        confirmBtn.isEnabled = true
                    }
                })
            }
        }

    }
}