package com.reptimate.iot_teamnova.User

import APIS
import APIS.Companion.createBaseService
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.*
import com.reptimate.iot_teamnova.databinding.ActivityUserEditBinding
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class UserEditActivity : AppCompatActivity() {

    var nameValidate = true
    var emailValidate = true

    var isNameChanged = false
    var isEmailChanged = false
    var isImageChanged = false

    lateinit var getProfilePath: String
    var photoUri: Uri? = null

    var bitmap: Bitmap? = null

    val Picture = 1
    val Gallery = 2

    private val binding by lazy { ActivityUserEditBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gson = Gson()

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        //뒤로가기(back) 버튼 클릭 시
        binding.backBtn.setOnClickListener{
            finish()
        }

        val basicUri = Uri.parse("android.resource://" + packageName + "/" + R.drawable.reptimate_logo)

        api.get_users().enqueue(object : Callback<GetResult> {
            override fun onResponse(call: Call<GetResult>, response: Response<GetResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                try {
                    var jsonObject = response.body()?.result
                    var getIdx = jsonObject?.get("idx").toString().replace("\"","") // 회원 idx
                    var getEmail = jsonObject?.get("email").toString().replace("\"","") // 회원 email
                    var getNickName = jsonObject?.get("nickname").toString().replace("\"","") // 회원 닉네임
                    getProfilePath = jsonObject?.get("profilePath").toString().replace("\"","") // 회원 프로필 이미지 경로
                    var getIsPremium = jsonObject?.get("is_premium").toString().replace("\"","") // 회원 프리미엄
                    var getAgreeWithMarketing = jsonObject?.get("agree_with_marketing").toString().replace("\"","") // 회원 마케팅 정보 수신 동의 여부
                    var getCreatedAt = jsonObject?.get("created_at").toString().replace("\"","") // 회원 가입일 시
                    val getLoginMethod = jsonObject?.get("loginMethod").toString().replace("\"","") // 회원 가입일 시

                    if(getLoginMethod == "KAKAO") {
                        binding.socialValidate.visibility = View.VISIBLE

                        binding.emailEt.isEnabled = false
                        binding.emailBtn.isEnabled = false
                        binding.emailEt.setBackgroundResource(R.drawable.edit_text_background_false)
                        binding.emailBtn.setBackgroundResource(R.drawable.button_background2)
                        binding.emailBtn.setTextColor(Color.parseColor("#9FAEF2"))

                        binding.passwordBtn.isEnabled = false
                        binding.passwordBtn.setBackgroundResource(R.drawable.button_background2)
                        binding.passwordBtn.setTextColor(Color.parseColor("#9FAEF2"))
                    }
                    if(getLoginMethod == "GOOGLE") {
                        binding.socialValidate.visibility = View.VISIBLE
                        binding.emailEt.isEnabled = false
                        binding.emailBtn.isEnabled = false
                        binding.emailEt.setBackgroundResource(R.drawable.edit_text_background_false)
                        binding.emailBtn.setBackgroundResource(R.drawable.button_background2)
                        binding.emailBtn.setTextColor(Color.parseColor("#9FAEF2"))

                        binding.passwordBtn.isEnabled = false
                        binding.passwordBtn.setBackgroundResource(R.drawable.button_background2)
                        binding.passwordBtn.setTextColor(Color.parseColor("#9FAEF2"))
                    }

                    binding.nameEt.setText(getNickName) // 닉네임 텍스트뷰에 띄우기
                    binding.emailEt.setText(getEmail) // 이메일 텍스트뷰에 띄우기

                    if(getProfilePath != "" && getProfilePath != "null"){ // 프로필이 존재할 때
                        Glide.with(applicationContext).load(getProfilePath).override(130, 130)
                            .into(binding.profile)
                    } else {
                        binding.profile.setImageResource(R.drawable.reptimate_logo)
                    }

                    binding.profile.setOnClickListener{
                        //사진 관련 권한 허용
                        if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED &&
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED
                        ) else {
                            ActivityCompat.requestPermissions(
                                this@UserEditActivity, arrayOf<String>(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ), 0
                            )
                        }

                        val info = arrayOf<CharSequence>("사진 촬영", "사진 앨범 선택", "기본 이미지로 변경")

                        val builder = AlertDialog.Builder(this@UserEditActivity)
                        builder.setTitle("업로드 이미지 선택")
                        builder.setItems(
                            info
                        ) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                                    startActivityForResult(intent, Picture)
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                }
                                1 -> {
                                    // 사진 앨범 선택
                                    val intent = Intent(Intent.ACTION_PICK)
                                    intent.type = MediaStore.Images.Media.CONTENT_TYPE
                                    intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                    startActivityForResult(
                                        intent,
                                        Gallery
                                    )
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                }
                                2 -> {
                                    // 기본 이미지로 변경
                                    photoUri = null
                                    bitmap = null
                                    getProfilePath = ""
                                    Glide.with(this@UserEditActivity).load(R.drawable.reptimate_logo).override(130, 130)
                                        .into(binding.profile)
                                    Toast.makeText(
                                        applicationContext,
                                        "기본 이미지로 변경되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            dialog.dismiss()
                        }
                        builder.show()
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

                    binding.emailBtn.setOnClickListener{
                        if (emailValidate) {
                            isEmailChanged = true
                            binding.emailBtn.text = "메일 전송"
                            emailValidate = false
                            binding.emailEt.isEnabled = true
                            binding.emailEt.requestFocus()
                            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.showSoftInput(binding.emailEt, InputMethodManager.SHOW_IMPLICIT)
                        } else {
                            // 이메일 editText를 문자열 값으로 받기
                            val userEmail = binding.emailEt.text.toString()
                            // 이메일 주소 값이 없을 때
                            if (userEmail == "")
                                Toast.makeText(
                                    applicationContext,
                                    "이메일을 입력해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            if (userEmail == getEmail)
                                Toast.makeText(
                                    applicationContext,
                                    "기존에 사용 중인 이메일입니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            // 이메일 주소가 형식에 맞지 않을 때
                            else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
                                Toast.makeText(
                                    applicationContext,
                                    "올바른 이메일 형식을 입력해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                                            binding.emailConfirmLayout.visibility = View.VISIBLE
                                            binding.confirmEmailEt.isEnabled = true
                                            binding.confirmEmailBtn.isEnabled = true
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
                                                    binding.emailBtn.text = "재설정"
                                                    emailValidate = true
                                                    binding.emailEt.isEnabled = false
                                                    binding.confirmEmailEt.isEnabled = false
                                                    binding.confirmEmailBtn.isEnabled = false
                                                    binding.emailConfirmLayout.visibility = View.GONE
                                                } else {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        "인증번호가 틀렸습니다.\n다시 확인해주세요.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                        } catch (e: JSONException) {
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
                    }

                    binding.passwordEt.addTextChangedListener(object : TextWatcher { // 비밀번호 유효성 검사
                        val pwPattern =
                            "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&]).{8,20}.\$" // 8~20자, 영문, 숫자, 특수문자

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }
                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            // 비밀번호 editText를 문자열 값으로 받기
                            val userPassword = binding.passwordEt.text.toString()
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

                    binding.rePasswordEt.addTextChangedListener(object : TextWatcher { // 비밀번호 확인

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }
                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            // 비밀번호, 비밀번호 확인 editText를 문자열 값으로 받기
                            val userPassword = binding.passwordEt.text.toString()
                            val userRePassword = binding.rePasswordEt.text.toString()
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

                    binding.nameEt.addTextChangedListener(object : TextWatcher { // 닉네임 유효성 검사

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }
                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            // 닉네임 editText를 문자열 값으로 받기
                            val userName = binding.nameEt.text.toString()
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

                    binding.nameBtn.setOnClickListener {

                        if (nameValidate) {
                            binding.nameBtn.text = "중복 확인"
                            isNameChanged = true
                            nameValidate = false
                            binding.nameEt.isEnabled = true
                            binding.nameEt.requestFocus()
                            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.showSoftInput(binding.nameEt, InputMethodManager.SHOW_IMPLICIT)
                        } else {
                            // 닉네임 editText를 문자열 값으로 받기
                            val userName = binding.nameEt.text.toString()
                            // 닉네임 값이 없을 때
                            if(userName == "")
                                binding.nameValidate.visibility = View.GONE
                            // 닉네임 형식이 맞지 않을 때
                            else if(userName.toByteArray(Charset.defaultCharset()).size > 18)
                                Toast.makeText(applicationContext, "닉네임 형식에 맞게 입력해주세요.", Toast.LENGTH_SHORT).show()
                            // 올바른 닉네임 형식일 때
                            else if(userName.toByteArray(Charset.defaultCharset()).size <= 18) {
                                val data = NickNameModel(userName)
                                api.post_nickName(data).enqueue(object : Callback<PostLoginResult> {
                                    @SuppressLint("SuspiciousIndentation")
                                    override fun onResponse(
                                        call: Call<PostLoginResult>,
                                        response: Response<PostLoginResult>
                                    ) {
                                        Log.d("log", response.toString())
                                        Log.d("body_log", response.body().toString())
                                        try {
                                            val jsonObject = response.body()?.result
                                            Log.d("result : ", jsonObject.toString())
                                        if(jsonObject.toString() == "false") {
                                            Toast.makeText(applicationContext, "사용 가능한 닉네임 입니다.", Toast.LENGTH_SHORT).show()
                                            binding.nameEt.isEnabled = false
                                            binding.nameBtn.text = "재설정"
                                            nameValidate = true
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "이미 사용 중인 닉네임 입니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        } catch (e: JSONException) {
                                            e.printStackTrace()
                                        }
                                    }

                                    override fun onFailure(call: Call<PostLoginResult>, t: Throwable) {
                                        // 실패
                                        Log.d("log", t.message.toString())
                                        Log.d("log", "fail")
                                    }
                                })
                            }
                        }
                    }

                    binding.passwordBtn.setOnClickListener {
                        binding.passwordBtn.visibility = View.GONE
                        binding.passwordLayout.visibility = View.VISIBLE

                        binding.passwordConfirmBtn.setOnClickListener {
                            val cur_password = binding.currentPasswordEt.text.toString()
                            val password = binding.passwordEt.text.toString()
                            val rePassword = binding.rePasswordEt.text.toString()

                            if(password == rePassword) {
                                val data = PasswordEditModel(cur_password, rePassword)
                                api.pass_edit(data).enqueue(object : Callback<GetUserResult> {
                                    override fun onResponse(
                                        call: Call<GetUserResult>,
                                        response: Response<GetUserResult>
                                    ) {
                                        Log.d("log", response.toString())
                                        Log.d("body_log", response.body().toString())
                                        if (!response.body().toString().isEmpty()) {
                                            if (response.body().toString() != "null") {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "비밀번호가 변경되었습니다.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                binding.passwordBtn.visibility = View.VISIBLE
                                                binding.passwordLayout.visibility = View.GONE
                                                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                                                currentFocus?.clearFocus()

                                            } else {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "현재 비밀번호가 틀렸습니다.\n비밀번호를 확인해주세요.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<GetUserResult>,
                                        t: Throwable
                                    ) {
                                        // 실패
                                        Log.d("log", t.message.toString())
                                        Log.d("log", "fail")
                                    }
                                })
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "비밀번호가 일치하지 않습니다.\n변경할 비밀번호를 재확인 해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    binding.passwordCancelBtn.setOnClickListener {
                        binding.passwordBtn.visibility = View.VISIBLE
                        binding.passwordLayout.visibility = View.GONE
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                        currentFocus?.clearFocus()
                    }

                    binding.confirmBtn.setOnClickListener {//완료 버튼 클릭 시
                        val email = binding.emailEt.text.toString()
                        val name = binding.nameEt.text.toString()
                        val profile = getProfilePath

                        Log.d("photoUri : ", photoUri.toString())
                        Log.d("bitmap : ", bitmap.toString())

                        if (!isNameChanged && !isEmailChanged && !isImageChanged) {
                            // 번경된 정보가 없을 때
                            Toast.makeText(applicationContext, "변경된 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                        } else if(isNameChanged && !nameValidate) {
                            Toast.makeText(applicationContext, "닉네임 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
                        } else if(isEmailChanged && !emailValidate) {
                            Toast.makeText(applicationContext, "이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show()
                        } else {
                            //이미지 변경 안되고
                            if(!isImageChanged) {
                                //둘다 변경 되었을 때
                                if (isEmailChanged && isNameChanged) {
                                    if (photoUri == null && bitmap == null) {
                                        api.user_edit(UserEditModel(email, name))
                                            .enqueue(object : Callback<GetUserResult> {
                                                override fun onResponse(
                                                    call: Call<GetUserResult>,
                                                    response: Response<GetUserResult>
                                                ) {
                                                    Log.d("log", response.toString())
                                                    Log.d("body_log", response.body().toString())
                                                    if (response.isSuccessful) {
                                                        // Request was successful
                                                        // Handle the response body here
                                                        val responseBody = response.body()
                                                        if (responseBody != null) {
                                                            // Process the response body
                                                            Toast.makeText(
                                                                applicationContext,
                                                                "정보 수정이 완료 되었습니다.",
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
                                                    }
                                                }
                                                override fun onFailure(
                                                    call: Call<GetUserResult>,
                                                    t: Throwable
                                                ) {
                                                    // 실패
                                                    Log.d("log", t.message.toString())
                                                    Log.d("log", "fail")
                                                }
                                            })
                                    }
                                }
                                //이메일만 변경
                                else if (isEmailChanged) {
                                    if (photoUri == null && bitmap == null) {
                                        api.user_edit2(UserEditModel2(email))
                                            .enqueue(object : Callback<GetUserResult> {
                                                override fun onResponse(
                                                    call: Call<GetUserResult>,
                                                    response: Response<GetUserResult>
                                                ) {
                                                    Log.d("log", response.toString())
                                                    Log.d("body_log", response.body().toString())
                                                    if (response.isSuccessful) {
                                                        // Request was successful
                                                        // Handle the response body here
                                                        val responseBody = response.body()
                                                        if (responseBody != null) {
                                                            // Process the response body
                                                            Toast.makeText(
                                                                applicationContext,
                                                                "정보 수정이 완료 되었습니다.",
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
                                                    }
                                                }
                                                override fun onFailure(
                                                    call: Call<GetUserResult>,
                                                    t: Throwable
                                                ) {
                                                    // 실패
                                                    Log.d("log", t.message.toString())
                                                    Log.d("log", "fail")
                                                }
                                            })
                                    }
                                }
                                //이름만 변경
                                else if (isNameChanged) {
                                    if (photoUri == null && bitmap == null) {
                                        api.user_edit3(UserEditModel3(name))
                                            .enqueue(object : Callback<GetUserResult> {
                                                override fun onResponse(
                                                    call: Call<GetUserResult>,
                                                    response: Response<GetUserResult>
                                                ) {
                                                    Log.d("log", response.toString())
                                                    Log.d("body_log", response.body().toString())
                                                    if (response.isSuccessful) {
                                                        // Request was successful
                                                        // Handle the response body here
                                                        val responseBody = response.body()
                                                        if (responseBody != null) {
                                                            // Process the response body
                                                            Toast.makeText(
                                                                applicationContext,
                                                                "정보 수정이 완료 되었습니다.",
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
                                                    }
                                                }
                                                override fun onFailure(
                                                    call: Call<GetUserResult>,
                                                    t: Throwable
                                                ) {
                                                    // 실패
                                                    Log.d("log", t.message.toString())
                                                    Log.d("log", "fail")
                                                }
                                            })
                                    }
                                }
                            }
                            //이미지가 변경되었을 때
                            if(isImageChanged) {
                                //둘다 변경 되었을 때
                                if (isEmailChanged && isNameChanged) {
                                    if (bitmap != null) {
                                        val file = convertBitmapToFile(bitmap!!)
                                        val requestFile =
                                            RequestBody.create("image/*".toMediaTypeOrNull(), file)
                                        val body = MultipartBody.Part.createFormData(
                                            "file",
                                            file.name,
                                            requestFile
                                        )
                                        Log.d("TAG : ", file.name)
                                        Log.d("전송되는 프로필 값 : ", body.toString())

                                        sendImage(MainApplication.prefs.token, email, name, body)
                                    }
                                    if (photoUri != null) {
                                        Log.d("프로필 사진 : ", "변경된 프로필 사진입니다.")
                                        Log.d("프로필 사진 경로 : ", photoUri.toString())
                                        val file =
                                            File(absolutelyPath(photoUri, this@UserEditActivity))
                                        val requestFile =
                                            RequestBody.create("image/*".toMediaTypeOrNull(), file)
                                        val body = MultipartBody.Part.createFormData(
                                            "file",
                                            file.name,
                                            requestFile
                                        )

                                        Log.d("TAG : ", file.name)
                                        Log.d("전송되는 프로필 값 : ", body.toString())

                                        sendImage(MainApplication.prefs.token, email, name, body)
                                    }
                                }
                                //이메일만 변경
                                else if (isEmailChanged) {
                                    if (bitmap != null) {
                                        val file = convertBitmapToFile(bitmap!!)
                                        val requestFile =
                                            RequestBody.create("image/*".toMediaTypeOrNull(), file)
                                        val body = MultipartBody.Part.createFormData(
                                            "file",
                                            file.name,
                                            requestFile
                                        )
                                        Log.d("TAG : ", file.name)
                                        Log.d("전송되는 프로필 값 : ", body.toString())

                                        sendImage2(MainApplication.prefs.token, email, body)
                                    }
                                    if (photoUri != null) {
                                        Log.d("프로필 사진 : ", "변경된 프로필 사진입니다.")
                                        Log.d("프로필 사진 경로 : ", photoUri.toString())
                                        val file =
                                            File(absolutelyPath(photoUri, this@UserEditActivity))
                                        val requestFile =
                                            RequestBody.create("image/*".toMediaTypeOrNull(), file)
                                        val body = MultipartBody.Part.createFormData(
                                            "file",
                                            file.name,
                                            requestFile
                                        )

                                        Log.d("TAG : ", file.name)
                                        Log.d("전송되는 프로필 값 : ", body.toString())

                                        sendImage2(MainApplication.prefs.token, email, body)
                                    }
                                }
                                //이름만 변경
                                else if (isNameChanged) {
                                    if (bitmap != null) {
                                        val file = convertBitmapToFile(bitmap!!)
                                        val requestFile =
                                            RequestBody.create("image/*".toMediaTypeOrNull(), file)
                                        val body = MultipartBody.Part.createFormData(
                                            "file",
                                            file.name,
                                            requestFile
                                        )
                                        Log.d("TAG : ", file.name)
                                        Log.d("전송되는 프로필 값 : ", body.toString())

                                        sendImage3(MainApplication.prefs.token, name, body)
                                    }
                                    if (photoUri != null) {
                                        Log.d("프로필 사진 : ", "변경된 프로필 사진입니다.")
                                        Log.d("프로필 사진 경로 : ", photoUri.toString())
                                        val file =
                                            File(absolutelyPath(photoUri, this@UserEditActivity))
                                        val requestFile =
                                            RequestBody.create("image/*".toMediaTypeOrNull(), file)
                                        val body = MultipartBody.Part.createFormData(
                                            "file",
                                            file.name,
                                            requestFile
                                        )

                                        Log.d("TAG : ", file.name)
                                        Log.d("전송되는 프로필 값 : ", body.toString())

                                        sendImage3(MainApplication.prefs.token, name, body)
                                    }
                                }
                                else if(!isEmailChanged && !isNameChanged) {
                                    if (bitmap != null) {
                                        val file = convertBitmapToFile(bitmap!!)
                                        val requestFile =
                                            RequestBody.create("image/*".toMediaTypeOrNull(), file)
                                        val body = MultipartBody.Part.createFormData(
                                            "file",
                                            file.name,
                                            requestFile
                                        )
                                        Log.d("TAG : ", file.name)
                                        Log.d("전송되는 프로필 값 : ", body.toString())

                                        sendImage4(MainApplication.prefs.token, body)
                                    }
                                    if (photoUri != null) {
                                        Log.d("프로필 사진 : ", "변경된 프로필 사진입니다.")
                                        Log.d("프로필 사진 경로 : ", photoUri.toString())
                                        val file =
                                            File(absolutelyPath(photoUri, this@UserEditActivity))
                                        val requestFile =
                                            RequestBody.create("image/*".toMediaTypeOrNull(), file)
                                        val body = MultipartBody.Part.createFormData(
                                            "file",
                                            file.name,
                                            requestFile
                                        )

                                        Log.d("TAG : ", file.name)
                                        Log.d("전송되는 프로필 값 : ", body.toString())

                                        sendImage4(MainApplication.prefs.token, body)
                                    }
                                }
                            }
                        }
                    }

                    Log.d("body_log", getIdx)
                } catch(e: JSONException){
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<GetResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })

    }

    // 절대경로 변환
    fun absolutelyPath(path: Uri?, context : Context): String {
        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        var index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()

        var result = c?.getString(index!!)

        return result!!
    }

    fun convertBitmapToFile(bitmap: Bitmap): File {
        val newFile = File(applicationContext.filesDir, "picture")
        val out = FileOutputStream(newFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        return newFile
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v("main onActivityResult", "onActivityResult 호출")
        if (resultCode == RESULT_OK) {
            Log.v("resultCode == RESULT_OK", "resultCode == RESULT_OK")
            Log.d("image data", data.toString())
            if (requestCode == Picture) { // 사진 촬영
                Log.v("Take A Picture", "requestCode == Take A Picture")

                if (resultCode == RESULT_OK && data?.hasExtra("data")!!) {
                    bitmap = data.extras?.get("data") as Bitmap
                    binding.profile.setImageBitmap(bitmap)
                    isImageChanged = true
                }

            }
            if (requestCode == Gallery) { // 앨범에서 선택
                Log.v("Pick From Gallery", "requestCode == Pick From Gallery")
                bitmap = null
                isImageChanged = true

                var ImageData: Uri? = data?.data
                if (ImageData != null) {
                    photoUri = ImageData
                } else {
                    photoUri == null
                }
                binding.profile.setImageURI(photoUri)
            }
        }
    }

    // 카메라로 촬영한 이미지를 파일로 저장해준다
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            getProfilePath = absolutePath
        }
    }

    //웹서버로 이미지전송(이메일, 닉네임 변경)
    fun sendImage(Authorization: String?, email: String, name: String, file: MultipartBody.Part) {
        val gson = Gson()

        val getEmail = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
        val getName = RequestBody.create("text/plain".toMediaTypeOrNull(), name)

        val service = createBaseService(APIS.Companion.RetrofitUserEdit::class.java) //레트로핏 통신 설정
        val call = service.profileEdit("Bearer $Authorization", getEmail, getName, file) //통신 API 패스 설정

        call.enqueue(object : Callback<GetUserResult> {
            override fun onResponse(call: Call<GetUserResult>, response: Response<GetUserResult>) {
                Log.d("log", response.toString())
                Log.d("body_log", response.body().toString())
                if (response.isSuccessful) {
                    // Request was successful
                    // Handle the response body here
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // Process the response body
                        Toast.makeText(
                            applicationContext,
                            "정보 수정이 완료 되었습니다.",
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
                }
            }
            override fun onFailure(call: Call<GetUserResult>, t: Throwable) {
                Log.d("로그 ",t.message.toString())
            }
        })
    }
    //웹서버로 이미지전송(이메일 변경)
    fun sendImage2(Authorization: String?, email: String, file: MultipartBody.Part) {
        val gson = Gson()

        val getEmail = RequestBody.create("text/plain".toMediaTypeOrNull(), email)

        val service = createBaseService(APIS.Companion.RetrofitUserEdit::class.java) //레트로핏 통신 설정
        val call = service.profileEdit2("Bearer $Authorization", getEmail, file) //통신 API 패스 설정

        call.enqueue(object : Callback<GetUserResult> {
            override fun onResponse(call: Call<GetUserResult>, response: Response<GetUserResult>) {
                Log.d("log", response.toString())
                Log.d("body_log", response.body().toString())
                if (response.isSuccessful) {
                    // Request was successful
                    // Handle the response body here
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // Process the response body
                        Toast.makeText(
                            applicationContext,
                            "정보 수정이 완료 되었습니다.",
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
                    }
                } else if (response.code() == 409) {
                    val errorResponseBodyString = response.errorBody()?.string()

                    // Parse the error response body using Gson
                    val errorResponse =
                        gson.fromJson(errorResponseBodyString, ErrorResponse::class.java)

                    // Access the necessary information from the error response
                    val message = errorResponse.message
                    val errorCode = errorResponse.errorCode

                    Log.d("errorBody", "$message/$errorCode")
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<GetUserResult>, t: Throwable) {
                Log.d("로그 ",t.message.toString())
            }
        })
    }
    //웹서버로 이미지전송(닉네임 변경)
    fun sendImage3(Authorization: String?, name: String, file: MultipartBody.Part) {
        val getName = RequestBody.create("text/plain".toMediaTypeOrNull(), name)

        val service = createBaseService(APIS.Companion.RetrofitUserEdit::class.java) //레트로핏 통신 설정
        val call = service.profileEdit3("Bearer $Authorization", getName, file) //통신 API 패스 설정

        call.enqueue(object : Callback<GetUserResult> {
            override fun onResponse(call: Call<GetUserResult>, response: Response<GetUserResult>) {
                Log.d("로그 ", " : $response")
                Toast.makeText(
                    applicationContext,
                    "회원 정보 수정이 완료되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()

            }

            override fun onFailure(call: Call<GetUserResult>, t: Throwable) {
                Log.d("로그 ",t.message.toString())
            }
        })
    }

    //웹서버로 이미지전송(이미지만 변경)
    fun sendImage4(Authorization: String?, file: MultipartBody.Part) {

        val service = createBaseService(APIS.Companion.RetrofitUserEdit::class.java) //레트로핏 통신 설정
        val call = service.profileEdit4("Bearer $Authorization", file) //통신 API 패스 설정

        call.enqueue(object : Callback<GetUserResult> {
            override fun onResponse(call: Call<GetUserResult>, response: Response<GetUserResult>) {
                Log.d("로그 ", " : $response")
                Toast.makeText(
                    applicationContext,
                    "회원 정보 수정이 완료되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()

            }
            override fun onFailure(call: Call<GetUserResult>, t: Throwable) {
                Log.d("로그 ",t.message.toString())
            }
        })
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}