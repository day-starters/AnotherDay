package com.example.alarmproject

import android.app.TabActivity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.amitshekhar.DebugDB
import kotlinx.android.synthetic.main.weather_tab.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.lang.Math.abs
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


// drawable 색상 변경 함수
object MyDrawableCompat {
    fun setColorFilter(view: View, colorStr: String) {
        var color = Color.parseColor(colorStr)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.background.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            view.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
        }
    }
}

// 날씨 이미지 설정 함수
fun setWeatherImg(weather : String, imageView : ImageView, now : String){
    if(weather == "맑음"){
        if(now == "night"){ // 밤일 경우 달 아이콘
            imageView.setImageResource(R.drawable.w_moon)
        }
        else{
            imageView.setImageResource(R.drawable.w_sun)
        }
    }
    else if(weather == "구름조금"){
        if(now == "night"){ // 밤일 경우 달 아이콘
            imageView.setImageResource(R.drawable.w_mooncloud)
        }
        else{
            imageView.setImageResource(R.drawable.w_cloud1)
        }
    }
    else if(weather == "구름많음"){
        imageView.setImageResource(R.drawable.w_cloud2)
    }
    else if(weather == "흐림"){
        imageView.setImageResource(R.drawable.w_cloud3)
    }
    else if(weather.contains("비 또는")){
        imageView.setImageResource(R.drawable.w_rain2)
    }
    else if(weather.contains("비")){
        imageView.setImageResource(R.drawable.w_rain1)
    }
    else if(weather.contains("눈 또는")){
        imageView.setImageResource(R.drawable.w_snow2)
    }
    else if(weather.contains("눈")){
        imageView.setImageResource(R.drawable.w_snow1)
    }
    else if(weather.contains("천둥번개")){
        imageView.setImageResource(R.drawable.w_thunder)
    }
    else{
        imageView.setImageResource(R.drawable.w_cloud4)
    }
}

// 습도 이미지 설정 함수
fun setHumidityImg(humidity : Int, imageView : ImageView){
    if(humidity == 0){
        imageView.setImageResource(R.drawable.h0)
    }
    else if(humidity in 1 .. 10){
        imageView.setImageResource(R.drawable.h10)
    }
    else if(humidity in 11 .. 20){
        imageView.setImageResource(R.drawable.h20)
    }
    else if(humidity in 21 .. 30){
        imageView.setImageResource(R.drawable.h30)
    }
    else if(humidity in 31 .. 40){
        imageView.setImageResource(R.drawable.h30)
    }
    else if(humidity in 41 .. 50){
        imageView.setImageResource(R.drawable.h40)
    }
    else if(humidity in 51 .. 60){
        imageView.setImageResource(R.drawable.h50)
    }
    else if(humidity in 61 .. 70){
        imageView.setImageResource(R.drawable.h60)
    }
    else if(humidity in 71 .. 80){
        imageView.setImageResource(R.drawable.h70)
    }
    else if(humidity in 81 .. 90){
        imageView.setImageResource(R.drawable.h80)
    }
    else if(humidity in 91 .. 99){
        imageView.setImageResource(R.drawable.h90)
    }
    else{
        imageView.setImageResource(R.drawable.h100)
    }
}

// 강수 확률 이미지 설정 함수
fun setRainImg(rain : Int, imageView : ImageView){
    if(rain == 0){
        imageView.setImageResource(R.drawable.rain0)
    }
    else if(rain in 1 .. 30){
        imageView.setImageResource(R.drawable.rain30)

    }
    else if(rain in 30 .. 59){
        imageView.setImageResource(R.drawable.rain60)
    }
    else{
        imageView.setImageResource(R.drawable.rain100)
    }
}

// 버튼 색상 설정 함수
fun setBtnColor(btnArray : Array<Button>, nowBit : Int, now : String){
    if(now == "sunrise" || now == "sunset"){ // 일출 또는 일몰 시간대
        if(nowBit == 1) {
            for (i in 0 .. 2){
                MyDrawableCompat.setColorFilter(btnArray[i], "#B55A1E")
            }
        }
        else{
            for (i in 3 .. 5){
                MyDrawableCompat.setColorFilter(btnArray[i], "#B55A1E")
            }
        }
    }
    else if(now == "morning"){ // 아침 시간대
        if(nowBit == 1) {
            for (i in 0 .. 2){
                MyDrawableCompat.setColorFilter(btnArray[i], "#1F73A6")
            }
        }
        else{
            for (i in 3 .. 5){
                MyDrawableCompat.setColorFilter(btnArray[i], "#1F73A6")
            }
        }
    }
    else if(now == "evening"){ // 저녁 시간대
        if(nowBit == 1) {
            for (i in 0 .. 2){
                MyDrawableCompat.setColorFilter(btnArray[i], "#104383")
            }
        }
        else{
            for (i in 3 .. 5){
                MyDrawableCompat.setColorFilter(btnArray[i], "#104383")
            }
        }
    }
    else{ // 밤 시간대
        if(nowBit == 1) {
            for (i in 0 .. 2){
                MyDrawableCompat.setColorFilter(btnArray[i], "#0D305F")
            }
        }
        else{
            for (i in 3 .. 5){
                MyDrawableCompat.setColorFilter(btnArray[i], "#0D305F")
            }
        }
    }
}


@Suppress("deprecation")
class MainActivity : TabActivity() {

    var news_List = arrayListOf<news_item>()
    var news_List_From_DB = arrayListOf<news_item>()
    var DBname = "test100"
    var now = "" // 현재 시간대 메모 String
    var isCrawling = true // DB의 크롤링 타임으로 부터 1시간 이상 지났을 때 true 또는 DB에 데이터가 없을 때 true
    lateinit var data : ArrayList<news_item>
    // 크롤링
    var base = "https://search.daum.net/search?w=tot&q="

