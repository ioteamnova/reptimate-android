package com.reptimate.iot_teamnova.Cage

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.databinding.DialogUvbEditBinding
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.text.SimpleDateFormat
import java.util.*

class EditUVBDialog : Activity(), MqttService.MqttCallbackListener, MqttManager.MqttManagerCallback{
    private lateinit var mqttManager: MqttManager
    private lateinit var mqttServiceConnection: MqttServiceConnection
    private lateinit var mqttService: MqttService

    var uvb_ok = "1"

    private val binding by lazy { DialogUvbEditBinding.inflate(layoutInflater) }
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
        val autoChkLight = intent.getStringExtra("autoChkLight")
        val autoLightUtctimeOn = intent.getStringExtra("autoLightUtctimeOn")
        val autoLightUtctimeOff = intent.getStringExtra("autoLightUtctimeOff")

        binding.uvbOn.setText(autoLightUtctimeOn)
        binding.uvbOff.setText(autoLightUtctimeOff)

        if(autoChkLight == "1"){
            binding.uvbSwitch.isChecked = true
            binding.uvbLayout.visibility = View.VISIBLE
            uvb_ok = "1"
        }
        if(autoChkLight == "0") {
            binding.uvbSwitch.isChecked = false
            binding.uvbLayout.visibility = View.GONE
            uvb_ok = "0"
        }
        binding.uvbOn.setOnClickListener {
            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                binding.uvbOn.text = SimpleDateFormat("HH:mm").format(cal.time)
            }

            TimePickerDialog(this@EditUVBDialog, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(
                Calendar.MINUTE), true).show()
        }

        binding.uvbOff.setOnClickListener {
            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                binding.uvbOff.text = SimpleDateFormat("HH:mm").format(cal.time)
            }

            TimePickerDialog(this@EditUVBDialog, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(
                Calendar.MINUTE), true).show()
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

        binding.yesBtn.setOnClickListener {
            if(uvb_ok == "1"){
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"autoChkLight\":\"${uvb_ok}\", \"autoLightUtctimeOn\":\"${Local_To_UTC(binding.uvbOn.text.toString())}\", \"autoLightUtctimeOff\":\"${Local_To_UTC(binding.uvbOff.text.toString())}\"}"

                println(message)
                mqttServiceConnection.getService()?.mqttManager?.publish("temphumid/setrequest/nest", message)
            }
            if(uvb_ok == "0"){
                val message =
                    "{\"userIdx\":\"${MainApplication.prefs.getidx}\", \"boardTempname\":\"KR_B1\", \"autoChkLight\":\"${uvb_ok}\", \"autoLightUtctimeOn\":\"0\", \"autoLightUtctimeOff\":\"0\"}"

                println(message)
                mqttServiceConnection.getService()?.mqttManager?.publish("temphumid/setrequest/nest", message)
            }
            Toast.makeText(applicationContext, "UVB 램프 세팅이 변경되었습니다.", Toast.LENGTH_SHORT).show()
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
}
