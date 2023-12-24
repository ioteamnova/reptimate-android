package com.reptimate.iot_teamnova.Cage

import android.content.Context
import android.util.Log
import com.reptimate.iot_teamnova.MainApplication
import com.reptimate.iot_teamnova.NotificationHelper
import com.reptimate.iot_teamnova.R
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

class MqttManager(private val context: Context, private val brokerUrl: String, private val clientId: String) {
    private lateinit var mqttClient: MqttAndroidClient
    private var mqttCallbackListener: MqttCallbackListener? = null
    private var callback: MqttManagerCallback? = null

    private val notificationHelper: NotificationHelper = NotificationHelper(context) // 노티피케이션 헬퍼

    interface MqttManagerCallback {
        fun onMessageReceived(topic: String, message: String)
        // 필요한 경우 다른 UI 업데이트 메서드를 추가할 수 있습니다.
    }

    interface MqttCallbackListener {
        fun onMqttMessageReceived(topic: String, message: MqttMessage)
    }

    fun setCallback(callback: MqttManagerCallback) {
        this.callback = callback
    }

    fun connect() {
        Log.d("connect : ", "연결 시작")
        mqttClient = MqttAndroidClient(context, brokerUrl, clientId)
        //mqttClient = MqttClient(brokerUrl, MqttClient.generateClientId(), null)

        // Mqtt의 Client가 서버에 연결하는 방법을 제어하는 클래스, MqttConnectOptions 객체 생성
        val mqttConnectOptions = MqttConnectOptions()

        // MqttConnectOptions의 소켓 팩토리 초기화
        mqttConnectOptions.socketFactory = mqttSSLAuth()
        // No subjectAltNames on the certificate match 에러 무시
        // TODO : 브로커의 IP와 호스트의 IP가 일치하는지를(인증) 무시하는 것 같음 → 다른 해결법 필요?
//        mqttConnectOptions.isHttpsHostnameVerificationEnabled = false
        // Mqtt 서버와 연결
        // 연결 결과 콜백 → callbackConnectResult
        mqttClient.connect(mqttConnectOptions, null, callbackConnectResult)
        Log.d("connect : ", "연결 완료")


    }

    private var callbackConnectResult = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            mqttClient.subscribe("30/KR_B1/setup/response/app", 1)
            mqttClient.subscribe("${MainApplication.prefs.getidx}/KR_B1/temphumid/setresponse/app", 1)
            mqttClient.subscribe("${MainApplication.prefs.getidx}/KR_B1/temphumid/getresponse/app", 1)
            mqttClient.subscribe("${MainApplication.prefs.getidx}/KR_B1/controlm/getresponse/app", 1)
            mqttClient.subscribe("${MainApplication.prefs.getidx}/KR_B1/emergency/getresponse/app", 1)
            mqttClient.subscribe("${MainApplication.prefs.getidx}/KR_B1/boardInfo/getresponse/app", 1)

