package com.example.iot_teamnova

import android.app.Application
import android.content.Context
import com.example.iot_teamnova.Cage.MqttService
import com.kakao.sdk.common.KakaoSdk

class MainApplication : Application() {
    val mqttService: MqttService by lazy { MqttService() }

    init {
        instance = this
    }

    companion object {
        lateinit var prefs: PreferenceUtil
        var instance: MainApplication? = null
        fun context() : Context{
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        prefs = PreferenceUtil(applicationContext)

        // KaKao SDK  초기화
        KakaoSdk.init(this, "acc5f5264d4bd2693ff7e975dd2e4dac")
    }
}