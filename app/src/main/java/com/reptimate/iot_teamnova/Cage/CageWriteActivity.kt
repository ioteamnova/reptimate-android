package com.reptimate.iot_teamnova.Cage

import APIS
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.databinding.ActivityCageWriteBinding
import org.eclipse.paho.client.mqttv3.*
import java.text.SimpleDateFormat
import java.util.*


class CageWriteActivity : AppCompatActivity(), MqttService.MqttCallbackListener, MqttManager.MqttManagerCallback {
    private lateinit var mqttManager: MqttManager
    private lateinit var mqttServiceConnection: MqttServiceConnection
    private lateinit var mqttService: MqttService

    var tem_ok = "1"
    var hum_ok = "1"
    var uvb_ok = "1"

    private val binding by lazy { ActivityCageWriteBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.parentLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            currentFocus?.clearFocus()
        }

        // MqttService와 연결하기 위한 ServiceConnection 객체 생성
        mqttServiceConnection = MqttServiceConnection()

        // MqttService와 바인딩
        val intent = Intent(this, MqttService::class.java)
        bindService(intent, mqttServiceConnection, Context.BIND_AUTO_CREATE)

        // MqttService 인스턴스 가져오기
        mqttService = MqttService.getInstance()!!

        // MqttService에 MqttCallbackListener 등록
        mqttService?.setMqttCallbackListener(this)

        // MqttManager 인스턴스 생성
        mqttManager = MqttManager(applicationContext, "ssl://43.201.185.236:8883", MqttClient.generateClientId())

        // MqttManager에 콜백 설정
        mqttManager.setCallback(this)

        mqttManager.connect()

