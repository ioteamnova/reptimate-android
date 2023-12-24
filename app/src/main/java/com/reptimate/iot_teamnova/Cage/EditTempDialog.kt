package com.reptimate.iot_teamnova.Cage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.databinding.DialogTempEditBinding
import org.eclipse.paho.client.mqttv3.*

class EditTempDialog : Activity(), MqttService.MqttCallbackListener, MqttManager.MqttManagerCallback{
    private lateinit var mqttManager: MqttManager
    private lateinit var mqttServiceConnection: MqttServiceConnection
    private lateinit var mqttService: MqttService

    var tem_ok = "1"

    private val binding by lazy { DialogTempEditBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // MqttService와 연결하기 위한 ServiceConnection 객체 생성
        mqttServiceConnection = MqttServiceConnection()

        // MqttService와 바인딩
        val seviceIntent = Intent(this, MqttService::class.java)
        bindService(seviceIntent, mqttServiceConnection, Context.BIND_AUTO_CREATE)

        // MqttService 인스턴스 가져오기
        mqttService = MqttService.getInstance()!!

        // MqttService에 MqttCallbackListener 등록
        mqttService?.setMqttCallbackListener(this)

        // MqttManager 인스턴스 생성
        mqttManager = MqttManager(applicationContext, "ssl://43.201.185.236:8883", MqttClient.generateClientId())

        // MqttManager에 콜백 설정
        mqttManager.setCallback(this)

        mqttManager.connect()

        val intent: Intent = getIntent()
        val boardTempname = intent.getStringExtra("boardTempname")
        val autoChkTemp = intent.getStringExtra("autoChkTemp")
        val maxTemp = intent.getStringExtra("maxTemp")
        val minTemp = intent.getStringExtra("minTemp")

        binding.temperatureMax.setText(maxTemp)
        binding.temperatureMin.setText(minTemp)

        if(autoChkTemp == "1"){
            binding.temperatureSwitch.isChecked = true
            binding.temperatrueLayout.visibility = View.VISIBLE
            tem_ok = "1"
        }
        if(autoChkTemp == "0") {
            binding.temperatureSwitch.isChecked = false
            binding.temperatrueLayout.visibility = View.GONE
            tem_ok = "0"
        }
        binding.temperatureMax.addTextChangedListener(object : TextWatcher {
            // et의 텍스트가 변경될 때
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            //            @SuppressLint("ResourceType")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // 텍스트가 변경될 때마다 함수 호출
                if (binding.temperatureMax.text.toString().isNotEmpty()) {
                    // 작성되어 있던 값을 지울 때 pareInt(" ") 에서 에러가 나기 때문에 위와 같은 과정 필요
                    if (binding.temperatureMax.text.toString().toInt() > 50) {
                        // 50 초과일 때
                        binding.temperatureMax.setText("50")
                        //et가 50으로 변경됨
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.temperatureMin.addTextChangedListener(object : TextWatcher {
            //모임 인원수 적는 et의 텍스트가 변경될 때
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            //            @SuppressLint("ResourceType")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //텍스트가 변경될 때마다 함수 호출
                if (binding.temperatureMin.text.toString().isNotEmpty()) {
                    // 작성되어 있던 값을 지울 때 pareInt(" ") 에서 에러가 나기 때문에 위와 같은 과정 필요
                    if (binding.temperatureMin.text.toString().toInt() > 50) {
                        // 50 초과일 때
                        binding.temperatureMin.setText("50")
                        //et가 50으로 변경됨
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.temperatureSwitch.setOnClickListener {
            if(binding.temperatureSwitch.isChecked) {
                binding.temperatrueLayout.visibility = View.VISIBLE

                binding.temperatureMin.isEnabled = true
                binding.temperatureMax.isEnabled = true
                tem_ok = "1"
            }
            else {
                binding.temperatrueLayout.visibility = View.GONE

                binding.temperatureMin.isEnabled = false
                binding.temperatureMax.isEnabled = false
                tem_ok = "0"
            }
        }

        binding.yesBtn.setOnClickListener {
            if(tem_ok == "1"){
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"autoChkTemp\":\"${tem_ok}\", \"maxTemp\":\"${binding.temperatureMax.text.toString()}\", \"minTemp\":\"${binding.temperatureMin.text.toString()}\"}"

                mqttServiceConnection.getService()?.mqttManager?.publish("temphumid/setrequest/nest", message)
            }
            if(tem_ok == "0"){
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"autoChkTemp\":\"${tem_ok}\", \"maxTemp\":\"0\", \"minTemp\":\"0\"}"

                mqttServiceConnection.getService()?.mqttManager?.publish("temphumid/setrequest/nest", message)
            }
            Toast.makeText(applicationContext, "히팅램프 세팅이 변경되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.noBtn.setOnClickListener {
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mqttServiceConnection)
    }

    override fun onMessageReceived(topic: String, message: String) {

    }

    override fun onMqttMessageReceived(topic: String, message: MqttMessage) {

    }
}