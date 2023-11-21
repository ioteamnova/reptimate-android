package com.reptimate.iot_teamnova.Scheduling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.reptimate.iot_teamnova.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orhanobut.logger.Logger
import java.util.*


class MyFirebaseMessagingService  : FirebaseMessagingService() {

    //메세지를 수신할 때 호출된다.(메세지를 받을때) remoteMessage는 수신한 메세지이다.
    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            Logger.d("Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            println(it)
            Logger.d("Message Notification Body: ${it.body}")
            val notificationInfo = mapOf(
                "title" to it.title.toString(),
                "body" to it.body.toString()
            )
            sendNotification(notificationInfo)
        }
    }

    override fun onNewToken(token: String) {
        Logger.w(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(messageBody: Map<String, String>) {
        println(messageBody)
        val bodyJsonString = messageBody["body"]

        var intent: Intent

        when (messageBody["title"]) {
            "chat" -> {
                intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("type", "chat")
            }
            "auctionClosed" -> {
                intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("type", "auctionClosed")
            }
            "auctionClosingRemind" -> {
                intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("type", "auctionClosingRemind")
            }
            "auctionPriceUpdate" -> {
                intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("type", "auctionPriceUpdate")
            }
            "CALENDAR" -> {
                intent = Intent(this, ScheduleActivity::class.java)
                intent.putExtra("type", "CALENDAR")
            }
            "REPETITION" -> {
                intent = Intent(this, ScheduleActivity::class.java)
                intent.putExtra("type", "REPETITION")
            }
            else -> {
                // Default intent or handle unrecognized types
                intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("type", "else")
            }
        }

        // Create a PendingIntent with the intent
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.reptimate_noti)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.reptimate_logo_200))
            .setContentTitle(messageBody["body"]) // 노티피케이션 제목
            .setContentText(messageBody["body"]) // 노티피케이션 내용
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 헤드업 노티피케이션
            .setAutoCancel(true) // 알림을 클릭하면 알림이 사라짐. (true)
            .setContentIntent(pendingIntent) // 인텐트

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                enableLights(true)
                lightColor = Color.RED
                description = "Description for My Channel"
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = Random().nextInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}