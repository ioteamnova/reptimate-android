package com.reptimate.iot_teamnova.Cage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.databinding.ActivityCageViewBinding
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class CageViewActivity : AppCompatActivity(), MqttService.MqttCallbackListener, MqttManager.MqttManagerCallback {
    private lateinit var mqttManager: MqttManager
    private lateinit var mqttServiceConnection: MqttServiceConnection
    private lateinit var mqttService: MqttService

    var is_uvb_on = "false"
    var is_heat_on = "false"

    private val binding by lazy { ActivityCageViewBinding.inflate(layoutInflater) }
    private val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // MqttService와 연결하기 위한 ServiceConnection 객체 생성
        mqttServiceConnection = MqttServiceConnection()

        // MqttService와 바인딩
        val serviceIntent = Intent(this, MqttService::class.java)
        bindService(serviceIntent, mqttServiceConnection, Context.BIND_AUTO_CREATE)

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
        val idx = intent.getStringExtra("idx")
        val cageName = intent.getStringExtra("cageName")
        val boardTempname = intent.getStringExtra("boardTempname")
        val currentUvbLight = intent.getStringExtra("currentUvbLight")
        val currentHeatingLight = intent.getStringExtra("currentHeatingLight")
        val autoChkLight = intent.getStringExtra("autoChkLight")
        val autoChkTemp = intent.getStringExtra("autoChkTemp")
        val autoChkHumid = intent.getStringExtra("autoChkHumid")
        val currentTemp = intent.getStringExtra("currentTemp")
        val currentTemp2 = intent.getStringExtra("currentTemp2")
        val maxTemp = intent.getStringExtra("maxTemp")
        val minTemp = intent.getStringExtra("minTemp")
        val currentHumid = intent.getStringExtra("currentHumid")
        val currentHumid2 = intent.getStringExtra("currentHumid2")
        val maxHumid = intent.getStringExtra("maxHumid")
        val minHumid = intent.getStringExtra("minHumid")
        var autoLightUtctimeOn = intent.getStringExtra("autoLightUtctimeOn")
        var autoLightUtctimeOff = intent.getStringExtra("autoLightUtctimeOff")

        if(autoChkTemp == "1") {
            binding.temperatureHigh.text = maxTemp + "º"
            binding.temperatureRow.text = minTemp + "º"
        }
        if(autoChkTemp == "0") {
            binding.temperatureHigh.text = "º"
            binding.temperatureRow.text = "º"
        }
        if(autoChkHumid == "1") {
            binding.humidityHigh.text = maxHumid + "%"
            binding.humidityRow.text = minHumid + "%"
        }
        if(autoChkHumid == "0") {
            binding.humidityHigh.text = "%"
            binding.humidityRow.text = "%"
        }
        if(autoChkLight == "1") {
            autoLightUtctimeOn = getLocalDate(autoLightUtctimeOn)
            autoLightUtctimeOff = getLocalDate(autoLightUtctimeOff)
            binding.uvbOnTime.text = autoLightUtctimeOn
            binding.uvbOffTime.text = autoLightUtctimeOff
        }
        if(autoChkLight == "0") {
            binding.uvbOnTime.text = ""
            binding.uvbOffTime.text = ""
        }

        binding.cageName.text = cageName
        binding.temperature1.text = currentTemp + "º"
        binding.temperature2.text = currentTemp2 + "º"
        binding.humidity1.text = currentHumid + "%"
        binding.humidity2.text = currentHumid2 + "%"

        if(currentUvbLight == "1") {
            binding.UVB.text = "ON"
        }
        if(currentUvbLight == "0") {
            binding.UVB.text = "OFF"
        }

        if(currentHeatingLight == "1") {
            binding.heatingLamp.text = "ON"
        }
        if(currentHeatingLight == "0") {
            binding.heatingLamp.text = "OFF"
        }

        binding.temperatureSetting.setOnClickListener {
            val intent = Intent(applicationContext, EditTempDialog::class.java)
            intent.putExtra("boardTempname", boardTempname)
            intent.putExtra("maxTemp", maxTemp)
            intent.putExtra("minTemp", minTemp)
            intent.putExtra("autoChkTemp", autoChkTemp)
            intent.putExtra("autoChkHumid", autoChkHumid)
            intent.putExtra("autoChkLight", autoChkLight)
            startActivity(intent)
        }

        binding.humiditySetting.setOnClickListener{
            val intent = Intent(applicationContext, EditHumidDialog::class.java)
            intent.putExtra("boardTempname", boardTempname)
            intent.putExtra("maxHumid", maxHumid)
            intent.putExtra("minHumid", minHumid)
            intent.putExtra("autoChkTemp", autoChkTemp)
            intent.putExtra("autoChkHumid", autoChkHumid)
            intent.putExtra("autoChkLight", autoChkLight)
            startActivity(intent)
        }

        binding.uvbSetting.setOnClickListener {
            val intent = Intent(applicationContext, EditUVBDialog::class.java)
            intent.putExtra("boardTempname", boardTempname)
            intent.putExtra("autoLightUtctimeOn", autoLightUtctimeOn)
            intent.putExtra("autoLightUtctimeOff", autoLightUtctimeOff)
            intent.putExtra("autoChkTemp", autoChkTemp)
            intent.putExtra("autoChkHumid", autoChkHumid)
            intent.putExtra("autoChkLight", autoChkLight)
            startActivity(intent)
        }

        binding.refreshBtn.setOnClickListener {
            // 온습도 값 수동 가져오기
//            val message = "{\"userIdx\":\"30\",\"boardTempname\":\"KR_B1\",\"type\":\"2\"}"
//            mqttServiceConnection.getService()?.mqttManager?.publish("temphumid/getrequest/nest", message)
            //재등록 임시 테스트 코드
            val message = "{\"boardIdx\":\"${idx}\",\"userIdx\":\"${MainApplication.prefs.getidx}\",\"boardTempname\":\"KR_B1\"}"
            mqttServiceConnection.getService()?.mqttManager?.publish("resetup/request/nest", message)
        }

        binding.temperatureOnBtn.setOnClickListener{
            if(is_heat_on == "false") {
                // 히팅램프 켜기
                val message =
                    "{\"userIdx\":\"30\",\"boardTempname\":\"KR_B1\",\"Commanded_Function\":\"HEAT_ON\",\"type\":\"2\"}"
                mqttServiceConnection.getService()?.mqttManager?.publish("controlm/getrequest/nest", message)
                is_heat_on = "true"
                Toast.makeText(applicationContext, "히팅램프가 켜졌습니다.", Toast.LENGTH_SHORT).show()
                binding.temperatureOnBtn.text = "작동 중지"
            }
            else if(is_heat_on == "true") {
                // 히팅램프 끄기
                val message =
                    "{\"userIdx\":\"30\",\"boardTempname\":\"KR_B1\",\"Commanded_Function\":\"HEAT_OFF\",\"type\":\"2\"}"
                mqttServiceConnection.getService()?.mqttManager?.publish("controlm/getrequest/nest", message)
                is_heat_on = "false"
                Toast.makeText(applicationContext, "히팅램프가 꺼졌습니다.", Toast.LENGTH_SHORT).show()
                binding.temperatureOnBtn.text = "작동하기"
            }
        }

        binding.humidityOnBtn.setOnClickListener {
            // 워터펌프 켜기
            val message = "{\"userIdx\":\"30\",\"boardTempname\":\"KR_B1\",\"Commanded_Function\":\"WATERPUMP_ON\",\"type\":\"2\"}"
            mqttServiceConnection.getService()?.mqttManager?.publish("controlm/getrequest/nest", message)
        }

        binding.uvbOnBtn.setOnClickListener {
            if(is_uvb_on == "false") {
                // uvb램프 켜기
                val message = "{\"userIdx\":\"30\",\"boardTempname\":\"KR_B1\",\"Commanded_Function\":\"UVB_ON\",\"type\":\"2\"}"
                mqttServiceConnection.getService()?.mqttManager?.publish("controlm/getrequest/nest", message)
                is_uvb_on = "true"
                Toast.makeText(applicationContext, "UVB램프가 켜졌습니다.", Toast.LENGTH_SHORT).show()
                binding.uvbOnBtn.text = "작동 중지"
            }
            else if(is_uvb_on == "true") {
                // uvb램프 끄기
                val message = "{\"userIdx\":\"30\",\"boardTempname\":\"KR_B1\",\"Commanded_Function\":\"UVB_OFF\",\"type\":\"2\"}"
                mqttServiceConnection.getService()?.mqttManager?.publish("controlm/getrequest/nest", message)
                is_uvb_on = "false"
                Toast.makeText(applicationContext, "UVB램프가 꺼졌습니다.", Toast.LENGTH_SHORT).show()
                binding.uvbOnBtn.text = "작동하기"
            }
        }

        binding.fanOnBtn.setOnClickListener {
            // 환기팬 켜기
            val message = "{\"userIdx\":\"30\",\"boardTempname\":\"KR_B1\",\"Commanded_Function\":\"COOLINGFAN_ON\",\"type\":\"2\"}"
            mqttServiceConnection.getService()?.mqttManager?.publish("controlm/getrequest/nest", message)
        }

        binding.temperatureStatistics.setOnClickListener {
            val intent = Intent(applicationContext, StatisticsActivity::class.java)
            intent.putExtra("title", "온도 통계")
            startActivity(intent)
        }

        binding.humidityStatistics.setOnClickListener {
            val intent = Intent(applicationContext, StatisticsActivity::class.java)
            intent.putExtra("title", "습도 통계")
            startActivity(intent)
        }

        binding.uvbStatistics.setOnClickListener {
            val intent = Intent(applicationContext, StatisticsActivity::class.java)
            intent.putExtra("title", "UVB 통계")
            startActivity(intent)
        }

        binding.fanStatistics.setOnClickListener {
            val intent = Intent(applicationContext, StatisticsActivity::class.java)
            intent.putExtra("title", "환기팬 통계")
            startActivity(intent)
        }

    }

    override fun onMessageReceived(topic: String, message: String) {
        println("topic $topic")
        println(message)
        if(topic == "30/KR_B1/temphumid/getresponse/app") {
            val messageString = message.toString() // Convert the payload to a string
            val jsonObject = JSONObject(messageString) // Convert the string to a JSONObject
            val currentTemp = jsonObject.getString("currentTemp")
            val currentHumid = jsonObject.getString("currentHumid")
            val currentTemp2 = jsonObject.getString("currentTemp2")
            val currentHumid2 = jsonObject.getString("currentHumid2")
            println(currentTemp)
            println(currentHumid)
            println(currentTemp2)
            println(currentHumid2)
            runOnUiThread {
                binding.temperature1.text = currentTemp + "º"
                binding.temperature2.text = currentTemp2 + "º"
                binding.humidity1.text = currentHumid + "%"
                binding.humidity2.text = currentHumid2 + "%"
            }
        }
    }

    override fun onMqttMessageReceived(topic: String, message: MqttMessage) {
        println("topic $topic")
        println(message)
        if(topic == "30/KR_B1/temphumid/getresponse/app") {
            val messageString = message.toString() // Convert the payload to a string
            val jsonObject = JSONObject(messageString) // Convert the string to a JSONObject
            val currentTemp = jsonObject.getString("currentTemp")
            val currentHumid = jsonObject.getString("currentHumid")
            val currentTemp2 = jsonObject.getString("currentTemp2")
            val currentHumid2 = jsonObject.getString("currentHumid2")
            println(currentTemp)
            println(currentHumid)
            println(currentTemp2)
            println(currentHumid2)
            runOnUiThread {
                binding.temperature1.text = currentTemp + "º"
                binding.temperature2.text = currentTemp2 + "º"
                binding.humidity1.text = currentHumid + "%"
                binding.humidity2.text = currentHumid2 + "%"
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

}