    // 현재
    var nowWeather = "" // 날씨
    var nowTemp = "" // 기온
    var nowWind = "" // 풍속
    var nowHumidity = "" // 습도
    var nowDustTxt = "" // 미세먼지 텍스트
    var nowDustNum = "" // 미세먼지 농도
    var nowDust = "" // 미세먼지 = 미세먼지 텍스트 + 미세먼지 농도

    // 오늘
    var weather = "" // 날씨(맑음, 구름많음 등)
    var temp = "" // 기온
    var rain = "" // 강수 확률
    var humidity = "" // 습도

    // 내일
    var tomWeather = ""
    var tomTemp = ""
    var tomRain = ""
    var tomHumidity = ""

    var weatherArray = List<String>(8, {""})
    var tempArray = List<String>(8, {""})
    var rainArray = List<String>(8, {""})
    var humArray = List<String>(8, {""})
    var tomWeatherArray = List<String>(8, {""})
    var tomTempArray = List<String>(8, {""})
    var tomRainArray = List<String>(8, {""})
    var tomHumArray = List<String>(8, {""})

    var imgIdArray = arrayOf<ImageView>()
    var txtIdArray = arrayOf<TextView>()
    var tomImgIdArray = arrayOf<ImageView>()
    var tomTxtIdArray = arrayOf<TextView>()

    var lightArray = arrayOf<View>()
    var darkArray = arrayOf<Button>()

    var createrun = false
    var newsCrawlingrun = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createrun = false
        newsCrawlingrun = false

        var tabHost = this.tabHost

        var tabSpecAlarm = tabHost.newTabSpec("ALARM").setIndicator("알람")
        tabSpecAlarm.setContent(R.id.tabAlarm)
        tabHost.addTab(tabSpecAlarm)

        var tabSpecWeather = tabHost.newTabSpec("WEATHER").setIndicator("날씨")
        tabSpecWeather.setContent(R.id.tabWeather)
        tabHost.addTab(tabSpecWeather)

        var tabSpecNews = tabHost.newTabSpec("NEWS").setIndicator("뉴스")
        tabSpecNews.setContent(R.id.tabNews)
        tabHost.addTab(tabSpecNews)

        tabHost.currentTab = 0
        var intent = intent
        if(intent.hasExtra("tab")){
            tabHost.currentTab = intent.getIntExtra("tab", 0)
        }

        val btnAdd = findViewById<Button>(R.id.btnAdd)

        btnAdd.setOnClickListener {
            var intent = Intent(applicationContext, AlarmAddActivity::class.java)
            intent.putExtra("alarmID", 0)
            intent.putExtra("alarmName", "")
            intent.putExtra("alarmDay", "")
            intent.putExtra("alarmTime", "")
            intent.putExtra("alarmgroup", false)
            startActivity(intent)
        }


        // 오늘 -------------------------------------------------------------------------------------------
        // 날씨 버튼 클릭
        weatherBtn.setOnClickListener {
            for(i in 0 .. 6){
                txtIdArray[i].text = tempArray[i] + "℃"
                setWeatherImg(weatherArray[i], imgIdArray[i], now)
            }
            setBtnColor(darkArray, 1, now)
        }

        // 습도 버튼 클릭
        humBtn.setOnClickListener {
            for(i in 0 .. 6){
                txtIdArray[i].text = humArray[i]
                var humNum = humArray[i].replace("%", "").toInt()
                setHumidityImg(humNum, imgIdArray[i])
            }
            setBtnColor(darkArray, 1, now)
        }

        // 강수 확률 버튼 클릭
        rainBtn.setOnClickListener {
            for(i in 0 .. 6){
                txtIdArray[i].text = rainArray[i]
                var rainNum = rainArray[i].replace("%", "").toInt()
                setRainImg(rainNum, imgIdArray[i])
            }
            setBtnColor(darkArray, 1, now)
        }


        // 내일 -------------------------------------------------------------------------------------------
        // 날씨 버튼 클릭
        weatherBtn2.setOnClickListener {
            for(i in 0 .. 6){
                tomTxtIdArray[i].text = tomTempArray[i] + "℃"
                setWeatherImg(tomWeatherArray[i], tomImgIdArray[i], now)
            }
            setBtnColor(darkArray, 2, now)
        }

        // 습도 버튼 클릭
        humBtn2.setOnClickListener {
            for(i in 0 .. 6){
                tomTxtIdArray[i].text = tomHumArray[i]
                var humNum = tomHumArray[i].replace("%", "").toInt()
                setHumidityImg(humNum, tomImgIdArray[i])
            }
            setBtnColor(darkArray, 2, now)
        }

        // 강수 확률 버튼 클릭
        rainBtn2.setOnClickListener {
            for(i in 0 .. 6){
                tomTxtIdArray[i].text = tomRainArray[i]
                var rainNum = tomRainArray[i].replace("%", "").toInt()
                setRainImg(rainNum, tomImgIdArray[i])
            }
            setBtnColor(darkArray, 2, now)
        }


        // 양방향 액티비티 (위치 변경)
        change_loc.setOnClickListener {
            val intent = Intent(applicationContext, SetLocation::class.java)
            startActivityForResult(intent, 200)
        }

        //=============NEWS================//
        val text_top1 = findViewById<TextView>(R.id.text_top)

        // 현재시간 저장
        val cal2 = Calendar.getInstance()
        cal2.time = Date()
        // +0900 해줘야 한국 시간임 - avd기준, avd가 아닌 폰에서 구동시 아래 cal.add코드를 주석처리해야함
        // cal.add(Calendar.HOUR, 9)
        val df: DateFormat = SimpleDateFormat("dd:HH") // 일:시간
        val df1: DateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초")
        var nowtime = df.format(cal2.time)
        var realtime = df1.format(cal2.time)

