package com.example.iot_teamnova.User

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iot_teamnova.R
import com.example.iot_teamnova.Retrofit.ErrorResponse
import com.example.iot_teamnova.Retrofit.FindPwModel
import com.example.iot_teamnova.Retrofit.JoinModel
import com.example.iot_teamnova.Retrofit.PostLoginResult
import com.example.iot_teamnova.databinding.ActivityChangePwBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class ChangePwActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChangePwBinding
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        val intent: Intent = getIntent()
        var email = intent.getStringExtra("email")

        binding.pw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    binding.layoutPw.hint = "비밀번호"
                } else {
                    binding.layoutPw.hint = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed in this case
            }
        })

        binding.rePw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    binding.layoutRePw.hint = "비밀번호 확인 "
                } else {
                    binding.layoutRePw.hint = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed in this case
            }
        })

        binding.pw.addTextChangedListener(object : TextWatcher { // 비밀번호 유효성 검사
            val pwPattern =
                "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&]).{8,20}.\$" // 8~20자, 영문, 숫자, 특수문자

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 비밀번호 editText를 문자열 값으로 받기
                val userPassword = binding.pw.text.toString()
                // 비밀번호 값이 없을 때
                if(userPassword == "")
                    binding.pwValidate.visibility = View.GONE
                // 비밀번호가 형식에 맞지 않을 때
                else if(!Pattern.matches(pwPattern, userPassword))
                    binding.pwValidate.visibility = View.VISIBLE
                // 올바른 비밀번호 형식일 때
                else if(Pattern.matches(pwPattern, userPassword))
                    binding.pwValidate.visibility = View.GONE

            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.rePw.addTextChangedListener(object : TextWatcher { // 비밀번호 확인

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 비밀번호, 비밀번호 확인 editText를 문자열 값으로 받기
                val userPassword = binding.pw.text.toString()
                val userRePassword = binding.rePw.text.toString()
                // 비밀번호 값이 없을 때
                if(userRePassword == "")
                    binding.rePwValidate.visibility = View.GONE
                // 비밀번호가 맞지 않을 때
                else if(userPassword != userRePassword)
                    binding.rePwValidate.visibility = View.VISIBLE
                // 비밀번호가 맞을 때
                else if(userPassword == userRePassword)
                    binding.rePwValidate.visibility = View.GONE

            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.findPwBtn.setOnClickListener {
            binding.findPwBtn.isEnabled = false

            val pw = binding.pw.text.toString()
            val rePw = binding.rePw.text.toString()

            val pwPattern =
                "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&]).{8,20}.\$" // 8~20자, 영문, 숫자, 특수문자

            if(pw == "") {
                Toast.makeText(applicationContext, "비밀번호를 입력 해주세요.", Toast.LENGTH_SHORT).show()
                binding.findPwBtn.isEnabled = true
            }
            else if(!Pattern.matches(pwPattern, pw)) {
                Toast.makeText(applicationContext, "유효한 형식의 비밀번호를 입력 해주세요.", Toast.LENGTH_SHORT).show()
                binding.findPwBtn.isEnabled = true
            }
            else if(rePw == "") {
                Toast.makeText(applicationContext, "비밀번호 확인을 입력 해주세요.", Toast.LENGTH_SHORT).show()
                binding.findPwBtn.isEnabled = true
            }
            else if(pw != rePw) {
                Toast.makeText(applicationContext, "비밀번호를 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
                binding.findPwBtn.isEnabled = true
            }
            else {
                val data = FindPwModel(email,pw)
                api.find_pw(data).enqueue(object : Callback<PostLoginResult> {
                    override fun onResponse(call: Call<PostLoginResult>, response: Response<PostLoginResult>) {
//                        Log.d("log",response.toString())
//                        Log.d("body_log", response.body().toString())
//                        Log.d("isSuccess",response.isSuccessful().toString())
//                        Log.d("code_log",response.code().toString())
                        if (response.isSuccessful) {
                            // Request was successful
                            // Handle the response body here
                            val responseBody = response.body()
                            if (responseBody != null) {
                                // Process the response body
                                Toast.makeText(
                                    applicationContext,
                                    "비밀번호 변경이 완료 되었습니다.\n다시 로그인 해주세요!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                // Handle the case where response.body() is null
                                Toast.makeText(
                                    applicationContext,
                                    "서버와의 오류가 발생하였습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.findPwBtn.isEnabled = true
                            }
                        }
                    }

                    override fun onFailure(call: Call<PostLoginResult>, t: Throwable) {
                        // 실패
                        Log.d("log",t.message.toString())
                        Log.d("log","fail")
                        binding.findPwBtn.isEnabled = true
                    }
                })
            }
        }

    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}