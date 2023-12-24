package com.reptimate.iot_teamnova

import APIS
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.reptimate.iot_teamnova.Retrofit.GetResult
import com.reptimate.iot_teamnova.Scheduling.ScheduleActivity
import com.reptimate.iot_teamnova.User.MainActivity
import com.reptimate.iot_teamnova.User.SettingActivity
import com.reptimate.iot_teamnova.User.UserEditActivity
import com.reptimate.iot_teamnova.databinding.FragAccountBinding
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountFragment : Fragment() {
    private var _binding:FragAccountBinding? = null
    private val binding get() = _binding!!
    private val api = APIS.create()
    private lateinit var customProgressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 1. 뷰 바인딩 설정
        _binding = FragAccountBinding.inflate(inflater, container, false)

//        binding.cage.setOnClickListener {
//            val i = Intent(activity, CageActivity::class.java)
//            startActivity(i)
//        }

        binding.swipeRefreshLayout.setOnRefreshListener {

            loadUserInfo(this.requireContext())

            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.scheduling.isEnabled = false
        binding.board.isEnabled = false
        binding.edit.isEnabled = false
        binding.setting.isEnabled = false

        binding.scheduling.setOnClickListener {
            val i = Intent(activity, ScheduleActivity::class.java)
            startActivity(i)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.board.setOnClickListener {
            val i = Intent(activity, BoardWebViewActivity::class.java)
            startActivity(i)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.edit.setOnClickListener {
            val i = Intent(activity, UserEditActivity::class.java)
            startActivity(i)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.setting.setOnClickListener {
            val i = Intent(activity, SettingActivity::class.java)
            startActivity(i)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Fragment 레이아웃 뷰 반환
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        //회원정보 로딩
        loadUserInfo(this.requireContext())
    }

    fun loadUserInfo(context : Context) {
        customProgressDialog = ProgressDialog(this.requireActivity())

        customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        customProgressDialog.show()

        api.get_users().enqueue(object : Callback<GetResult> {
            override fun onResponse(call: Call<GetResult>, response: Response<GetResult>) {
                Log.d("log",response.toString())
                Log.d("body_log", response.body().toString())
                if (response.isSuccessful) {
                    // Request was successful
                    // Handle the response body here
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val jsonObject = response.body()?.result
                            val getIdx = jsonObject?.get("idx").toString().replace("\"","") // 회원 idx
                            val getEmail = jsonObject?.get("email").toString().replace("\"","") // 회원 email
                            val getNickName = jsonObject?.get("nickname").toString().replace("\"","") // 회원 닉네임
                            val getProfilePath = jsonObject?.get("profilePath").toString().replace("\"","") // 회원 프로필 이미지 경로
                            val getIsPremium = jsonObject?.get("is_premium").toString().replace("\"","") // 회원 프리미엄
                            val getAgreeWithMarketing = jsonObject?.get("agree_with_marketing").toString().replace("\"","") // 회원 마케팅 정보 수신 동의 여부
                            val getCreatedAt = jsonObject?.get("created_at").toString().replace("\"","") // 회원 가입일 시
                            val getLoginMethod = jsonObject?.get("loginMethod").toString().replace("\"","") // 회원 가입일 시

                            customProgressDialog.dismiss()

                            if(getLoginMethod == "KAKAO") {
                                binding.kakao.visibility = View.VISIBLE
                            }
                            if(getLoginMethod == "GOOGLE") {
                                binding.google.visibility = View.VISIBLE
                            }

                            binding.name.text = getNickName // 닉네임 텍스트뷰에 띄우기
                            binding.email.text = getEmail // 이메일 텍스트뷰에 띄우기

                            if(getProfilePath != "" && getProfilePath != "null"){
                                Glide.with(context!!).load(getProfilePath).override(130, 130)
                                    .into(binding.profile)
                            } else {
                                binding.profile.setImageResource(R.drawable.reptimate_logo)
                            }

                            binding.scheduling.isEnabled = true
                            binding.board.isEnabled = true
                            binding.edit.isEnabled = true
                            binding.setting.isEnabled = true

                            Log.d("body_log", getIdx)
                        } catch(e: JSONException){
                            e.printStackTrace()
                            customProgressDialog.dismiss()

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("오류")
                                .setMessage("서버와의 오류가 발생하였습니다.\n인터넷 연결을 확인해주세요.")
                                .setPositiveButton("확인",
                                    DialogInterface.OnClickListener { _, _ ->
                                    })
                            // 다이얼로그를 띄워주기
                            builder.show()
                        }
                    }
                    else {
                        customProgressDialog.dismiss()
                        // Handle the case where response.body() is null
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("오류")
                            .setMessage("서버와의 오류가 발생하였습니다.\n인터넷 연결을 확인해주세요.")
                            .setPositiveButton("확인",
                                DialogInterface.OnClickListener { _, _ ->
                                })
                        // 다이얼로그를 띄워주기
                        builder.show()
                    }
                }
                else if (response.code() == 401) {
                    customProgressDialog.dismiss()
                    Log.d("토큰 결과 : ", "401에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            loadUserInfo(context)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                }
                else if (response.code() == 404) {
                    customProgressDialog.dismiss()
                    Log.d("토큰 결과 : ", "404에러. 토큰 재발급 시도.")
                    PreferenceUtil(binding.root.context).checkToken { success ->
                        if (success) {
                            loadUserInfo(context)
                        } else {
                            // Handle the case where token check failed
                            Log.d("토큰 결과 : ", "끝 너 멈춰.")
                        }
                    }
                } else {
                    customProgressDialog.dismiss()
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("오류")
                        .setMessage("서버와의 오류가 발생하였습니다.\n인터넷 연결을 확인해주세요.")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { _, _ ->
                            })
                    // 다이얼로그를 띄워주기
                    builder.show()
                }
            }

            override fun onFailure(call: Call<GetResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")

                customProgressDialog.dismiss()

                val builder = AlertDialog.Builder(context)
                builder.setTitle("오류")
                    .setMessage("서버 통신에 오류가 발생하였습니다.\n인터넷 연결을 확인해주세요.")
                    .setPositiveButton("확인",
                        DialogInterface.OnClickListener { _, _ ->
                        })
                // 다이얼로그를 띄워주기
                builder.show()
            }
        })
    }

    override fun onPause() {
        super.onPause()

        // Clear the current activity in MainApplication
//        MainApplication.getInstance().setCurrentActivity(null)
    }
}