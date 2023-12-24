package com.reptimate.iot_teamnova.Cage

import android.content.Context
import com.reptimate.iot_teamnova.R
import org.bouncycastle.util.io.pem.PemReader
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
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

object MQTTService1 {
    private var mqttClient: MqttAndroidClient? = null

    fun initialize(context: Context, brokerAddress: String, clientId: String) {
        mqttClient = MqttAndroidClient(context, brokerAddress, clientId)
        // MQTT 클라이언트 초기화 및 연결 설정

        // 연결 설정이 완료된 후에 publish 또는 subscribe를 호출하려면 다음과 같이 합니다:
        mqttClient?.let {
            val options = MqttConnectOptions()
            // 연결 옵션 설정
            options.socketFactory = mqttSSLAuth(context)

            try {
                val token = it.connect(options)
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        // 연결 성공 처리 작업
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        // 연결 실패 처리 작업
                    }
                }
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    fun subscribe(topic: String, callback: (String) -> Unit) {
        mqttClient?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                // 연결 완료 후 작업 수행
            }

            override fun connectionLost(cause: Throwable) {
                // 연결 끊김 처리 작업
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                callback(message.toString())
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                // 메시지 전송 완료 처리 작업
            }
        })

        mqttClient?.subscribe(topic, 0)
    }

    fun publish(topic: String, message: String) {
        mqttClient?.let {
            val mqttMessage = MqttMessage()
            mqttMessage.payload = message.toByteArray()

            try {
                it.publish(topic, mqttMessage)
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    private fun mqttSSLAuth(context: Context, password: String = ""): SocketFactory {
        // raw에 등록한 인증 파일들을 InputStream 변수로 만듬(파일을 읽기 위한 처리)
        val caInputStream: InputStream = BufferedInputStream(context.resources.openRawResource(R.raw.ca))
        val clientCrtInputStream: InputStream = BufferedInputStream(context.resources.openRawResource(
            R.raw.client_crt))
        val clientKeyInputStream: InputStream = BufferedInputStream(context.resources.openRawResource(
            R.raw.client_key))

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