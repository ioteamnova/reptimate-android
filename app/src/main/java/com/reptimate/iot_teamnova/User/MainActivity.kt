package com.reptimate.iot_teamnova.User

import APIS
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.reptimate.iot_teamnova.HomeActivity
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.PreferenceUtil
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.GoogleLoginModel
import com.reptimate.iot_teamnova.Retrofit.KakaoLoginModel
import com.reptimate.iot_teamnova.Retrofit.LoginModel
import com.reptimate.iot_teamnova.Retrofit.PostResult
import com.reptimate.iot_teamnova.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    var fbToken = ""

    private val RC_SIGN_IN = 9001

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        Glide.with(applicationContext).load(com.reptimate.iot_teamnova.R.drawable.login_logo).into(binding.loginLogo)

        val preferenceUtil = PreferenceUtil(this)
        if (preferenceUtil.isLogin() != null) {
            finish()
            preferenceUtil.checkLogin()
        } else {

        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("fail : ", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            fbToken = msg.substringAfter("FCM registration token: ")
            Log.d("log", msg)
            Log.d("fb token 2 :", fbToken)

        })

        binding.loginBtn.setOnClickListener{
            println(fbToken)
            val id = binding.loginId.text.toString()
            val pw = binding.loginPw.text.toString()

            val data = LoginModel(id,pw,fbToken)
            api.post_login(data).enqueue(object : Callback<PostResult> {
                override fun onResponse(call: Call<PostResult>, response: Response<PostResult>) {
                    Log.d("log",response.toString())
                    Log.d("body_log", response.body().toString())
                    if(!response.body().toString().isEmpty()) {
                        if (response.body().toString() != "null") {
                            try {
                                val jsonObject = response.body()?.result
                                val accessToken = jsonObject?.get("accessToken").toString().replace("\"","")
                                val idx = jsonObject?.get("idx").toString().replace("\"","")
                                val refreshToken = jsonObject?.get("refreshToken").toString().replace("\"","")
                                val nickname = jsonObject?.get("nickname").toString().replace("\"","")
                                val profilePath = jsonObject?.get("profilePath").toString().replace("\"","")

                                MainApplication.prefs.setString("accessToken", accessToken)
                                MainApplication.prefs.setString("idx", idx)
                                MainApplication.prefs.setString("refreshToken", refreshToken)
                                MainApplication.prefs.setString("nickname", nickname)
                                MainApplication.prefs.setString("profilePath", profilePath)

                                Toast.makeText(
                                    applicationContext,
                                    "로그인에 성공하였습니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                intent.putExtra("join", "join")
                                startActivity(intent)
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                finish()
                            } catch(e: JSONException){
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "아이디 혹은 비밀번호를 확인해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<PostResult>, t: Throwable) {
                    // 실패
                    Log.d("log",t.message.toString())
                    Log.d("log","fail")
                }
            })
        }

        binding.join.setOnClickListener{
            val intent = Intent(this, JoinActivity::class.java)
            intent.putExtra("join", "join")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.findPw.setOnClickListener{
            val intent = Intent(this, FindPwActivity::class.java)
            intent.putExtra("findPw", "findPw")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // 로그인 버튼 클릭 리스너
        binding.kakaoLogin.setOnClickListener {
            // Start Kakao login process
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    // Handle error
                } else if (token != null) {
                    //카카오 로그인을 위한 토큰을 발급 받아서 서버로 보냄.
                    // Get authorization code
                    val code = token.accessToken
                    Log.d("authorization code : ", code)
                    // Use authorization code to get access token and refresh token
                    val data = KakaoLoginModel(code, "KAKAO", fbToken)
                    api.post_kakao_login(data).enqueue(object : Callback<PostResult> {
                        override fun onResponse(call: Call<PostResult>, response: Response<PostResult>) {
                            Log.d("log",response.toString())
                            Log.d("body_log", response.body().toString())
                            if(!response.body().toString().isEmpty()) {
                                if (response.body().toString() != "null") {
                                    //TODO 카카오 로그인 성공 시 코드
                                    val jsonObject = response.body()?.result
                                    val accessToken = jsonObject?.get("accessToken").toString().replace("\"","")
                                    val idx = jsonObject?.get("idx").toString().replace("\"","")
                                    val refreshToken = jsonObject?.get("refreshToken").toString().replace("\"","")
                                    val nickname = jsonObject?.get("nickname").toString().replace("\"","")
                                    val profilePath = jsonObject?.get("profilePath").toString().replace("\"","")

                                    MainApplication.prefs.setString("accessToken", accessToken)
                                    MainApplication.prefs.setString("idx", idx)
                                    MainApplication.prefs.setString("refreshToken", refreshToken)
                                    MainApplication.prefs.setString("nickname", nickname)
                                    MainApplication.prefs.setString("profilePath", profilePath)

                                    Toast.makeText(
                                        applicationContext,
                                        "로그인에 성공하였습니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                    intent.putExtra("join", "join")
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "카카오 로그인 오류.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<PostResult>, t: Throwable) {
                            // 실패
                            Log.d("log",t.message.toString())
                            Log.d("log","fail")
                        }
                    })
                }
            }
            UserApiClient.instance.run {
                // Start Kakao login process
                if (isKakaoTalkLoginAvailable(this@MainActivity)) { // 카카오톡 앱을 사용한 카카오 로그인
                    loginWithKakaoTalk(this@MainActivity, callback = callback)
                } else { // 웹을 사용한 카카오 로그인
                    loginWithKakaoAccount(this@MainActivity, callback = callback)
                }
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(com.reptimate.iot_teamnova.R.string.default_web_client_id))
                    .requestEmail()
                    .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleLogin.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle the sign-in result
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnCompleteListener {
                try {
                    val account = task.getResult(ApiException::class.java)
                    val accessToken = account?.serverAuthCode // 서버 인가 코드
                    val personName = account?.displayName // 유저 전체 이름 (ex : 김유수)
                    val personGivenName = account?.givenName // 유저 이름 (ex : 유수)
                    val personFamilyName = account?.familyName // 유저 성 (ex : 김)
                    val personEmail = account?.email // 유저 이메일
                    val personId = account?.id // 유저 아이디
                    val idToken = account?.idToken // 유저 아이디 토큰

                    val data = GoogleLoginModel(personName, personEmail, "GOOGLE", fbToken)
                    api.post_google_login(data).enqueue(object : Callback<PostResult> {
                        override fun onResponse(call: Call<PostResult>, response: Response<PostResult>) {
                            Log.d("log",response.toString())
                            Log.d("body_log", response.body().toString())
                            if(!response.body().toString().isEmpty()) {
                                if (response.body().toString() != "null") {
                                    //TODO 구글 로그인 성공 시 코드
                                    val jsonObject = response.body()?.result
                                    val accessToken = jsonObject?.get("accessToken").toString().replace("\"","")
                                    val idx = jsonObject?.get("idx").toString().replace("\"","")
                                    val refreshToken = jsonObject?.get("refreshToken").toString().replace("\"","")
                                    val nickname = jsonObject?.get("nickname").toString().replace("\"","")
                                    val profilePath = jsonObject?.get("profilePath").toString().replace("\"","")

                                    MainApplication.prefs.setString("accessToken", accessToken)
                                    MainApplication.prefs.setString("idx", idx)
                                    MainApplication.prefs.setString("refreshToken", refreshToken)
                                    MainApplication.prefs.setString("nickname", nickname)
                                    MainApplication.prefs.setString("profilePath", profilePath)

                                    Toast.makeText(
                                        applicationContext,
                                        "로그인에 성공하였습니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                    intent.putExtra("join", "join")
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "구글 로그인 오류.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<PostResult>, t: Throwable) {
                            // 실패
                            Log.d("log",t.message.toString())
                            Log.d("log","fail")
                        }
                    })

                    Log.d("MainActivity", "handleSignInResult:$account")
                    Log.d("MainActivity", "handleSignInResult:personName $personName")
                    Log.d("MainActivity", "handleSignInResult:personGivenName $personGivenName")
                    Log.d("MainActivity", "handleSignInResult:personEmail $personEmail")
                    Log.d("MainActivity", "handleSignInResult:personId $personId")
                    Log.d("MainActivity", "handleSignInResult:personFamilyName $personFamilyName")
                    // access other user information as needed
                    Log.d("MainActivity", "Access token: $accessToken")
                    Log.d("MainActivity", "handleSignInResult:idToken $idToken")
                } catch(e: ApiException){
                    Log.e("MainActivity", "Sign-in failed with error code: ${e.statusCode}")
                }
            }
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}