        // catch문에서 예외처리를 위해 여기서 변수 선언
        var Date = ""
        var Hour = 31
        var NowDate = ""
        var NowTime = 31

        // DB 읽어오는 코드
        var myHelper = myDBHelper(this)
        var sqlDB: SQLiteDatabase
        try {
            sqlDB = myHelper.readableDatabase
            var cursor: Cursor = sqlDB.rawQuery("SELECT * FROM News;", null)
            if (cursor != null) {
                println("cursor != null")
                while (cursor.moveToNext()) {
                    //테이블에서 필요한 컬럼들의 값을 가져와서
                    val CrawlingTime = cursor.getString(cursor.getColumnIndex("CrawlingTime"))
                    val Ranking = cursor.getString(cursor.getColumnIndex("Ranking"))
                    val SearchWord = cursor.getString(cursor.getColumnIndex("SearchWord"))
                    val SearchTraffic = cursor.getString(cursor.getColumnIndex("SearchTraffic"))
                    val NewsTitle = cursor.getString(cursor.getColumnIndex("NewsTitle"))
                    val NewsDateTime = cursor.getString(cursor.getColumnIndex("NewsDateTime"))
                    val NewsSource = cursor.getString(cursor.getColumnIndex("NewsSource"))
                    val NewsLInk = cursor.getString(cursor.getColumnIndex("NewsLInk"))
                    val NewsImg = cursor.getString(cursor.getColumnIndex("NewsImg"))

                    news_List_From_DB.add(
                        news_item(
                            CrawlingTime,
                            nowtime,
                            NewsDateTime,
                            Ranking,
                            SearchWord,
                            SearchTraffic,
                            NewsTitle,
                            NewsSource,
                            NewsLInk,
                            NewsImg
                        )
                    )
                    if (news_List_From_DB.size == 20)
                        break
                }
            }
            sqlDB.close()

            // 크롤링은 <앱 실행 시간을 기준으로> DB의 크롤링 타임과 비교하여 1. 날짜가 달라지거나 2. 1시간 이상 차이나면 되게끔 함
            if (news_List_From_DB.count() != 0) {
                Date = news_List_From_DB[0].CrawlingTime.split("일")[0]
                Date = Date.substring(Date.lastIndex - 1, Date.lastIndex + 1)
                var temp = news_List_From_DB[0].CrawlingTime.split("시")[0]
                Hour = temp.substring(temp.lastIndex - 1, temp.lastIndex + 1).toInt()
                NowDate = nowtime.split(":")[0]
                NowTime = nowtime.split(":")[1].toInt()
            }

            // 데이터를 불러온 시점에서 1시간 이상 차이날 때 크롤링 실행
            if ((Date != NowDate) || abs(NowTime - Hour) >= 1) {
                isCrawling = true
//                Toast.makeText(
//                        applicationContext,
//                        "${Date}일 ${Hour}분에 크롤링함 -> DB 동기화후 로드",
//                        Toast.LENGTH_LONG
//                ).show()
                newsCrawlingrun = true
            }
        } catch (e: Exception) {
            if (Date == "") { // 디비에 저장된 데이터가 없을 때 isCrawling = true
                isCrawling = true
                Toast.makeText(applicationContext, "DB에 데이터가 없습니다\n 크롤링을 실시합니다.", Toast.LENGTH_LONG).show()
            } else
                Toast.makeText(applicationContext, "DB에서 데이터 로드시 에러 발생\n Error : ${e.message}", Toast.LENGTH_SHORT).show()
        }