            mqttCallBack()
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            reconnect()
        }
    }

    private fun reconnect (){
        mqttClient = MqttAndroidClient(context, brokerUrl, MqttClient.generateClientId())
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.socketFactory = mqttSSLAuth()
        mqttClient?.connect(mqttConnectOptions, null, callbackConnectResult)
    }

    /**
     * 메시지 상태 콜백
     */
    private fun mqttCallBack() {
        // 콜백 설정
        mqttClient.setCallback(object : MqttCallback {
            // 연결이 끊겼을 경우
            override fun connectionLost(p0: Throwable?) {
//                mqttClient1 = MqttAndroidClient(applicationContext, brokerUrl, MqttClient.generateClientId())
//                val mqttConnectOptions = MqttConnectOptions()
//                mqttConnectOptions.socketFactory = mqttSSLAuth()
//                mqttClient1.connect(mqttConnectOptions, null, callbackConnectResult)

            }

            // 메세지가 도착했을 때
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {

                if(topic == "${MainApplication.prefs.getidx}/KR_B1/emergency/getresponse/app") {
                    val messageString = mqttMessage.toString() // Convert the payload to a string
                    val jsonObject = JSONObject(messageString) // Convert the string to a JSONObject
                    val cageName = jsonObject.getString("cageName")
                    val boardIdx = jsonObject.getString("boardIdx")
                    val module = jsonObject.getString("module")
                    val limit = jsonObject.getString("limit")
                    if(limit == "MAX_TEMP") {
                        val title = "$cageName 케이지의 온도 센서$module 에서 문제 발생!!"
                        val message = "온도가 너무 높습니다. 케이지 상태를 확인해주세요."

                        notificationHelper.showNotification(title, message, boardIdx)
                    }
                    if(limit == "MIN_TEMP") {
                        val title = "$cageName 케이지의 온도 센서$module 에서 문제 발생!!"
                        val message = "온도가 너무 낮습니다. 케이지 상태를 확인해주세요."

                        notificationHelper.showNotification(title, message, boardIdx)
                    }
                    if(limit == "MAX_HUMID") {
                        val title = "$cageName 케이지의 습도 센서$module 에서 문제 발생!!"
                        val message = "습도가 너무 높습니다. 케이지 상태를 확인해주세요."

                        notificationHelper.showNotification(title, message, boardIdx)
                    }
                    if(limit == "MIN_HUMID") {
                        val title = "$cageName 케이지의 습도 센서$module 에서 문제 발생!!"
                        val message = "습도가 너무 낮습니다. 케이지 상태를 확인해주세요."

                        notificationHelper.showNotification(title, message, boardIdx)
                    }
                    
                }

                // 메시지 도착 시 콜백 호출
                callback?.onMessageReceived(topic, mqttMessage.toString())
            }

            // 메시지 전송이 성공했을 때
            override fun deliveryComplete(p0: IMqttDeliveryToken?) {
            }
        })
    }

    fun setMqttCallbackListener(listener: MqttCallbackListener) {
        mqttCallbackListener = listener
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
        } catch (ex: MqttException) {
            ex.printStackTrace()
            // Handle any exceptions that occurred during disconnection
        }
    }

    fun subscribe(topic: String, qos: Int = 0) {
        try {
            mqttClient?.subscribe(topic, qos)
        } catch (ex: MqttException) {
            ex.printStackTrace()
            // Handle any exceptions that occurred during subscription
        }
    }

    fun unsubscribe(topic: String) {
        try {
            mqttClient.unsubscribe(topic)
        } catch (ex: MqttException) {
            ex.printStackTrace()
            // Handle any exceptions that occurred during unsubscription
        }
    }

    fun publish(topic: String, message: String, qos: Int = 1, retained: Boolean = false) {
        try {
            mqttClient.publish(topic, MqttMessage(message.toByteArray()))
//            print("메세지 전송 완료")
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    private fun mqttSSLAuth(password: String = ""): SocketFactory {
        // raw에 등록한 인증 파일들을 InputStream 변수로 만듬(파일을 읽기 위한 처리)
        val caInputStream: InputStream = BufferedInputStream(context.resources.openRawResource(R.raw.ca))
        val clientCrtInputStream: InputStream = BufferedInputStream(context.resources.openRawResource(R.raw.client_crt))
        val clientKeyInputStream: InputStream = BufferedInputStream(context.resources.openRawResource(R.raw.client_key))

        // X.509 인증서 타입의 CertificateFactory 객체 생성
        // → 각 파일들을 CertificateFactory 객체로 초기화 시키는데 사용.
        val certificateFactory = CertificateFactory.getInstance("X.509")

        // 파일 데이터로 Certificate 객체 생성 및 초기화
        val caCertificate  = certificateFactory.generateCertificate(caInputStream)
        val clientCertificate = certificateFactory.generateCertificate(clientCrtInputStream)

        // client key를 전달하여 PemReader 객체 생성
        // → Private Key로 된 '.key' 파일을 PemReader로 판독
        val keyPemReader = PemReader(InputStreamReader(clientKeyInputStream))

        // key 파일 내용을 ByteArray로 가져옴
        val pemContent = keyPemReader.readPemObject().content
        keyPemReader.close() // PemReader 종료(초기화)

        // PemReader로 구한 ByteArray를 PKCS #8 표준에 따라 인코딩
        // → PKCS #8는 일반적으로 PEM base64 인코딩 형식으로 변환된다고 함
        val keySpecPKCS8 = PKCS8EncodedKeySpec(pemContent)

        // 암호화 된 키를 기본 키로 변환하기 위해 KeyFactory 사용
        // → RSA로 암호화 되어있기 때문에 RSA 알고리즘을 전달하여 KeyFactory 객체 반환
        val keyFactory = KeyFactory.getInstance("RSA")

        // 인코딩 된 keySpec을 받아서 개인 키를 생성
        val privateKey = keyFactory.generatePrivate(keySpecPKCS8)

        // KeyStore를 이용하여 ca 인증서가 신뢰할 수 있는 인증서라고 정의
        // → 초기화와 신뢰할 수 있는 인증서로 정의를 안하면 ssl 인증에 실패
        val caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        caKeyStore.load(null, null) // 초기화
        caKeyStore.setCertificateEntry("ca-certificate", caCertificate) // 신뢰할 수 있는 인증서로 정의

        // 기본 TrustManagerFactory 알고리즘을 전달하여 TrustManagerFactory 객체 생성
        // KeyStore를 전달 받아 TrustManagerFactory를 초기화
        // → SSLContext를 초기화하는데 사용 예정
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(caKeyStore)

        // KeyStore를 이용하여 client_crt 인증서에 개인 키를 전달하고 신뢰할 수 있는 인증서라고 정의
        // → 초기화와 신뢰할 수 있는 인증서로 정의를 안하면 ssl 인증에 실패
        // → setKeyEntry를 이용해 개인 키로 client 인증서를 인증 (암호는 아무 값이나..?)
        val clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        clientKeyStore.load(null, null)
        clientKeyStore.setCertificateEntry("certificate", clientCertificate)
        clientKeyStore.setKeyEntry("private-key", privateKey, password.toCharArray(), arrayOf<Certificate>(clientCertificate))

        // 기본 KeyManagerFactory 알고리즘을 전달하여 KeyManagerFactory 객체 생성
        // KeyStore와 암호를 전달 받아 KeyManagerFactory를 초기화
        // → SSLContext를 초기화하는데 사용 예정
        // → 암호는 아무 값이나..?
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(clientKeyStore, password.toCharArray())

        // MqttConnectOptions에 소켓팩토리를 정보를 주기 위해 SSLContext 객체 생성
        // KeyManagerFactory, TrustManagerFactory를 넘겨서 초기화
        val context = SSLContext.getInstance("TLSv1.2")
        context.init(kmf.keyManagers, tmf.trustManagers, null)

        return context.socketFactory
    }
}