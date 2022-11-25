package com.example.alarmproject

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alarm_off.*
import kotlinx.android.synthetic.main.weather_tab.*
import org.jsoup.Jsoup
import org.w3c.dom.Text
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

open class AlarmOff : Activity() {

    var textToSpeech: TextToSpeech? = null

    // 현재
    var nowWeather = ""
    var nowTemp = ""
    var nowWind = ""
    var nowHumidity = ""
    var nowDust = ""
    var nowDustTxt = ""
    var nowDustNum = ""
    var nowWeather_ = "" // 날씨 텍스트에서 날씨만 추출

    // 현재
    var temp = "" // 날씨
    var rain = "" // 강수 확률
    var humidity = "" // 습도
    var now = ""

    var calendar = Calendar.getInstance()
    var month = calendar.get(Calendar.MONTH) + 1    //현재 월
    var date = calendar.get(Calendar.DAY_OF_MONTH)  //현재 일
    var ampm : String = ""                          //AM / PM
    var location = ""
    var hourTime = calendar.get(Calendar.HOUR_OF_DAY)
    var hour = calendar.get(Calendar.HOUR_OF_DAY)   //현재 시
    var minute = calendar.get(Calendar.MINUTE)      //현재 분

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_off)

        var switchTTS : Switch = findViewById(R.id.switchTTS)
        var tvLocation : TextView = findViewById(R.id.tvLocation)
        var tvWeather : TextView = findViewById(R.id.tvWeather)
        var tvDustTxt : TextView = findViewById(R.id.tvDustTxt)
        var tvTem : TextView = findViewById(R.id.tvTem)
        var tvHumidity : TextView = findViewById(R.id.tvHumidity)
        var tvWind : TextView = findViewById(R.id.tvWind)
        var btnAlarmOk : Button = findViewById(R.id.btnAlarmOk)
        var btnNews : Button = findViewById(R.id.btnNews)

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

        //시간, 날짜 지정
        tvDate.text = "${String.format("%02d", month)}월 ${String.format("%02d", date)}일 ${day}요일"
        tvTime.text = "${String.format("%02d", hour)} : ${String.format("%02d", minute)}"

        switchTTS.isChecked = false //tts off으로 설정

        if(hour > 12) {
            ampm = "오후"
            hour -= 12
        } else {
            ampm = "오전"
        }


        var base = "https://search.daum.net/search?w=tot&q="

        Thread(Runnable {

            val pref = this.getSharedPreferences("MessageKey", 0)
            val msg = pref.getString("MessageKey", "")

            if (msg == "") {
                moveTaskToBack(true) // 태스크를 백그라운드로 이동
                if (Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask();
                } else {
                    finish();
                } // 액티비티 종료 + 태스크 리스트에서 지우기
                android.os.Process.killProcess(android.os.Process.myPid()) // 앱 프로세스 종료
            } else {
                location = msg.toString()
                var url = "$base$location 날씨"
                var doc = Jsoup.connect(url).get()

                nowWeather = doc.select(".cont_today").select(".txt_weather").text()
                nowTemp = doc.select(".cont_today").select(".txt_temp").text()
                nowWind = doc.select(".info_another").select("dl:nth-child(1)").text().split(" ")[1]
                nowHumidity = doc.select(".info_another").select("dl:nth-child(2)").text().split(" ")[1]
                nowDustTxt = doc.select(".info_another").select("a").text() .split(" ")[1]
                nowDustNum = doc.select(".info_another").select("a").text().split(" ")[2]
                nowDust = nowDustTxt + " " + nowDustNum
                nowWeather_ = nowWeather.split(", ")[1]

                temp = doc.select("table:nth-child(2)").select(".type_temp").select("td").text()
                rain = doc.select("table:nth-child(2)").select("tr:nth-child(2)").select("td").text()
                humidity = doc.select("table:nth-child(2)").select(".type_humidity").select("td").text()

                this@AlarmOff.runOnUiThread(java.lang.Runnable {
                    // 현재 시간 저장

                    if(hourTime == 5) { // 일출
                        now = "sunrise"
                        off_alarm.setBackgroundResource(R.drawable.sunset_bg)

                    }
                    else if (hourTime in 6 .. 16){ // 낮
                        now = "morning"
                        off_alarm.setBackgroundResource(R.drawable.morning_bg)

                    }
                    else if (hourTime == 17){ // 일몰
                        now = "sunrise"
                        off_alarm.setBackgroundResource(R.drawable.sunset_bg)
                    }

                    else if (hourTime in 18 .. 19){ // 저녁
                        now = "evening"
                        off_alarm.setBackgroundResource(R.drawable.evening_bg)
                    }
                    else if (hourTime in 20 .. 23 || hour in 0 .. 4){ // 밤
                        now = "night"
                        off_alarm.setBackgroundResource(R.drawable.night_bg)

                    }

                    setWeatherImg(nowWeather_, imageWeather, now)
                    //setHumidityImg(nowHumidity.toInt(), imageHumidity)
                    tvLocation.text = location
                    tvWeather.text = nowWeather_
                    tvTem.text = nowTemp
                    tvDustTxt.text = nowDustTxt
                    tvHumidity.text = nowHumidity
                    tvWind.text = nowWind
                })
            }
        }).start()

        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                //사용할 언어를 설정
                val result = textToSpeech!!.setLanguage(Locale.KOREA)
                //언어 데이터가 없거나 혹은 언어가 지원하지 않으면
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    //음성 톤(피치)
                    textToSpeech!!.setPitch(1.0f)
                    //읽는 속도
                    textToSpeech!!.setSpeechRate(1.0f)
                }
            }
        })

        //확인버튼
        btnAlarmOk.setOnClickListener {
            moveTaskToBack(true) // 태스크를 백그라운드로 이동
            if (Build.VERSION.SDK_INT >= 21) {
                finishAndRemoveTask();
            } else {
                finish();
            } // 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid()) // 앱 프로세스 종료
        }

        //뉴스버튼
        btnNews.setOnClickListener {
            var intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("tab", 2)
            startActivity(intent)
        }

        //tts 스위치 켜져있을 때
        switchTTS.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                Speech() //버튼을 누르면 텍스트를 음성으로 말해준다.
            }
            else {
                textToSpeech!!.stop()
            }
        }

    }

    //대본
    private fun Speech() {
        var text = "안녕하세요 또하시입니다. 현재 시각은 ${ampm} ${hour}시 ${minute}분이고, 현재 ${location}의 기온은 ${nowTemp}도 입니다. 미세먼지는 ${nowDustTxt}이고, 풍속은 ${nowWind}, 습도는 ${nowHumidity}입니다. 좋은 하루 되세요. "
        // QUEUE_FLUSH: Queue 값을 초기화한 후 값을 넣는다.
        // QUEUE_ADD: 현재 Queue에 값을 추가하는 옵션이다.
        // API 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //TTS는 롤리팝(5.0, api21)이상에서 지원
            textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

}