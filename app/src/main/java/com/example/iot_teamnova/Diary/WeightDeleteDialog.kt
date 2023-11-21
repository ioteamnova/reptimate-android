package com.example.iot_teamnova.Diary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import com.example.iot_teamnova.R
import com.example.iot_teamnova.Retrofit.GetUserResult
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeightDeleteDialog : Activity() {

    var noBtn: Button? = null
    var yesBtn: Button? = null
    var getIdx: String? = null
    val api = APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_weight_delete)
        yesBtn = findViewById<Button>(R.id.yesBtn)
        noBtn = findViewById<Button>(R.id.noBtn)

        val intent: Intent = getIntent()
        getIdx = intent.getStringExtra("idx")

        yesBtn?.setOnClickListener{
            api.WeightDelete(getIdx).enqueue(object :
                Callback<GetUserResult> {
                override fun onResponse(call: Call<GetUserResult>, response: Response<GetUserResult>) {
                    Log.d("log",response.toString())
                    Log.d("body_log", response.body().toString())
                    try {
                        val message = response.body()?.message

                        if(message == "Success"){
                            Toast.makeText(applicationContext, "체중 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
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
