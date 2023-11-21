package com.reptimate.iot_teamnova.User

import APIS
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.reptimate.iot_teamnova.PreferenceUtil
import com.reptimate.iot_teamnova.R
import com.reptimate.iot_teamnova.Retrofit.GetUserResult
import com.reptimate.iot_teamnova.Retrofit.UserDelModel
import com.reptimate.iot_teamnova.databinding.ActivitySettingBinding
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SettingActivity : AppCompatActivity() {


    val binding by lazy { ActivitySettingBinding.inflate(layoutInflater) }
    val api = APIS.create()
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val preferenceUtil = PreferenceUtil(this)

        //뒤로가기(back) 버튼 클릭 시
        binding.backBtn.setOnClickListener{
            finish()
        }

        binding.allNoti.setOnCheckedChangeListener { _, isChecked  ->
            if (isChecked) {
                // 스위치가 ON일 때 실행할 코드
                binding.boardNoti.isEnabled = true
                binding.adNoti.isEnabled = true
                binding.cageNoti.isEnabled = true
            } else {
                // 스위치가 OFF일 때 실행할 코드
                binding.boardNoti.isEnabled = false
                binding.adNoti.isEnabled = false
                binding.cageNoti.isEnabled = false
            }
        }

        //로그아웃 버튼 클릭 시
        binding.logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                        preferenceUtil.editor.clear()
                        preferenceUtil.editor.commit()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        Toast.makeText(applicationContext,"로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                    })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener{ dialogInterface, i ->
                        return@OnClickListener
                    })
            // 다이얼로그를 띄워주기
            builder.show()
        }

        binding.userDel.setOnClickListener {
//            val txtEdit = EditText(applicationContext)
//            txtEdit.hint = "비밀번호"
//            txtEdit.inputType = 0x00000081
//            txtEdit.setPadding(5)
//            txtEdit.setBackgroundResource(R.drawable.edit_text_background)
            val view = LayoutInflater.from(applicationContext).inflate(R.layout.dialog_user_del, null)
            val et = view.findViewById<EditText>(R.id.password)
            // 다이얼로그를 생성하기 위해 Builder 클래스 생성자를 이용해 줍니다.
            val builder = AlertDialog.Builder(this)
            builder.setTitle("본인 확인")
                .setMessage("본인 확인을 위한 비밀번호를 입력해주세요.\n확인 버튼을 눌러주시면 탈퇴 절차가 완료됩니다.")
                .setView(view)
                .setNegativeButton("취소",
                DialogInterface.OnClickListener{ dialog, id ->
                    return@OnClickListener
                })
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                        if(et.text.toString() == "") {
                            Toast.makeText(applicationContext, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            api.get_user_del(UserDelModel(et.text.toString())).enqueue(object : Callback<GetUserResult> {
                                override fun onResponse(call: Call<GetUserResult>, response: Response<GetUserResult>) {
                                    Log.d("log",response.toString())
                                    Log.d("body_log", response.body().toString())
                                    try {
                                        val message = response.body()?.message

                                        if(message == "Success"){
                                            preferenceUtil.editor.clear()
                                            preferenceUtil.editor.commit()
                                            val intent = Intent(applicationContext, MainActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                            Toast.makeText(applicationContext, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(applicationContext, "비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
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
                    })
            // 다이얼로그를 띄워주기
            val alertDialog = builder.create()
            alertDialog.show()
            //다이얼로그 메시지 텍스트 크기 조정
            val messageView = alertDialog.findViewById<TextView>(android.R.id.message)
            messageView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}