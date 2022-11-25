package com.example.alarmproject

import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.concurrent.thread

class AlarmOn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.alarm_on)

        var dateTextView : TextView = findViewById(R.id.dateTextView)
        var clockTextView : TextView = findViewById(R.id.clockTextView)
        var ampmTextView : TextView = findViewById(R.id.ampmTextView)
        var secondTextView : TextView = findViewById(R.id.secondTextView)
        var alarmSeekBar : SeekBar = findViewById(R.id.alarmSeekBar)

        //알람 소리
        val uriRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(this, uriRingtone)
        ringtone.play()

        thread(start=true) {
            while (!Thread.interrupted()) {
                runOnUiThread(Runnable() {
                    thread(start=true) {
                        var calendar = Calendar.getInstance()
                        var year = calendar.get(Calendar.YEAR)          //현재 년도
                        var month = calendar.get(Calendar.MONTH) + 1    //현재 월
                        var date = calendar.get(Calendar.DAY_OF_MONTH)  //현재 일
                        var day : String                                //현재 요일
                        when(calendar.get(Calendar.DAY_OF_WEEK)) {
                            1 -> day = "일"
                            2 -> day = "월"
                            3 -> day = "화"
                            4 -> day = "수"
                            5 -> day = "목"
                            6 -> day = "금"
                            7 -> day = "토"
                            else -> day = "error"
                        }
                        var hour = calendar.get(Calendar.HOUR_OF_DAY)   //현재 시
                        var minute = calendar.get(Calendar.MINUTE)      //현재 분
                        var second = calendar.get(Calendar.SECOND)      //현재 초
                        var ampm : String = ""                          //AM / PM

                        if(hour > 12) {
                            ampm = "   PM"
                            hour -= 12
                        } else {
                            ampm = "   AM"
                        }

                        dateTextView.text = "${year}년 ${String.format("%02d", month)}월 ${String.format("%02d", date)}일 ${day}요일"
                        clockTextView.text = "${String.format("%02d", hour)} : ${String.format("%02d", minute)}"
                        secondTextView.text = "${String.format("  %02d", second)}"
                        ampmTextView.text = "$ampm"
                    }
                })
                try {
                    Thread.sleep(1000) //1초씩 시간 흐르게
                } catch (e : InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        //시크바 (왼쪽, 오른쪽 = 알람 해제)
        alarmSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 1 || progress == 100) {
                    var alarmOffIntent = Intent(applicationContext, AlarmOff::class.java)
                    startActivity(alarmOffIntent)
                    ringtone.stop()
                    finish()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })


    }
}