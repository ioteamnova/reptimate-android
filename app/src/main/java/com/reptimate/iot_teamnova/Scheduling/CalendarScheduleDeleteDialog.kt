package com.reptimate.iot_teamnova.Scheduling

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.GetUserResult
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CalendarScheduleDeleteDialog : Activity() {

    var noBtn: Button? = null
    var yesBtn: Button? = null
    var getIdx: String? = null
    val api = APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_schedule_delete)
        yesBtn = findViewById<Button>(R.id.yesBtn)
        noBtn = findViewById<Button>(R.id.noBtn)
        val intent: Intent = getIntent()
        getIdx = intent.getStringExtra("idx")

        yesBtn?.setOnClickListener{
            api.CalendarScheduleDelete(getIdx).enqueue(object :
                Callback<GetUserResult> {
                override fun onResponse(call: Call<GetUserResult>, response: Response<GetUserResult>) {
                    Log.d("log",response.toString())
                    Log.d("body_log", response.body().toString())
                    try {
                        val message = response.body()?.message

                        if(message == "Success"){
                            Toast.makeText(applicationContext, "스케줄이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(applicationContext, "서버와의 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
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
        }

        noBtn?.setOnClickListener { finish() }
    }

}