        //네트워크를 통한 작업이기 때문에 비동기식으로 구현을 해야 한다. - 크롤링코드
        Thread(Runnable {

            // 데이터를 불러온 시점에서 1시간 이상 차이날 때 크롤링 실행
            if (isCrawling) {
                val url = "https://trends.google.co.kr/trends/trendingsearches/daily/rss?geo=KR"
                val doc = Jsoup.connect(url).get()

                val today: Elements = doc.select("item")
                val remove: ArrayList<String> = arrayListOf<String>(
                    "\n",
                    "</",
                    ">",
                    "&amp;#39;",
                    "&amp;quot;",
                    "amp;",
                    "&nbsp;",
                    "&lt;",
                    "&gt;"
                )

                var count = 0
                try {
                    // 반복문 돌리면서 크롤링함
                    today.forEach { item ->
                        if (news_List.count() >= 20)
                            return@forEach

                        val item_title = item.select("title").text().trim() // 검색어 타이틀 ex) 내일 날씨
                        var item_datetime = item.select("pubDate").text()
                            .trim() // 검색어 등장 시간 ex) Mon, 24 May 2021 10:00:00 +0900
                        var idate = item_datetime.split(", ")[1].substring(0..1) + ":" // 24  (일)
                        var itime = item_datetime.split("2021 ")[1].substring(0..1) // 10 (시간)
                        var News_Day_Hour: String = idate + itime // 24:10 (일:시간)

                        var item_traffic = doc.toString()
                            .split("ht:approx_traffic")[1 + news_List.count() * 2].split("/ht:approx_traffic")[0]  // 검색수 ex) 500,000
                        // println("날짜 : $idate, 시간 : $itime")

                        // 검색어 별로 기사를 2개씩 제공 해줘서 count*2를 해줘야 제대로 크롤링 가능 - 검색어별로 기사를 1개씩 제공하는 경우가 있어 유의해야함
                        var item_news_title = doc.toString()
                            .split("ht:news_item_title")[1 + count * 2].split("/ht:news_item_title")[0]  // 기사 제목
                        var item_link = doc.toString()
                            .split("ht:news_item_url")[1 + count * 2].split("/ht:news_item_url")[0]  // 기사 링크
                        var item_news_snippet = doc.toString()
                            .split("ht:news_item_snippet")[1 + count * 2].split("/ht:news_item_snippet")[0]  // 기사 요약 - 넣을지 말지 고민중
                        var item_img = doc.toString()
                            .split("ht:picture")[1 + news_List.count() * 4].split("/ht:picture")[0] // 기사의 간략한 이미지
                        var item_news_source = doc.toString()
                            .split("ht:news_item_source")[1 + news_List.count() * 2].split("ht:news_item_source")[0] // 기사의 출처

                        for (i in 0..remove.count() - 1) {
                            item_traffic = item_traffic.replace(remove[i], "").trim()
                            item_news_title = item_news_title.replace(remove[i], "").trim()
                            item_link = item_link.replace(remove[i], "").trim()
                            item_news_snippet = item_news_snippet.replace(remove[i], "").trim()
                            item_img = item_img.replace(remove[i], "").trim()
                            item_news_source = item_news_source.replace(remove[i], "").trim()
                        }
                        item_news_title.replace(",", ",\n")

                        // arrayList 리스트에 추가해 준다.
                        val Rank = (news_List.count() + 1).toString()
                        news_List.add(
                            news_item(
                                realtime,
                                nowtime,
                                News_Day_Hour,
                                Rank,
                                item_title,
                                item_traffic,
                                item_news_title,
                                item_news_source,
                                item_link,
                                item_img
                            )
                        )
                        var tt = item.toString().split("</ht:news_item>")
                        var c = tt.lastIndex // 기사 개수 파악~
                        count += c
                    }
                } catch (e: IndexOutOfBoundsException) {
                    Handler(Looper.getMainLooper()).post {
                        // Thread안에서 Toast 호출할때는 Handler을 사용해야함
                        Toast.makeText(applicationContext, "크롤링 - 인덱스 에러", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        //Thread안에서 Toast 호출할때는 Handler을 사용해야함
                        Toast.makeText(applicationContext, "크롤링시 에러 발생\n Error : ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                // 디비 수정하기
                try {
                    // DB에 쓰는 코드
                    //myHelper.writableDatabase 이게 sqlDB.Open() 이라고 생각하면 된다
                    sqlDB = myHelper.writableDatabase
                    DebugDB.getAddressLog()

                    // news_List : 크롤링해서 저장하는 ArrayList 타입 변수명, news_List.indices 대신에 숫자로 써도 무방
                    for (i in news_List.indices) {
                        // 디비에 데이터가 있음에도 불구하고 크롤링을 한다는 건 데이터 갱신이 필요한 시점이라는 뜻이므로 UPDATE사용
                        if (news_List_From_DB.count() != 0) { // news_List_From_DB : 디비에서 읽어와서 저장받는 ArrayList 타입 변수명
                            // "UPDATE News(테이블이름) SET CrawlingTime(컬럼이름) = '${news_List[i].realtime}'('${변수}') WHERE Num = $i(여긴 '' 안붙임!!;") - 마지막에 ; 넣는 거 까먹으면 안댐
                            sqlDB.execSQL("UPDATE News SET CrawlingTime = '${news_List[i].CrawlingTime}' WHERE Num = ${i + 1};")

                            if (i == 0) {
                                var temp = news_List_From_DB[0].CrawlingTime.split("시")[0]
                                Hour = temp.substring(temp.lastIndex - 1, temp.lastIndex + 1).toInt()
                            }

                            sqlDB.execSQL("UPDATE News SET SearchWord = '${news_List[i].item_title}' WHERE Num = ${i + 1};")
                            sqlDB.execSQL("UPDATE News SET SearchTraffic = '${news_List[i].item_traffic}' WHERE Num = ${i + 1};")
                            sqlDB.execSQL("UPDATE News SET NewsTitle = '${news_List[i].item_news_title}' WHERE Num = ${i + 1};") // ${Hour}U 업데이트 확인을 위해 넣음
                            sqlDB.execSQL("UPDATE News SET NewsDateTime = '${news_List[i].News_Day_Hour}' WHERE Num = ${i + 1};")
                            sqlDB.execSQL("UPDATE News SET NewsSource = '${news_List[i].item_news_source}' WHERE Num = ${i + 1};")
                            sqlDB.execSQL("UPDATE News SET NewsLInk = '${news_List[i].item_link}' WHERE Num = ${i + 1};")
                            sqlDB.execSQL("UPDATE News SET NewsImg = '${news_List[i].item_img}' WHERE Num = ${i + 1};")
                        }
                        // 디비에 데이터가 없는 경우이므로 해당 항목들을 INSERT 삽입 해준다.
                        else {
                            var temp =
                                "'${news_List[i].CrawlingTime}', '${news_List[i].Rank}', '${news_List[i].item_title}', '${news_List[i].item_traffic}', '${news_List[i].nowtime}', '${news_List[i].item_news_title}', '${news_List[i].News_Day_Hour}', '${news_List[i].item_news_source}', '${news_List[i].item_link}', '${news_List[i].item_img}'"
                            var sqlstring =
                                "INSERT INTO News('CrawlingTime', 'Ranking', 'SearchWord', 'SearchTraffic', 'PubDate', 'NewsTitle', 'NewsDateTime', 'NewsSource', 'NewsLInk', 'NewsImg') VALUES($temp);"
                            sqlDB.execSQL(sqlstring)
                        }
                    }
                    sqlDB.close()
                } catch (e: Exception) {
                    // text_top1.setText("DB삽입, 수정시 에러 발생\n Error : ${e.message}")
                }
                // 업데이트 후 DB를 다시 읽어서 화면에 출력함
                // DB 읽어오는 코드
                news_List_From_DB.clear()
                var myHelper = myDBHelper(this)
                var sqlDB: SQLiteDatabase
                try {
                    sqlDB = myHelper.readableDatabase
                    var cursor: Cursor = sqlDB.rawQuery("SELECT * FROM News;", null)
                    if (cursor != null) {
                        println("cursor != null")
                        while (cursor.moveToNext()) {
                            //테이블에서 필요한 컬럼들의 값을 가져와서
                            val CrawlingTime = cursor.getString(cursor.getColumnIndex("CrawlingTime"))
                            val Ranking = cursor.getString(cursor.getColumnIndex("Ranking"))
                            val SearchWord = cursor.getString(cursor.getColumnIndex("SearchWord"))
                            val SearchTraffic = cursor.getString(cursor.getColumnIndex("SearchTraffic"))
                            val NewsTitle = cursor.getString(cursor.getColumnIndex("NewsTitle"))
                            val NewsDateTime = cursor.getString(cursor.getColumnIndex("NewsDateTime"))
                            val NewsSource = cursor.getString(cursor.getColumnIndex("NewsSource"))
                            val NewsLInk = cursor.getString(cursor.getColumnIndex("NewsLInk"))
                            val NewsImg = cursor.getString(cursor.getColumnIndex("NewsImg"))

                            news_List_From_DB.add(
                                news_item(
                                    CrawlingTime,
                                    nowtime,
                                    NewsDateTime,
                                    Ranking,
                                    SearchWord,
                                    SearchTraffic,
                                    NewsTitle,
                                    NewsSource,
                                    NewsLInk,
                                    NewsImg
                                )
                            )
                            if (news_List_From_DB.size == 20) {
                                sqlDB.close()
                                break
                            }
                        }
                    }
                }
                catch(e: Exception) {

                }
                finally {
                    newsCrawlingrun = true
                    runOnUiThread {
                        //==============NEWS================//
                        //어답터 연결하기
                        var mainListView = findViewById<ListView>(R.id.mainListView1)
                        val newsAdapter = MainListAdapter(this, news_List_From_DB)
                        mainListView.adapter = newsAdapter

                        mainListView.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
                            val item = parent.getItemAtPosition(position) as news_item
                            var title = item.item_news_title
                            var link = item.item_link
                            Toast.makeText(
                                applicationContext,
                                "기사 제목 : ${title}로 이동합니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            startActivity(intent)
                        }
                    }
                }
            }
        }).start()
        runOnUiThread {

        }
        //==============WEATHER===================//
        // 배경 설정에 필요한 시간대 변수 선언
        // 현재시간 저장
        val cal = Calendar.getInstance()
        cal.time = Date()
        val hour: DateFormat = SimpleDateFormat("HH")
        var hourTime = hour.format(cal.time).toInt()
        var now = "" // 현재 시간대 메모 String

        // MessageKey 변수에 데이터가 저장되어 있음
        val pref = this.getSharedPreferences("MessageKey", 0)
        // MessageKey 값 지우기 (테스트용)
//        val editor = pref.edit()
//        editor.putString("MessageKey", "").apply()
        // MessageKey 값 불러오기
        val msg = pref.getString("MessageKey", "")

        // MessageKey 값 있으면 가져오기
        if (msg == "") textLoc.text = "지정된 위치 없음"
        else {
            textLoc.text = msg.toString()

            //네트워크를 통한 작업이기 때문에 비동기식으로 구현을 해야 한다.
            Thread(Runnable {
                var location = textLoc.text
                var url = "$base$location 날씨"
                var doc = Jsoup.connect(url).get()

                nowWeather = doc.select(".cont_today").select(".txt_weather").text().split(", ")[1]
                nowTemp = doc.select(".cont_today").select(".txt_temp").text()
                nowWind = doc.select(".info_another").select("dl:nth-child(1)").text().split(" ")[1]
                nowHumidity = doc.select(".info_another").select("dl:nth-child(2)").text().split(" ")[1]
                nowDust = doc.select(".info_another").select("a").text().replace("미세먼지 ", "")
                try { // 인덱스 에러 방지
                    nowDustTxt = nowDust.split(" ")[0]
                    nowDustNum = nowDust.split(" ")[1]
                } catch (e: IndexOutOfBoundsException) {
                    nowDustTxt = nowDust.split(" ")[0]
                }

                weather = doc.select("table:nth-child(2)").select(".type_temp").select("td")
                    .select("span:nth-child(1)").text()
                temp = doc.select("table:nth-child(2)").select(".type_temp").select("td")
                    .select("span:nth-child(2)").text()
                rain = doc.select("table:nth-child(2)").select("tr:nth-child(2)").select("td").text()
                humidity = doc.select("table:nth-child(2)").select(".type_humidity").select("td").text().replace(".0", "")

                tomWeather = doc.select("table:nth-child(3)").select(".type_temp").select("td")
                    .select("span:nth-child(1)").text()
                tomTemp = doc.select("table:nth-child(3)").select(".type_temp").select("td")
                    .select("span:nth-child(2)").text()
                tomRain = doc.select("table:nth-child(3)").select("tr:nth-child(2)").select("td").text()
                tomHumidity = doc.select("table:nth-child(3)").select(".type_humidity").select("td").text().replace(".0", "")

                imgIdArray = arrayOf(img0, img3, img6, img9, img12, img18, img21)
                txtIdArray = arrayOf(txt0, txt3, txt6, txt9, txt12, txt18, txt21)
                tomImgIdArray = arrayOf(img02, img32, img62, img92, img122, img182, img212)
                tomTxtIdArray = arrayOf(txt02, txt32, txt62, txt92, txt122, txt182, txt212)

                lightArray = arrayOf(todayTxt, buttons, tomorrowTxt, buttons2)
                darkArray = arrayOf(weatherBtn, humBtn, rainBtn, weatherBtn2, humBtn2, rainBtn2)

                // 오늘 정보
                weatherArray = weather.split(" ")
                tempArray = temp.split(" ")
                rainArray = rain.split(" ")
                humArray = humidity.split(" ")

                // 내일 정보
                tomWeatherArray = tomWeather.split(" ")
                tomTempArray = tomTemp.split(" ")
                tomRainArray = tomRain.split(" ")
                tomHumArray = tomHumidity.split(" ")

                textLoc.text = location
                nowWeatherTxt.text = nowWeather
                nowTempTxt.text = nowTemp
                nowDustTxtV.text = "미세먼지: " + nowDust
                nowHumTxt.text = "습도: " + nowHumidity
                nowWindTxt.text = "풍속: " + nowWind
                createrun = true

            }).start()

            runOnUiThread {
                //==============WEATHERS================//
                while(true) {
                    if (createrun) {
                        createrun = false
                        break
                    }
                }
                //Toast.makeText(applicationContext, "데이터 출력 완료", Toast.LENGTH_SHORT).show()

                // 시간별 배경 변화
                if(hourTime == 5) { // 일출
                    now = "sunrise"
                    mainLayout.setBackgroundResource(R.drawable.sunset_bg)
                    for (i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#FA8C43")
                    }
                    for (i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#B55A1E")
                    }
                }
                else if (hourTime in 6 .. 16){ // 낮
                    now = "morning"
                    mainLayout.setBackgroundResource(R.drawable.morning_bg)
                    for(i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#058CDF")
                    }
                    for(i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#1F73A6")
                    }
                }
                else if (hourTime == 17){ // 일몰
                    now = "sunrise"
                    mainLayout.setBackgroundResource(R.drawable.sunset_bg)
                    for(i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#")
                    }
                    for(i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#")
                    }
                }

                else if (hourTime in 18 .. 19){ // 저녁
                    now = "evening"
                    mainLayout.setBackgroundResource(R.drawable.evening_bg)
                    for(i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#0254BA")
                    }
                    for(i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#104383")
                    }
                }
                else if (hourTime in 20 .. 23 || hourTime in 0 .. 4){ // 밤
                    now = "night"
                    mainLayout.setBackgroundResource(R.drawable.night_bg)
                    for(i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#2C71CE")
                    }
                    for(i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#0D305F")
                    }
                }

                if (tempArray[0] != "") {
                    setWeatherImg(nowWeather, nowWeatherImg, now)
                    for (i in 0..6) {
                        txtIdArray[i].text = tempArray[i] + "℃"
                        tomTxtIdArray[i].text = tomTempArray[i] + "℃"
                        setWeatherImg(weatherArray[i], imgIdArray[i], now)
                        setWeatherImg(tomWeatherArray[i], tomImgIdArray[i], now)
                    }
                }
                // 오늘 날씨 초기세팅
                if (tempArray[0] != ""){
                    weatherIcoRow.visibility = View.VISIBLE
                    for(i in 0 .. 6){
                        txtIdArray[i].text = tempArray[i] + "℃"
                        setWeatherImg(weatherArray[i], imgIdArray[i], now)
                    }
                }
//                else {
//                    Toast.makeText(applicationContext, "오늘 날씨 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
//                }

                // 내일 날씨 초기세팅
                if (tomTempArray[0] != ""){
                    weatherIcoRow2.visibility = View.VISIBLE
                    for (i in 0 .. 6) {
                        tomTxtIdArray[i].text = tomTempArray[i] + "℃"
                        setWeatherImg(tomWeatherArray[i], tomImgIdArray[i], now)
                    }
                }
//                else {
//                    Toast.makeText(applicationContext, "내일 날씨 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
//                }

            }
        }
    }

    //=====================NEWS==========================//

    class news_item(val CrawlingTime:String, val nowtime:String, val News_Day_Hour:String, val Rank: String, val item_title: String, val item_traffic: String, val item_news_title: String, val item_news_source: String, val item_link: String, val item_img: String)

    // context : 앱정보
    class myDBHelper(context: Context) : SQLiteOpenHelper(context, "test25", null, 1) {
        // 테이블 생성
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL("CREATE TABLE IF NOT EXISTS Alarm(" +
                    "id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "ringtime TEXT NOT NULL, " +
                    "day TEXT NOT NULL, " +
                    "state INTEGER NOT NULL, " +
                    "groupFlag INTEGER NOT NULL);")

            db?.execSQL("CREATE TABLE IF NOT EXISTS News(" +
                    "Num INTEGER primary key autoincrement, " +
                    "CrawlingTime TEXT NOT NULL, " +
                    "Ranking TEXT NOT NULL, " +
                    "SearchWord TEXT NOT NULL, " +
                    "SearchTraffic TEXT NOT NULL, " +
                    "PubDate TEXT NOT NULL, " +
                    "NewsTitle TEXT NOT NULL, " +
                    "NewsDateTime TEXT NOT NULL, " +
                    "NewsSource TEXT NOT NULL, " +
                    "NewsLInk TEXT NOT NULL, " +
                    "NewsImg TEXT NOT NULL);")
            db?.execSQL("CREATE TABLE IF NOT EXISTS saveLocation(place TEXT PRIMARY KEY);")
        }

        // 전체 테이블 삭제 후 다시 생성 -> DB 테이블 초기화 (레코드들 다 사라짐!! 유의)
        override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
            if(p1 == 1 && p2 == 1) {
//                db!!.execSQL("DROP TABLE IF EXISTS Alarm;")
//                db.execSQL("DROP TABLE IF EXISTS News;")
                onCreate(db)
            }
            // "위치 불러오기" 행 없으면 생성
            db?.execSQL("INSERT OR IGNORE INTO saveLocation VALUES ('위치 불러오기');")

            // "위치 불러오기" 행 제외하고 모두 지우기 (테스트용)
//        db.execSQL("DELETE FROM saveLocation WHERE place != '위치 불러오기';")
        }
    }

    override fun onResume() {
        super.onResume()
        lateinit var myHelper : myDBHelper
        lateinit var sqlDB : SQLiteDatabase

        myHelper = myDBHelper(this)
        sqlDB = myHelper.readableDatabase
        var cursor : Cursor

        // child alarm
        cursor = sqlDB.rawQuery("SELECT * FROM Alarm EXCEPT\n" +
                "SELECT * FROM Alarm GROUP BY name HAVING MIN(id) ORDER BY id;", null)
        var child_id  = mutableListOf<Int>()
        var child_name = mutableListOf<String>()
        var child_time = mutableListOf<String>()
        var child_day = mutableListOf<String>()
        var child_state = mutableListOf<Int>()
        var child_flag = mutableListOf<Int>()

        while (cursor.moveToNext()) {
            child_id.add(cursor.getInt(cursor.getColumnIndex("id")))
            child_name.add(cursor.getString(cursor.getColumnIndex("name")))
            child_time.add(cursor.getString(cursor.getColumnIndex("ringtime")))
            child_day.add(cursor.getString(cursor.getColumnIndex("day")))
            child_state.add(cursor.getInt(cursor.getColumnIndex("state")))
            child_flag.add(cursor.getInt(cursor.getColumnIndex("groupFlag")))
        }
        var subAlarmList = mutableListOf<Alarm>()
        for (i in child_id.indices) {
            // 상태 Int --> Boolean
            var state : Boolean = child_state[i] != 0
            val dayflag=!child_day[i].contains("-")
            // 이름 null --> ""
            var name : String
            if (child_name[i] == "null") name = ""
            else name = child_name[i]

            subAlarmList.add(Alarm(child_id[i], name, child_time[i], child_day[i], dayflag, state, null))
        }

        // parent alarm
        cursor = sqlDB.rawQuery("SELECT * FROM Alarm where groupFlag=1 GROUP BY name HAVING MIN(id) UNION SELECT * FROM Alarm where groupFlag=0 ORDER BY id", null)
        var parent_id  = mutableListOf<Int>()
        var parent_name = mutableListOf<String>()
        var parent_time = mutableListOf<String>()
        var parent_day = mutableListOf<String>()
        var parent_state = mutableListOf<Int>()
        var parent_flag = mutableListOf<Int>()

        while (cursor.moveToNext()) {
            parent_id.add(cursor.getInt(cursor.getColumnIndex("id")))
            parent_name.add(cursor.getString(cursor.getColumnIndex("name")))
            parent_time.add(cursor.getString(cursor.getColumnIndex("ringtime")))
            parent_day.add(cursor.getString(cursor.getColumnIndex("day")))
            parent_state.add(cursor.getInt(cursor.getColumnIndex("state")))
            parent_flag.add(cursor.getInt(cursor.getColumnIndex("groupFlag")))
        }

        var alarmList = mutableListOf<Alarm>()
        for (i in parent_id.indices) {
            // 상태 Int --> Boolean
            var state : Boolean = parent_state[i] != 0
            var groupFlag : Boolean = parent_flag[i] != 0
            val dayflag=!parent_day[i].contains("-")
            // 이름 null --> ""
            var name : String
            if (parent_name[i] == "null") name = ""
            else name = parent_name[i]
            if(groupFlag) {
                var groupList = mutableListOf<Alarm>()
                for (i in subAlarmList.indices) {
                    if (subAlarmList[i].name == name) {
                        groupList.add(subAlarmList[i])
                    }
                }

                var groupArray = arrayOfNulls<Alarm>(groupList.size)
                for (i in groupList.indices) {
                    groupArray[i] = groupList[i]
                }

                alarmList.add(Alarm(parent_id[i], name, parent_time[i], parent_day[i], dayflag, state, groupArray))
            } else{ alarmList.add(Alarm(parent_id[i], name, parent_time[i], parent_day[i], dayflag, state, null)) }
        }

        val alarmAdapter = AlarmListAdapter(this, alarmList.toTypedArray()) // mutableList를 배열로 변경
        var alarmList_info = findViewById<View>(R.id.alarmList) as ListView
        alarmList_info.adapter = alarmAdapter


        //==============WEATHER===================//
        // 배경 설정에 필요한 시간대 변수 선언
        // 현재시간 저장
        val cal = Calendar.getInstance()
        cal.time = Date()
        val hour: DateFormat = SimpleDateFormat("HH")
        var hourTime = hour.format(cal.time).toInt()
        var now = "" // 현재 시간대 메모 String

        // MessageKey 변수에 데이터가 저장되어 있음
        val pref = this.getSharedPreferences("MessageKey", 0)
        // MessageKey 값 지우기 (테스트용)
//        val editor = pref.edit()
//        editor.putString("MessageKey", "").apply()
        // MessageKey 값 불러오기
        val msg = pref.getString("MessageKey", "")

        // MessageKey 값 있으면 가져오기
        if (msg == "") textLoc.text = "지정된 위치 없음"
        else {
            textLoc.text = msg.toString()

            //네트워크를 통한 작업이기 때문에 비동기식으로 구현을 해야 한다.
            Thread(Runnable {
                var location = textLoc.text
                var url = "$base$location 날씨"
                var doc = Jsoup.connect(url).get()

                nowWeather = doc.select(".cont_today").select(".txt_weather").text().split(", ")[1]
                nowTemp = doc.select(".cont_today").select(".txt_temp").text()
                nowWind = doc.select(".info_another").select("dl:nth-child(1)").text().split(" ")[1]
                nowHumidity = doc.select(".info_another").select("dl:nth-child(2)").text().split(" ")[1]
                nowDust = doc.select(".info_another").select("a").text().replace("미세먼지 ", "")
                try { // 인덱스 에러 방지
                    nowDustTxt = nowDust.split(" ")[0]
                    nowDustNum = nowDust.split(" ")[1]
                } catch (e: IndexOutOfBoundsException) {
                    nowDustTxt = nowDust.split(" ")[0]
                }

                weather = doc.select("table:nth-child(2)").select(".type_temp").select("td")
                    .select("span:nth-child(1)").text()
                temp = doc.select("table:nth-child(2)").select(".type_temp").select("td")
                    .select("span:nth-child(2)").text()
                rain = doc.select("table:nth-child(2)").select("tr:nth-child(2)").select("td").text()
                humidity = doc.select("table:nth-child(2)").select(".type_humidity").select("td").text().replace(".0", "")

                tomWeather = doc.select("table:nth-child(3)").select(".type_temp").select("td")
                    .select("span:nth-child(1)").text()
                tomTemp = doc.select("table:nth-child(3)").select(".type_temp").select("td")
                    .select("span:nth-child(2)").text()
                tomRain = doc.select("table:nth-child(3)").select("tr:nth-child(2)").select("td").text()
                tomHumidity = doc.select("table:nth-child(3)").select(".type_humidity").select("td").text().replace(".0", "")

                imgIdArray = arrayOf(img0, img3, img6, img9, img12, img18, img21)
                txtIdArray = arrayOf(txt0, txt3, txt6, txt9, txt12, txt18, txt21)
                tomImgIdArray = arrayOf(img02, img32, img62, img92, img122, img182, img212)
                tomTxtIdArray = arrayOf(txt02, txt32, txt62, txt92, txt122, txt182, txt212)

                lightArray = arrayOf(todayTxt, buttons, tomorrowTxt, buttons2)
                darkArray = arrayOf(weatherBtn, humBtn, rainBtn, weatherBtn2, humBtn2, rainBtn2)

                // 오늘 정보
                weatherArray = weather.split(" ")
                tempArray = temp.split(" ")
                rainArray = rain.split(" ")
                humArray = humidity.split(" ")

                // 내일 정보
                tomWeatherArray = tomWeather.split(" ")
                tomTempArray = tomTemp.split(" ")
                tomRainArray = tomRain.split(" ")
                tomHumArray = tomHumidity.split(" ")

                textLoc.text = location
                nowWeatherTxt.text = nowWeather
                nowTempTxt.text = nowTemp
                nowDustTxtV.text = "미세먼지: " + nowDust
                nowHumTxt.text = "습도: " + nowHumidity
                nowWindTxt.text = "풍속: " + nowWind
                createrun = true

            }).start()

            runOnUiThread {
                //==============WEATHERS================//
                while(true) {
                    if (createrun) {
                        createrun = false
                        break
                    }
                }
                //Toast.makeText(applicationContext, "데이터 출력 완료", Toast.LENGTH_SHORT).show()

                // 시간별 배경 변화
                if(hourTime == 5) { // 일출
                    now = "sunrise"
                    mainLayout.setBackgroundResource(R.drawable.sunset_bg)
                    for (i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#FA8C43")
                    }
                    for (i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#B55A1E")
                    }
                }
                else if (hourTime in 6 .. 16){ // 낮
                    now = "morning"
                    mainLayout.setBackgroundResource(R.drawable.morning_bg)
                    for(i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#058CDF")
                    }
                    for(i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#1F73A6")
                    }
                }
                else if (hourTime == 17){ // 일몰
                    now = "sunrise"
                    mainLayout.setBackgroundResource(R.drawable.sunset_bg)
                    for(i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#")
                    }
                    for(i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#")
                    }
                }

                else if (hourTime in 18 .. 19){ // 저녁
                    now = "evening"
                    mainLayout.setBackgroundResource(R.drawable.evening_bg)
                    for(i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#0254BA")
                    }
                    for(i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#104383")
                    }
                }
                else if (hourTime in 20 .. 23 || hourTime in 0 .. 4){ // 밤
                    now = "night"
                    mainLayout.setBackgroundResource(R.drawable.night_bg)
                    for(i in 0 .. 3) {
                        MyDrawableCompat.setColorFilter(lightArray[i], "#2C71CE")
                    }
                    for(i in 0 .. 5) {
                        MyDrawableCompat.setColorFilter(darkArray[i], "#0D305F")
                    }
                }

                if (tempArray[0] != "") {
                    setWeatherImg(nowWeather, nowWeatherImg, now)
                    for (i in 0..6) {
                        txtIdArray[i].text = tempArray[i] + "℃"
                        tomTxtIdArray[i].text = tomTempArray[i] + "℃"
                        setWeatherImg(weatherArray[i], imgIdArray[i], now)
                        setWeatherImg(tomWeatherArray[i], tomImgIdArray[i], now)
                    }
                }
                // 초기 세팅
                if (tempArray[0] != ""){
                    weatherIcoRow.visibility = View.VISIBLE
                    for(i in 0 .. 6){
                        txtIdArray[i].text = tempArray[i] + "℃"
                        setWeatherImg(weatherArray[i], imgIdArray[i], now)
                    }
                }

                // 갑자기 뉴스 내용 안뜨면 밑에 sleep 주석처리한 거 주석 지우고 실행되게 바꿔바바

                Thread.sleep(200)
                //==============NEWS================//
                //어답터 연결하기
                var mainListView = findViewById<ListView>(R.id.mainListView1)
                val newsAdapter = MainListAdapter(this, news_List_From_DB)
                mainListView.adapter = newsAdapter

                mainListView.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
                    val item = parent.getItemAtPosition(position) as news_item
                    var title = item.item_news_title
                    var link = item.item_link
                    Toast.makeText(applicationContext, "기사 제목 : ${title}로 이동합니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(intent)
                }

            }
        }
        // onResume 끝
    }

    // SetLocation 액티비티로부터 데이터 받아오기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 200) {
            val loc = data!!.getStringExtra("Location")
            Toast.makeText(this, "위치 저장됨: $loc", Toast.LENGTH_LONG).show()
            textLoc.text = loc.toString()
        }
    }
}