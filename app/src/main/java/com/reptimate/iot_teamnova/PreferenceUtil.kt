package com.reptimate.iot_teamnova

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.reptimate.iot_teamnova.Retrofit.*
import com.reptimate.iot_teamnova.User.MainActivity
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PreferenceUtil(context : Context) {

    private val prefs : SharedPreferences = context.getSharedPreferences("user_token", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = prefs.edit();
    private val api = APIS.create()

    var token: String?
    get() = prefs.getString("accessToken", null)
    set(value) {
        prefs.edit().putString("accessToken", value).apply()
    }

    var getidx: String?
    get() = prefs.getString("idx", null)
    set(value) {
        prefs.edit().putString("idx", value).apply()
    }

    var refreshToken: String?
        get() = prefs.getString("refreshToken", null)
        set(value) {
            prefs.edit().putString("refreshToken", value).apply()
        }

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String){
        prefs.edit().putString(key, str).apply()
    }

    fun isLogin() : String? {
        return prefs.getString("accessToken", null);
    }

    fun checkLogin() {
        if (isLogin() != null) {
//            (MainApplication.context() as MainActivity).finish()
            val i = Intent(MainApplication.context(), HomeActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            MainApplication.context().startActivity(i)
        } else {

        }
    }

    fun checkToken(callback: (Boolean) -> Unit) {
        api.post_token(TokenModel(MainApplication.prefs.refreshToken)).enqueue(object : Callback<PostResult> {
            override fun onResponse(call: Call<PostResult>, response: Response<PostResult>)
            {
                Log.d("log", response.toString())
                Log.d("body_log", response.body().toString())
                if (response.isSuccessful) {
                    // Request was successful
                    // Handle the response body here
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val jsonObject = response.body()?.result
                            val accessToken = jsonObject?.get("accessToken").toString().replace("\"","")

                            MainApplication.prefs.setString("accessToken", accessToken)

                            editor.commit()

                            Log.d("토큰 결과 : ", "토큰이 재발급 되었습니다.")

                            callback(true)

                        } catch(e: JSONException){
                            e.printStackTrace()
                        }
                    } else {
                        // Handle the case where response.body() is null
                        Log.d("토큰 결과 : ", "서버와의 오류가 발생하였습니다.")
                    }
                } else if (response.code() == 401) {
                    val i = Intent(MainApplication.context(), MainActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    MainApplication.context().startActivity(i)
                    editor.clear()
                    editor.commit()
                    Toast.makeText(
                        MainApplication.context(),
                        "리프레시 토큰이 만료되었습니다.\n다시 로그인이 필요합니다.", Toast.LENGTH_SHORT
                    ).show()
                    callback(false)

                } else if (response.code() == 404) {
                    val i = Intent(MainApplication.context(), MainActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    MainApplication.context().startActivity(i)
                    editor.clear()
                    editor.commit()
                    Toast.makeText(
                        MainApplication.context(),
                        "회원정보가 일치하지 않습니다.\n다시 로그인 해주세요.", Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }
            override fun onFailure(call: Call<PostResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    open fun logout() {
        editor.clear()
        editor.commit()
        val i = Intent(MainApplication.context(), MainActivity::class.java)
        MainApplication.context().startActivity(i)
        (MainApplication.context() as HomeActivity).finish()
        Toast.makeText(
            (MainApplication.context() as HomeActivity).applicationContext,
            "로그아웃되었습니다.", Toast.LENGTH_SHORT
        ).show()
    }
}