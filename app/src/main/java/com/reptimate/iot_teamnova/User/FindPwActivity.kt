package com.reptimate.iot_teamnova.User

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.EmailModel
import com.reptimate.iot_teamnova.Retrofit.PostResult
import com.reptimate.iot_teamnova.databinding.ActivityFindPwBinding
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindPwActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFindPwBinding
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindPwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        binding.emailEt.addTextChangedListener(object : TextWatcher { // 이메일 주소 유효성 검사

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 이메일 editText를 문자열 값으로 받기
                val userEmail = binding.emailEt.text.toString()
                // 이메일 주소 값이 없을 때
                if(userEmail == "")
                    binding.emailValidate.visibility = View.GONE
                // 이메일 주소가 형식에 맞지 않을 때
                else if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
                    binding.emailValidate.visibility = View.VISIBLE
                // 올바른 이메일 주소 형식일 때
                else if(Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
                    binding.emailValidate.visibility = View.GONE

            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })

       binding.emailBtn.setOnClickListener {
           binding.emailBtn.isEnabled = false
           // 이메일 editText를 문자열 값으로 받기
           val userEmail = binding.emailEt.text.toString()
           // 이메일 주소 값이 없을 때
           if (userEmail == "") {
               Toast.makeText(
                   applicationContext,
                   "이메일을 입력해주세요.",
                   Toast.LENGTH_SHORT
               ).show()
               binding.emailBtn.isEnabled = true
           }

           // 이메일 주소가 형식에 맞지 않을 때
           else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
               Toast.makeText(
                   applicationContext,
                   "올바른 이메일 형식을 입력해주세요.",
                   Toast.LENGTH_SHORT
               ).show()
               binding.emailBtn.isEnabled = true
           }

           // 올바른 이메일 주소 형식일 때
           else if (Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
               val data = EmailModel(userEmail,"OLDUSER")
               api.post_email(data).enqueue(object : Callback<PostResult> {
                   @SuppressLint("SuspiciousIndentation")
                   override fun onResponse(
                       call: Call<PostResult>,
                       response: Response<PostResult>
                   ) {
                       Log.d("log", response.toString())

                       if (response.body().toString().isNotEmpty())
                           try {
                               Toast.makeText(applicationContext, "메일로 인증 코드가 발송 되었습니다.", Toast.LENGTH_SHORT).show()
                               binding.emailBtn.text = "재전송"
                               binding.confirmEmailEt.isEnabled = true
                               binding.emailBtn.isEnabled = true
                               binding.confirmEmailEt.setBackgroundResource(R.drawable.edit_text_background)
                               binding.confirmEmailEt.requestFocus()
                               val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                               inputMethodManager.showSoftInput(binding.confirmEmailEt, InputMethodManager.SHOW_IMPLICIT)

                               val jsonObject = response.body()?.result
                               val userToken =
                                   jsonObject?.get("signupVerifyToken").toString()
                                       .replace("\"", "")

                               binding.confirmEmailBtn.setOnClickListener {
                                   val confirmEmail =
                                       binding.confirmEmailEt.text.toString()
                                   Log.d("status : ", userToken)
                                   Log.d("status : ", confirmEmail)

                                   if (confirmEmail == userToken) {
                                       Toast.makeText(
                                           applicationContext,
                                           "인증이 완료 되었습니다.",
                                           Toast.LENGTH_SHORT
                                       ).show()
                                       finish()
                                       val i = Intent(applicationContext, ChangePwActivity::class.java)
                                       i.putExtra("email", userEmail)
                                       startActivity(i)
                                       overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                   } else {
                                       Toast.makeText(
                                           applicationContext,
                                           "인증번호가 틀렸습니다.\n다시 확인해주세요.",
                                           Toast.LENGTH_SHORT
                                       ).show()
                                       binding.emailBtn.isEnabled = true
                                   }
                               }

                           } catch (e: JSONException) {
                               e.printStackTrace()
                               binding.emailBtn.isEnabled = true
                           }
                   }

                   override fun onFailure(call: Call<PostResult>, t: Throwable) {
                       // 실패
                       Log.d("log", t.message.toString())
                       Log.d("log", "fail")
                       binding.emailBtn.isEnabled = true
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