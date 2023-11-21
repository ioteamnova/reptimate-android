package com.example.iot_teamnova.Cage

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class MqttServiceConnection : ServiceConnection {
    private var mqttService: MqttService? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mqttService = (service as? MqttService.MqttBinder)?.getService()
        // 서비스와 연결된 후 필요한 작업을 수행할 수 있습니다.
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        mqttService = null
        // 서비스와 연결이 끊겼을 때 필요한 작업을 수행할 수 있습니다.
    }

    fun getService(): MqttService? {
        return mqttService
    }
}