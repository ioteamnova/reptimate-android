//package com.example.iot_teamnova.Cage
//
//import android.app.AlertDialog
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import org.bouncycastle.jce.provider.BouncyCastleProvider
//import org.bouncycastle.util.io.pem.PemReader
//import org.eclipse.paho.android.service.MqttAndroidClient
//import org.eclipse.paho.client.mqttv3.*
//import org.json.JSONObject
//import java.io.BufferedInputStream
//import java.io.InputStream
//import java.io.InputStreamReader
//import java.security.KeyFactory
//import java.security.KeyStore
//import java.security.Security
//import java.security.cert.Certificate
//import java.security.cert.CertificateFactory
//import java.security.cert.X509Certificate
//import java.security.spec.PKCS8EncodedKeySpec
//import javax.net.SocketFactory
//import javax.net.ssl.KeyManagerFactory
//import javax.net.ssl.SSLContext
//import javax.net.ssl.SSLSocketFactory
//import javax.net.ssl.TrustManagerFactory
//
//
//class TestActivity : AppCompatActivity() {
//    private lateinit var textView1: TextView
//    private var brokerUrl :String = "ssl://43.201.185.236:8883"
//    private lateinit var dialog : AlertDialog
//    private lateinit var mqttClient1: MqttAndroidClient
//
//    //    private lateinit var InFo: String;
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_test)
//
//        val myButton1 = findViewById<Button>(R.id.button1)
//        val myButton2 = findViewById<Button>(R.id.button2)
//        val myButton3 = findViewById<Button>(R.id.button3)
//        val myButton4 = findViewById<Button>(R.id.button4)
//        val myButton5 = findViewById<Button>(R.id.button5)
//        val myButton6 = findViewById<Button>(R.id.button6)
//        val myButton7 = findViewById<Button>(R.id.button7)
//        textView1 = findViewById<TextView>(R.id.textView)
//
//        mqttClient1 = MqttAndroidClient(this, brokerUrl, MqttClient.generateClientId())
//        //mqttClient = MqttClient(brokerUrl, MqttClient.generateClientId(), null)
//
//        // Mqtt의 Client가 서버에 연결하는 방법을 제어하는 클래스, MqttConnectOptions 객체 생성
//        val mqttConnectOptions = MqttConnectOptions()
//
//        // MqttConnectOptions의 소켓 팩토리 초기화
//        mqttConnectOptions.socketFactory = mqttSSLAuth()
//        // No subjectAltNames on the certificate match 에러 무시
//        // TODO : 브로커의 IP와 호스트의 IP가 일치하는지를(인증) 무시하는 것 같음 → 다른 해결법 필요?
////        mqttConnectOptions.isHttpsHostnameVerificationEnabled = false
//        // Mqtt 서버와 연결
//        // 연결 결과 콜백 → callbackConnectResult
//        mqttClient1.connect(mqttConnectOptions, null, callbackConnectResult)
//        // Connect to the broker
//
//        myButton1.setOnClickListener {
//            // Handle button click event
//            val message = "{\"userIdx\":\"1\",\"tempname\":\"KR_B1\",\"Commanded_Function\":\"WATERPUMP_ON\",\"type\":\"2\"}"
//            mqttClient1.publish("controlm/getrequest/nest", MqttMessage(message.toByteArray()))
//            println("211/board1/water_pump/control")
//        }
//
//        myButton2.setOnClickListener {
//            val message = "{\"userIdx\":\"1\",\"tempname\":\"KR_B1\",\"Commanded_Function\":\"UVB_ON\",\"type\":\"2\"}"
//            mqttClient1.publish("controlm/getrequest/nest", MqttMessage(message.toByteArray()))
//        }
//
//        myButton3.setOnClickListener {
//            val message ="{\"userIdx\":\"1\",\"tempname\":\"KR_B1\",\"Commanded_Function\":\"UVB_OFF\",\"type\":\"2\"}"
//            mqttClient1.publish("controlm/getrequest/nest", MqttMessage(message.toByteArray()))
//        }
//
//        myButton4.setOnClickListener {
//            val message =  "{\"userIdx\":\"1\",\"tempname\":\"KR_B1\",\"Commanded_Function\":\"Temp_HumidMessage\",\"type\":\"2\"}"
//            mqttClient1.publish("temphumid/getrequest/nest", MqttMessage(message.toByteArray()))
//        }
//
//        myButton5.setOnClickListener {
//            val message = "{\"userIdx\":\"1\",\"tempname\":\"KR_B1\",\"Commanded_Function\":\"COOLINGFAN_ON\",\"type\":\"2\"}"
//            mqttClient1.publish("controlm/getrequest/nest", MqttMessage(message.toByteArray()))
//        }
//        myButton6.setOnClickListener {
//            openMyDialog()
//        }
//        myButton7.setOnClickListener {
//            val message = "{\"userIdx\":\"1\",\"maxTemp\":\"28.1\",\"minTemp\":\"22.3\",\"maxHumid\":\"80.6\",\"minHumid\":\"41.2\",\"tempname\":\"KR_B1\"}"
//            mqttClient1.publish("temphumid/setrequest/nest", MqttMessage(message.toByteArray()))
//        }
//
//    }
//
//    fun openMyDialog() {
//        // Inflate the layout for the dialog
//        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialogview, null)
//        // Create a dialog builder
//        val dialogBuilder = AlertDialog.Builder(this)
//        // Set the dialog view
//        dialogBuilder.setView(dialogView)
//        // Create the dialog
//        dialog = dialogBuilder.create()
//        // Show the dialog
//        dialog.show()
//
//        dialog.setContentView(R.layout.dialogview)
//        val editText = dialog.findViewById<EditText>(R.id.my_text_view)
//        val Button1 = dialog.findViewById<Button>(R.id.my_button)
//        val ApplyBtn = dialog.findViewById<Button>(R.id.ApplyBtn)
//        editText.setText("211/board1/sendTemp_Huid/control")
//
//        Button1.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        ApplyBtn.setOnClickListener {
//            val message = "{\"userIdx\":\"1\", \"petIdx\":\"2\", \"cageName\":\"헬로\", \"light\":\"true\", \"waterpump\":\"true\", \"coolingfan\":\"true\", \"currentTemp\":\"0\", \"maxTemp\":\"30.0\", \"minTemp\":\"22.0\", \"currentHumid\":\"0\", \"maxHumid\":\"80.0\", \"minHumid\":\"40.0\", \"usage\":\"크레스티드 게코\", \"tempname\":\"KR_B1\"}"
//            // val message = "{\"Commanded_Function\":\"User_Register\", \"UserId\":\"AA\",\"Min_Humi\":\"35.0\",\"rq\":\"rq\"}"
//
//            println(message)
//            mqttClient1.publish("setup/request/nest", MqttMessage(message.toByteArray()))
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // Disconnect from the broker
//    }
//
//    private fun mqttSSLAuth(password: String = ""): SocketFactory {
//        // raw에 등록한 인증 파일들을 InputStream 변수로 만듬(파일을 읽기 위한 처리)
//        val caInputStream: InputStream = BufferedInputStream(resources.openRawResource(R.raw.ca))
//        val clientCrtInputStream: InputStream = BufferedInputStream(resources.openRawResource(R.raw.client_crt))
//        val clientKeyInputStream: InputStream = BufferedInputStream(resources.openRawResource(R.raw.client_key))
//
//        // X.509 인증서 타입의 CertificateFactory 객체 생성
//        // → 각 파일들을 CertificateFactory 객체로 초기화 시키는데 사용.
//        val certificateFactory = CertificateFactory.getInstance("X.509")
//
//        // 파일 데이터로 Certificate 객체 생성 및 초기화
//        val caCertificate  = certificateFactory.generateCertificate(caInputStream)
//        val clientCertificate = certificateFactory.generateCertificate(clientCrtInputStream)
//
//        // client key를 전달하여 PemReader 객체 생성
//        // → Private Key로 된 '.key' 파일을 PemReader로 판독
//        val keyPemReader = PemReader(InputStreamReader(clientKeyInputStream))
//
//        // key 파일 내용을 ByteArray로 가져옴
//        val pemContent = keyPemReader.readPemObject().content
//        keyPemReader.close() // PemReader 종료(초기화)
//
//        // PemReader로 구한 ByteArray를 PKCS #8 표준에 따라 인코딩
//        // → PKCS #8는 일반적으로 PEM base64 인코딩 형식으로 변환된다고 함
//        val keySpecPKCS8 = PKCS8EncodedKeySpec(pemContent)
//
//        // 암호화 된 키를 기본 키로 변환하기 위해 KeyFactory 사용
//        // → RSA로 암호화 되어있기 때문에 RSA 알고리즘을 전달하여 KeyFactory 객체 반환
//        val keyFactory = KeyFactory.getInstance("RSA")
//
//        // 인코딩 된 keySpec을 받아서 개인 키를 생성
//        val privateKey = keyFactory.generatePrivate(keySpecPKCS8)
//
//        // KeyStore를 이용하여 ca 인증서가 신뢰할 수 있는 인증서라고 정의
//        // → 초기화와 신뢰할 수 있는 인증서로 정의를 안하면 ssl 인증에 실패
//        val caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
//        caKeyStore.load(null, null) // 초기화
//        caKeyStore.setCertificateEntry("ca-certificate", caCertificate) // 신뢰할 수 있는 인증서로 정의
//
//        // 기본 TrustManagerFactory 알고리즘을 전달하여 TrustManagerFactory 객체 생성
//        // KeyStore를 전달 받아 TrustManagerFactory를 초기화
//        // → SSLContext를 초기화하는데 사용 예정
//        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
//        tmf.init(caKeyStore)
//
//        // KeyStore를 이용하여 client_crt 인증서에 개인 키를 전달하고 신뢰할 수 있는 인증서라고 정의
//        // → 초기화와 신뢰할 수 있는 인증서로 정의를 안하면 ssl 인증에 실패
//        // → setKeyEntry를 이용해 개인 키로 client 인증서를 인증 (암호는 아무 값이나..?)
//        val clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
//        clientKeyStore.load(null, null)
//        clientKeyStore.setCertificateEntry("certificate", clientCertificate)
//        clientKeyStore.setKeyEntry("private-key", privateKey, password.toCharArray(), arrayOf<Certificate>(clientCertificate))
//
//        // 기본 KeyManagerFactory 알고리즘을 전달하여 KeyManagerFactory 객체 생성
//        // KeyStore와 암호를 전달 받아 KeyManagerFactory를 초기화
//        // → SSLContext를 초기화하는데 사용 예정
//        // → 암호는 아무 값이나..?
//        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
//        kmf.init(clientKeyStore, password.toCharArray())
//
//        // MqttConnectOptions에 소켓팩토리를 정보를 주기 위해 SSLContext 객체 생성
//        // KeyManagerFactory, TrustManagerFactory를 넘겨서 초기화
//        val context = SSLContext.getInstance("TLSv1.2")
//        context.init(kmf.keyManagers, tmf.trustManagers, null)
//
//        return context.socketFactory
//    }
//
//
//    /**
//     * connect 결과 처리
//     */
//    private var callbackConnectResult = object : IMqttActionListener {
//        override fun onSuccess(asyncActionToken: IMqttToken?) {
//            println("성공 $asyncActionToken")
//            mqttClient1.subscribe("1/KR_B1/setup/response/app", 0)
//            mqttClient1.subscribe("1/KR_B1/temphumid/setresponse/app", 0)
//            mqttClient1.subscribe("1/KR_B1/temphumid/getresponse/app", 0)
//            mqttClient1.subscribe("1/KR_B1/controlm/getresponse/app", 0)
//
//            mqttCallBack()
//        }
//
//        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
//            println("실패 $exception")
//            reconnect()
//        }
//    }
//
//    private fun reconnect (){
//        mqttClient1 = MqttAndroidClient(applicationContext, brokerUrl, MqttClient.generateClientId())
//        val mqttConnectOptions = MqttConnectOptions()
//        mqttConnectOptions.socketFactory = mqttSSLAuth()
//        mqttClient1.connect(mqttConnectOptions, null, callbackConnectResult)
//    }
//
//
//
//    /**
//     * 메시지 상태 콜백
//     */
//    private fun mqttCallBack() {
//        // 콜백 설정
//        mqttClient1.setCallback(object : MqttCallback {
//            // 연결이 끊겼을 경우
//            override fun connectionLost(p0: Throwable?) {
//                println("연결 끊어짐")
////                mqttClient1 = MqttAndroidClient(applicationContext, brokerUrl, MqttClient.generateClientId())
////                val mqttConnectOptions = MqttConnectOptions()
////                mqttConnectOptions.socketFactory = mqttSSLAuth()
////                mqttClient1.connect(mqttConnectOptions, null, callbackConnectResult)
//
//            }
//
//            // 메세지가 도착했을 때
//            override fun messageArrived(topic: String?, message: MqttMessage?) {
//                println("topic $topic")
//                println("message $message")
//                Toast.makeText(this@TestActivity, "받음", Toast.LENGTH_SHORT).show()
//            }
//
//            // 메시지 전송이 성공했을 때
//            override fun deliveryComplete(p0: IMqttDeliveryToken?) {
//                println("메세지 전송 성공")
//            }
//        })
//    }
//
//}