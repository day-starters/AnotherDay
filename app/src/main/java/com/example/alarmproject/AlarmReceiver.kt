package com.example.alarmproject

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "AlarmReceiver"
        const val NOTIFICATION_ID = 0
        const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
    }

    lateinit var notificationManager: NotificationManager
    var ID=0
    var setTime=Array<String>(2,{""})


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        ID=intent.getIntExtra("alarmID",0)
        //setTime= intent.getStringExtra("alarmTime")!!.split(":").toTypedArray()
        Log.d(TAG, "Received intent :$ID, $intent ")
        notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        deliverNotification(context)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun deliverNotification(context: Context) {
        val contentIntent = Intent(context, AlarmOn::class.java)
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(contentIntent)
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        /*
        1. FLAG_UPDATE_CURRENT : 현재 PendingIntent를 유지하고, 대신 인텐트의 extra data는 새로 전달된 Intent로 교체
        2. FLAG_CANCEL_CURRENT : 현재 인텐트가 이미 등록되어있다면 삭제, 다시 등록
        3. FLAG_NO_CREATE : 이미 등록된 인텐트가 있다면, null
        4. FLAG_ONE_SHOT : 한번 사용되면, 그 다음에 다시 사용하지 않음
         */
        var cal = Calendar.getInstance()

        val builder =
            NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                //                .setSmallIcon(R.drawable.ic_launcher_background) //아이콘
                //                .setContentTitle("알람") //제목
                //                .setContentText("알람이 울립니다.") //내용
                .setFullScreenIntent(contentPendingIntent, true)
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                //.setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_CALL)

        Log.d(
            "알람 울림",
            ID.toString() + ", ${cal.get(Calendar.YEAR)}년 ${cal.get(Calendar.MONTH) + 1}월 ${cal.get(
                Calendar.DATE
            )}일" +
                    " ${cal.get(Calendar.HOUR_OF_DAY)}시 ${cal.get(Calendar.MINUTE)}분 ${cal.get(
                        Calendar.SECOND
                    )}초"
        )

        //notificationManager.notify(ID, builder.build())

        //if((setTime[0].toInt()*60)+(setTime[1].toInt())
        //        >(cal.get(Calendar.HOUR_OF_DAY)*60)+cal.get(Calendar.MINUTE)-5) {

        // }
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                PRIMARY_CHANNEL_ID,
                "Stand up notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "AlarmManager Tests"
            notificationManager.createNotificationChannel(
                notificationChannel)
        }
    }
}