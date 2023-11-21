package com.reptimate.iot_teamnova.User

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.*
import com.reptimate.iot_teamnova.databinding.ActivityJoinBinding
import com.google.gson.Gson
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.Charset
import java.util.regex.Pattern

class JoinActivity : AppCompatActivity() {

    var emailConfirm = "false"
    var isPremium = false
    var agreeWithEmail = false

    var check1 = false
    var check2 = false

    val binding by lazy { ActivityJoinBinding.inflate(layoutInflater) }
    val api = APIS.create()
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gson = Gson()

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        binding.checkbox1.setOnCheckedChangeListener { buttonView, isChecked ->
            updateButtonState()
            updateCheckBox1State()
            if (isChecked) {
                binding.checkbox1.isChecked = true
                binding.checkbox2.isChecked = true
                binding.checkbox3.isChecked = true
                binding.checkbox4.isChecked = true
            } else {

            }
        }

        binding.checkbox2.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
            updateCheckBox1State()
        }

        binding.checkbox3.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
            updateCheckBox1State()
        }

        binding.checkbox4.setOnCheckedChangeListener { buttonView, isChecked ->
            updateButtonState()
            updateCheckBox1State()
            if (isChecked) {
                agreeWithEmail = true
            } else {
                agreeWithEmail = false
            }
        }

        binding.rule1.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder
                .setTitle("서비스 이용약관")
                .setMessage(R.string.rule1)
                .setPositiveButton("확인") { dialog, _ ->
                    // OK button clicked
                    dialog.dismiss()
                }
            val dialog = builder.create()
            dialog.show()
        }

        binding.rule2.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder
                .setTitle("개인정보 보호정책")
                .setMessage(R.string.rule2)
                .setPositiveButton("확인") { dialog, _ ->
                    // OK button clicked
                    dialog.dismiss()
                }
            val dialog = builder.create()
            dialog.show()
        }

        binding.joinEmail.addTextChangedListener(object : TextWatcher{ // 이메일 주소 유효성 검사

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 이메일 editText를 문자열 값으로 받기
                val userEmail = binding.joinEmail.text.toString()
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

        binding.joinEmailButton.setOnClickListener{
            // 이메일 editText를 문자열 값으로 받기
            val userEmail = binding.joinEmail.text.toString()
            // 이메일 주소 값이 없을 때
            if(userEmail == "")
                Toast.makeText(applicationContext, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            // 이메일 주소가 형식에 맞지 않을 때
            else if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
                Toast.makeText(applicationContext, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
            // 올바른 이메일 주소 형식일 때
            else if(Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                val data = EmailModel(userEmail,"NEWUSER")
                api.post_email(data).enqueue(object : Callback<PostResult> {
                    @SuppressLint("SuspiciousIndentation")
                    override fun onResponse(call: Call<PostResult>, response: Response<PostResult>) {
                        Log.d("log", response.toString())
                        Log.d("body : ", response.body().toString())
                        Log.d("status : ", response.body()?.status.toString())
                        Log.d("status : ", response.body()?.message.toString())
                        Log.d("status : ", response.body()?.result.toString())
                        binding.joinEmailButton.text = "재전송"
                        binding.emailConfirmLayout.visibility = View.VISIBLE

                    if(response.body().toString().isNotEmpty())
                        try {
                            Toast.makeText(applicationContext, "메일로 인증 코드가 발송 되었습니다.", Toast.LENGTH_SHORT).show()
                            val jsonObject = response.body()?.result
                            val userToken = jsonObject?.get("signupVerifyToken").toString().replace("\"","")

                            binding.emailConfirmButton.setOnClickListener{
                                val confirmEmail = binding.joinEmailConfirm.text.toString()
                                Log.d("status : ", userToken)
                                Log.d("status : ", confirmEmail)

                                if(confirmEmail == userToken) {
                                    Toast.makeText(
                                        applicationContext,
                                        "인증이 완료 되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.joinEmail.isEnabled = false
                                    binding.joinEmailButton.isEnabled = false
                                    binding.joinEmailConfirm.isEnabled = false
                                    binding.emailConfirmButton.isEnabled = false

                                    binding.joinEmail.setBackgroundResource(R.drawable.edit_text_background_false)
                                    binding.joinEmailConfirm.setBackgroundResource(R.drawable.edit_text_background_false)

                                    emailConfirm = "true"
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "인증번호가 틀렸습니다.\n다시 확인해주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        } catch(e: JSONException){
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call<PostResult>, t: Throwable) {
                        // 실패
                        Log.d("log", t.message.toString())
                        Log.d("log", "fail")
                    }
                })
            }
        }

        binding.joinPw.addTextChangedListener(object : TextWatcher {
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

        binding.joinRePw.addTextChangedListener(object : TextWatcher {
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

        binding.joinPw.addTextChangedListener(object : TextWatcher{ // 비밀번호 유효성 검사
            val pwPattern =
                "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&]).{8,20}.\$" // 8~20자, 영문, 숫자, 특수문자

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 비밀번호 editText를 문자열 값으로 받기
                val userPassword = binding.joinPw.text.toString()
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

        binding.joinRePw.addTextChangedListener(object : TextWatcher{ // 비밀번호 확인

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 비밀번호, 비밀번호 확인 editText를 문자열 값으로 받기
                val userPassword = binding.joinPw.text.toString()
                val userRePassword = binding.joinRePw.text.toString()
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

        binding.joinName.addTextChangedListener(object : TextWatcher{ // 닉네임 유효성 검사

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 닉네임 editText를 문자열 값으로 받기
                val userName = binding.joinName.text.toString()
                // 닉네임 값이 없을 때
                if(userName == "")
                    binding.nameValidate.visibility = View.GONE
                // 닉네임 형식이 맞지 않을 때
                else if(userName.toByteArray(Charset.defaultCharset()).size > 18)
                    binding.nameValidate.visibility = View.VISIBLE
                // 올바른 닉네임 형식일 때
                else if(userName.toByteArray(Charset.defaultCharset()).size <= 18)
                    binding.nameValidate.visibility = View.GONE

            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.joinBtn.setOnClickListener {//회원가입 버튼 클릭 시
            binding.joinBtn.isEnabled = false

            val email = binding.joinEmail.text.toString()
            val pw = binding.joinPw.text.toString()
            val rePw = binding.joinRePw.text.toString()
            val name = binding.joinName.text.toString()

            val pwPattern =
                "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&]).{8,20}.\$" // 8~20자, 영문, 숫자, 특수문자

            if(email == "") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("이메일을 입력해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.joinBtn.isEnabled = true
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                val builder = AlertDialog.Builder(this)
                builder.setMessage("유효한 형식의 이메일을 입력해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.joinBtn.isEnabled = true
            }
            else if(emailConfirm == "false") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("이메일 인증을 해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.joinBtn.isEnabled = true
            }
            else if(pw == "") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("비밀번호를 입력해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.joinBtn.isEnabled = true
            }
            else if(!Pattern.matches(pwPattern, pw)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("유효한 형식의 비밀번호를 입력해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.joinBtn.isEnabled = true
            }
            else if(rePw == "") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("비밀번호 확인을 입력 해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.joinBtn.isEnabled = true
            }
            else if(pw != rePw) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("비밀번호를 다시 확인해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.joinBtn.isEnabled = true
            }
            else if(name == "") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("유효한 형식의 닉네임을 입력해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.joinBtn.isEnabled = true
            }
            else if(name.toByteArray(Charset.defaultCharset()).size > 18) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("유효한 형식의 닉네임을 입력 해주세요.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // OK button clicked
                        dialog.dismiss()
                    }
                val dialog = builder.create()
                dialog.show()
                binding.joinBtn.isEnabled = true
            }
            else {
                val data = JoinModel(email,pw,name, isPremium, agreeWithEmail)
                api.post_join(data).enqueue(object : Callback<PostLoginResult> {
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
                                    "회원가입이 완료 되었습니다.",
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
                                binding.joinBtn.isEnabled = true
                            }
                        } else if (response.code() == 409) {
                            val errorResponseBodyString = response.errorBody()?.string()

                            // Parse the error response body using Gson
                            val errorResponse = gson.fromJson(errorResponseBodyString, ErrorResponse::class.java)

                            // Access the necessary information from the error response
                            val message = errorResponse.message
                            val errorCode = errorResponse.errorCode

                            Log.d("errorBody", "$message/$errorCode")
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                            binding.joinBtn.isEnabled = true
                        }
                    }

                    override fun onFailure(call: Call<PostLoginResult>, t: Throwable) {
                        // 실패
                        Log.d("log",t.message.toString())
                        Log.d("log","fail")
                        binding.joinBtn.isEnabled = true
                    }
                })
            }
        }

    }

    private fun updateButtonState() {
        val isCheckbox1Checked = binding.checkbox2.isChecked
        val isCheckbox2Checked = binding.checkbox3.isChecked

        if (isCheckbox1Checked && isCheckbox2Checked) {
            binding.joinBtn.isEnabled = true
            binding.joinBtn.setBackgroundResource(R.drawable.button_background)
            binding.joinBtn.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            binding.joinBtn.isEnabled = false
            binding.joinBtn.setBackgroundResource(R.drawable.button_background2)
            binding.joinBtn.setTextColor(Color.parseColor("#9FAEF2"))
        }
    }

    private fun updateCheckBox1State() {
        val isCheckbox1Checked = binding.checkbox2.isChecked
        val isCheckbox2Checked = binding.checkbox3.isChecked
        val isCheckbox3Checked = binding.checkbox4.isChecked

        if (isCheckbox1Checked && isCheckbox2Checked && isCheckbox3Checked) {
            binding.checkbox1.isChecked = true
        } else {
            binding.checkbox1.isChecked = false
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}