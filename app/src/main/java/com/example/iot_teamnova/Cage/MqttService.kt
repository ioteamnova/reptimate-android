package com.example.iot_teamnova.Cage

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.iot_teamnova.MainApplication
import com.example.iot_teamnova.R
import org.bouncycastle.util.io.pem.PemReader
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.SocketFactory
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class MqttService : Service() {
    lateinit var mqttManager: MqttManager

    // MQTT 콜백 인터페이스 정의
    interface MqttCallbackListener {
        fun onMqttMessageReceived(topic: String, message: MqttMessage)
    }

    // MqttCallbackListener 변수 선언
    private var mqttCallbackListener: MqttCallbackListener? = null

    inner class MqttBinder : Binder() {
        fun getService(): MqttService {
            return this@MqttService
        }
    }

    // MqttBinder 인스턴스 생성
    private val mqttBinder = MqttBinder()

    override fun onBind(intent: Intent): IBinder {
        return mqttBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MqttService", "MQTT service created")

        // Initialize and configure the MQTT manager
        val brokerUrl = "ssl://43.201.185.236:8883" // Replace with your MQTT broker URL
        val clientId = MqttClient.generateClientId() // Replace with your desired client ID
        mqttManager = MqttManager(applicationContext, brokerUrl, clientId)
        mqttManager.connect()

        mqttManager.setMqttCallbackListener(object : MqttManager.MqttCallbackListener {
            override fun onMqttMessageReceived(topic: String, message: MqttMessage) {
                // 메세지 도착 시 동작할 코드
                println("topic $topic")
                println(message)
                println("서비스에서 실행")
                // 이 코드 아래에 필요한 작업을 추가하세요.
                mqttCallbackListener?.onMqttMessageReceived(topic, message)
            }
        })
    }

    // MqttCallbackListener 설정 메서드
    fun setMqttCallbackListener(listener: MqttCallbackListener) {
        mqttCallbackListener = listener
    }

    override fun onDestroy() {
        Log.d("MqttService", "MQTT service destroyed")

        // Disconnect from the MQTT broker
        mqttManager.disconnect()

        super.onDestroy()
    }

    companion object {
        private var instance: MqttService? = null

        fun getInstance(): MqttService? {
            if(instance == null) {
                instance = MqttService()
            }
            return instance
        }
    }
}