        binding.temperatureMax.addTextChangedListener(object : TextWatcher {
            //모임 인원수 적는 et의 텍스트가 변경될 때
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            //            @SuppressLint("ResourceType")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //텍스트가 변경될 때마다 함수 호출
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

        binding.humidityMax.addTextChangedListener(object : TextWatcher {
            //모임 인원수 적는 et의 텍스트가 변경될 때
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            //            @SuppressLint("ResourceType")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //텍스트가 변경될 때마다 함수 호출
                if (binding.humidityMax.text.toString().isNotEmpty()) {
                    // 작성되어 있던 값을 지울 때 pareInt(" ") 에서 에러가 나기 때문에 위와 같은 과정 필요
                    if (binding.humidityMax.text.toString().toInt() > 100) {
                        // 100 초과일 때
                        binding.humidityMax.setText("100")
                        //et가 100으로 변경됨 
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
                        // 100 초과일 때
                        binding.humidityMin.setText("100")
                        //et가 100으로 변경됨
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

        binding.uvbSwitch.setOnClickListener {
            if(binding.uvbSwitch.isChecked) {
                binding.uvbLayout.visibility = View.VISIBLE
                binding.uvbOn.isEnabled = true
                binding.uvbOff.isEnabled = true
                uvb_ok = "1"
            }
            else {
                binding.uvbLayout.visibility = View.GONE
                binding.uvbOn.isEnabled = false
                binding.uvbOff.isEnabled = false
                uvb_ok = "0"
            }
        }

        binding.uvbOn.setOnClickListener {
            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                binding.uvbOn.text = SimpleDateFormat("HH:mm").format(cal.time)
            }

            TimePickerDialog(this@CageWriteActivity, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        binding.uvbOff.setOnClickListener {
            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                binding.uvbOff.text = SimpleDateFormat("HH:mm").format(cal.time)
            }

            TimePickerDialog(this@CageWriteActivity, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        binding.confirmBtn.setOnClickListener {
            if(tem_ok == "0" && hum_ok == "0" && uvb_ok == "0"){ // 자동 설정 온도 X, 습도 X, UVB X
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"cageName\":\"${binding.name.text.toString()}\", \"autoChkLight\":\"$uvb_ok\", \"autoChkTemp\":\"$tem_ok\", \"autoChkHumid\":\"$hum_ok\", \"maxTemp\":\"0\", \"minTemp\":\"0\", \"maxHumid\":\"0\", \"minHumid\":\"0\", \"usage\":\"사육장\", \"autoLightUtctimeOn\":\"00:00\", \"autoLightUtctimeOff\":\"00:00\"}"

                mqttServiceConnection.getService()?.mqttManager?.publish("setup/request/nest", message)
                finish()
            }
            if(tem_ok == "1" && hum_ok == "0" && uvb_ok == "0"){ // 자동 설정 온도 O, 습도 X, UVB X
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"cageName\":\"${binding.name.text.toString()}\", \"autoChkLight\":\"$uvb_ok\", \"autoChkTemp\":\"$tem_ok\", \"autoChkHumid\":\"$hum_ok\", \"maxTemp\":\"${binding.temperatureMax.text.toString()}\", \"minTemp\":\"${binding.temperatureMin.text.toString()}\", \"maxHumid\":\"0\", \"minHumid\":\"0\", \"usage\":\"사육장\", \"autoLightUtctimeOn\":\"00:00\", \"autoLightUtctimeOff\":\"00:00\"}"

                mqttServiceConnection.getService()?.mqttManager?.publish("setup/request/nest", message)
                finish()
            }
            if(tem_ok == "0" && hum_ok == "1" && uvb_ok == "0"){ // 자동 설정 온도 X, 습도 O, UVB X
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"cageName\":\"${binding.name.text.toString()}\", \"autoChkLight\":\"$uvb_ok\", \"autoChkTemp\":\"$tem_ok\", \"autoChkHumid\":\"$hum_ok\", \"maxTemp\":\"0\", \"minTemp\":\"0\", \"maxHumid\":\"${binding.humidityMax.text.toString()}\", \"minHumid\":\"${binding.humidityMin.text.toString()}\", \"usage\":\"사육장\", \"autoLightUtctimeOn\":\"00:00\", \"autoLightUtctimeOff\":\"00:00\"}"

                mqttServiceConnection.getService()?.mqttManager?.publish("setup/request/nest", message)
                finish()
            }
            if(tem_ok == "0" && hum_ok == "0" && uvb_ok == "1"){ // 자동 설정 온도 X, 습도 X, UVB O
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"cageName\":\"${binding.name.text.toString()}\", \"autoChkLight\":\"$uvb_ok\", \"autoChkTemp\":\"$tem_ok\", \"autoChkHumid\":\"$hum_ok\", \"maxTemp\":\"0\", \"minTemp\":\"0\", \"maxHumid\":\"0\", \"minHumid\":\"0\", \"usage\":\"사육장\", \"autoLightUtctimeOn\":\"${Local_To_UTC(binding.uvbOn.text.toString())}\", \"autoLightUtctimeOff\":\"${Local_To_UTC(binding.uvbOff.text.toString())}\"}"

                mqttServiceConnection.getService()?.mqttManager?.publish("setup/request/nest", message)
                finish()
            }
            if(tem_ok == "1" && hum_ok == "1" && uvb_ok == "0"){ // 자동 설정 온도 O, 습도 O, UVB X
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"cageName\":\"${binding.name.text.toString()}\", \"autoChkLight\":\"$uvb_ok\", \"autoChkTemp\":\"$tem_ok\", \"autoChkHumid\":\"$hum_ok\", \"maxTemp\":\"${binding.temperatureMax.text.toString()}\", \"minTemp\":\"${binding.temperatureMin.text.toString()}\", \"maxHumid\":\"${binding.humidityMax.text.toString()}\", \"minHumid\":\"${binding.humidityMin.text.toString()}\", \"usage\":\"사육장\", \"autoLightUtctimeOn\":\"00:00\", \"autoLightUtctimeOff\":\"00:00\"}"

                mqttServiceConnection.getService()?.mqttManager?.publish("setup/request/nest", message)
                finish()
            }
            if(tem_ok == "1" && hum_ok == "0" && uvb_ok == "1"){ // 자동 설정 온도 O, 습도 X, UVB O
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"cageName\":\"${binding.name.text.toString()}\", \"autoChkLight\":\"$uvb_ok\", \"autoChkTemp\":\"$tem_ok\", \"autoChkHumid\":\"$hum_ok\", \"maxTemp\":\"${binding.temperatureMax.text.toString()}\", \"minTemp\":\"${binding.temperatureMin.text.toString()}\", \"maxHumid\":\"0\", \"minHumid\":\"0\", \"usage\":\"사육장\", \"autoLightUtctimeOn\":\"${Local_To_UTC(binding.uvbOn.text.toString())}\", \"autoLightUtctimeOff\":\"${Local_To_UTC(binding.uvbOff.text.toString())}\"}"

                mqttServiceConnection.getService()?.mqttManager?.publish("setup/request/nest", message)
                finish()
            }
            if(tem_ok == "0" && hum_ok == "1" && uvb_ok == "1"){ // 자동 설정 온도 X, 습도 O, UVB O
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"cageName\":\"${binding.name.text.toString()}\", \"autoChkLight\":\"$uvb_ok\", \"autoChkTemp\":\"$tem_ok\", \"autoChkHumid\":\"$hum_ok\", \"maxTemp\":\"0\", \"minTemp\":\"0\", \"maxHumid\":\"${binding.humidityMax.text.toString()}\", \"minHumid\":\"${binding.humidityMin.text.toString()}\", \"usage\":\"사육장\", \"autoLightUtctimeOn\":\"${Local_To_UTC(binding.uvbOn.text.toString())}\", \"autoLightUtctimeOff\":\"${Local_To_UTC(binding.uvbOff.text.toString())}\"}"

                mqttServiceConnection.getService()?.mqttManager?.publish("setup/request/nest", message)
                finish()
            }
            if(tem_ok == "1" && hum_ok == "1" && uvb_ok == "1") { // 자동 설정 온도 O, 습도 O, UVB O
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"cageName\":\"${binding.name.text.toString()}\", \"autoChkLight\":\"$uvb_ok\", \"autoChkTemp\":\"$tem_ok\", \"autoChkHumid\":\"$hum_ok\", \"maxTemp\":\"${binding.temperatureMax.text.toString()}\", \"minTemp\":\"${binding.temperatureMin.text.toString()}\", \"maxHumid\":\"${binding.humidityMax.text.toString()}\", \"minHumid\":\"${binding.humidityMin.text.toString()}\", \"usage\":\"사육장\", \"autoLightUtctimeOn\":\"${Local_To_UTC(binding.uvbOn.text.toString())}\", \"autoLightUtctimeOff\":\"${Local_To_UTC(binding.uvbOff.text.toString())}\"}"
//                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"cageName\":\"${binding.name.text.toString()}\", \"maxTemp\":\"${binding.temperatureMax.text.toString()}\", \"minTemp\":\"${binding.temperatureMin.text.toString()}\", \"maxHumid\":\"${binding.humidityMax.text.toString()}\", \"minHumid\":\"${binding.humidityMin.text.toString()}\"}"

                println(message)
                mqttServiceConnection.getService()?.mqttManager?.publish("setup/request/nest", message)
//                mqttClient1.publish("temphumid/setrequest/nest", MqttMessage(message.toByteArray()))
                finish()
            }
        }

    }

    @Throws(Exception::class)
    fun Local_To_UTC(localTime: String?): String? {
        var utcTime: String? = null
        val sdf = SimpleDateFormat("HH:mm")
        val tz = TimeZone.getDefault()
        try {
            val parseDate = sdf.parse(localTime)
            val milliseconds = parseDate.time
            val offset = tz.getOffset(milliseconds)
            utcTime = sdf.format(milliseconds - offset)
            utcTime = utcTime.replace("+0000", "")
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception(e)
        }
        println("협정시: $utcTime")
        return utcTime
    }

    fun getLocalDate(UTC: String?): String? {
        var locTime: String? = null
        val tz = TimeZone.getDefault()
        val sdf = SimpleDateFormat("HH:mm")
        try {
            val parseDate = sdf.parse(UTC)
            val milliseconds = parseDate.time
            val offset = tz.getOffset(milliseconds)
            locTime = sdf.format(milliseconds + offset)
            locTime = locTime.replace("+0000", "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return locTime
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mqttServiceConnection)
    }

    override fun onMessageReceived(topic: String, message: String) {
        // 메시지 도착 시 UI 업데이트 수행
        println("topic $topic")
        println(message)
        println("케이지 등록 페이지")
    }

    override fun onMqttMessageReceived(topic: String, message: MqttMessage) {
        // 메시지 도착 시 UI 업데이트 수행
        println("topic $topic")
        println(message)
        println("케이지 등록 페이지")
    }
}