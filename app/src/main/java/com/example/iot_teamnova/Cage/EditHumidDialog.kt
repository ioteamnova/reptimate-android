package com.example.iot_teamnova.Cage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.example.iot_teamnova.MainApplication
import com.example.iot_teamnova.databinding.DialogHumidEditBinding
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage

class EditHumidDialog : Activity(), MqttService.MqttCallbackListener, MqttManager.MqttManagerCallback{
    private lateinit var mqttManager: MqttManager
    private lateinit var mqttServiceConnection: MqttServiceConnection
    private lateinit var mqttService: MqttService

    var hum_ok = "1"

    private val binding by lazy { DialogHumidEditBinding.inflate(layoutInflater) }
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
        val autoChkHumid = intent.getStringExtra("autoChkHumid")
        val maxHumid = intent.getStringExtra("maxHumid")
        val minHumid = intent.getStringExtra("minHumid")

        binding.humidityMax.setText(maxHumid)
        binding.humidityMin.setText(minHumid)

        if(autoChkHumid == "1"){
            binding.humiditySwitch.isChecked = true
            binding.humidityLayout.visibility = View.VISIBLE
            hum_ok = "1"
        }
        if(autoChkHumid == "0") {
            binding.humiditySwitch.isChecked = false
            binding.humidityLayout.visibility = View.GONE
            hum_ok = "0"
        }
        binding.humidityMax.addTextChangedListener(object : TextWatcher {
            // et의 텍스트가 변경될 때
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            //            @SuppressLint("ResourceType")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // 텍스트가 변경될 때마다 함수 호출
                if (binding.humidityMax.text.toString().isNotEmpty()) {
                    // 작성되어 있던 값을 지울 때 pareInt(" ") 에서 에러가 나기 때문에 위와 같은 과정 필요
                    if (binding.humidityMax.text.toString().toInt() > 100) {
                        // 50 초과일 때
                        binding.humidityMax.setText("100")
                        //et가 50으로 변경됨
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.humidityMin.addTextChangedListener(object : TextWatcher {
            //모임 인원수 적는 et의 텍스트가 변경될 때
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            //            @SuppressLint("ResourceType")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //텍스트가 변경될 때마다 함수 호출
                if (binding.humidityMin.text.toString().isNotEmpty()) {
                    // 작성되어 있던 값을 지울 때 pareInt(" ") 에서 에러가 나기 때문에 위와 같은 과정 필요
                    if (binding.humidityMin.text.toString().toInt() > 100) {
                        // 50 초과일 때
                        binding.humidityMin.setText("100")
                        //et가 50으로 변경됨
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.humiditySwitch.setOnClickListener {
            if(binding.humiditySwitch.isChecked) {
                binding.humidityLayout.visibility = View.VISIBLE

                binding.humidityMin.isEnabled = true
                binding.humidityMax.isEnabled = true
                hum_ok = "1"
            }
            else {
                binding.humidityLayout.visibility = View.GONE

                binding.humidityMin.isEnabled = false
                binding.humidityMax.isEnabled = false
                hum_ok = "0"
            }
        }

        binding.yesBtn.setOnClickListener {
            if(hum_ok == "1"){
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"autoChkHumid\":\"${hum_ok}\", \"maxHumid\":\"${binding.humidityMax.text.toString()}\", \"minHumid\":\"${binding.humidityMin.text.toString()}\"}"

                println(message)
                mqttServiceConnection.getService()?.mqttManager?.publish("temphumid/setrequest/nest", message)
            }
            if(hum_ok == "0"){
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"autoChkHumid\":\"${hum_ok}\", \"maxHumid\":\"0\", \"minHumid\":\"0\"}"

                println(message)
                mqttServiceConnection.getService()?.mqttManager?.publish("temphumid/setrequest/nest", message)
            }
            Toast.makeText(applicationContext, "습도 세팅이 변경되었습니다.", Toast.LENGTH_SHORT).show()
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
        println("topic $topic")
        println(message)
    }

    override fun onMqttMessageReceived(topic: String, message: MqttMessage) {
        println("topic $topic")
        println(message)
    }
}