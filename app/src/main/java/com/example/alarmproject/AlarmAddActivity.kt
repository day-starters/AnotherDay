package com.example.alarmproject

import android.app.Activity
import android.app.AlarmManager
import android.app.AlarmManager.INTERVAL_DAY
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class AlarmAddActivity : Activity() {

    @RequiresApi(VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_add)

        lateinit var sqlDB: SQLiteDatabase

        var intent = intent

        //var alarmDB=mutableMapOf()
        //키는 요일로 하고, 값은 List<String>으로 해서 디비에서 로드시에 ON_OFF 확인해서 1인 것만 Alarm에 넣어주는 그런 타입으로 하면 될 것 같고 매주 반복이 false면 (월, 수, 목)이면 목요일날 최종적으로 알람 울리고 OFF 시켜버리는 거루
        var dayIDArray = arrayOf(
            R.id.togMon,
            R.id.togTue,
            R.id.togWed,
            R.id.togThu,
            R.id.togFri,
            R.id.togSat,
            R.id.togSun
        )
        var dayArray = ArrayList<ToggleButton>(7)
        var textInfor = findViewById<TextView>(R.id.textInfor)
        var btncal = findViewById<Button>(R.id.btnCal)
        var idv = findViewById<RadioButton>(R.id.indivAlarm)
        var gro = findViewById<RadioButton>(R.id.groupAlarm)

        var btnCancel = findViewById<Button>(R.id.btnCancel)
        var btnSave = findViewById<Button>(R.id.btnSave)

        var tPicker = arrayOf(
            findViewById<TimePicker>(R.id.timePicker1),
            findViewById<TimePicker>(R.id.timePicker2)
        )

        var tAM_PM = Array<String>(2, { "" })
        var tHour = Array<String>(2, { "" })
        var tMin = arrayOf(tPicker[0].currentMinute.toString(), tPicker[1].currentMinute.toString())

        var interval = findViewById<EditText>(R.id.interval)
        var editAlarmName = findViewById<EditText>(R.id.editAlarmName)

        textInfor.setText("")
        var dayText = ""

        var cYear: String = Calendar.getInstance().get(Calendar.YEAR).toString()
        var cMonth: String = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString()
        var cDate: String = Calendar.getInstance().get(Calendar.DATE).toString()
        var dayFlag = intent.getBooleanExtra("alarmdayFlag", false)

        //flag가 true면 매주 해당 요일마다, false면 특정 날짜만
        var weekday = Array<String>(7, { "" })
        var groupFlag = false

        var dayContext = Array<String>(7, { "" })
        var timeContext = Array<String>(2, { "" })

        for (i in dayIDArray.indices) {
            dayArray.add(findViewById<ToggleButton>(dayIDArray[i]))
        }


        //수정일때 값 가져옴

        var editFlag = false
        if (intent.getStringExtra("alarmDay")!!.isNotEmpty()) {
            editFlag = true  //수정중이란 뜻
            findViewById<TextView>(R.id.topTitle).setText("알람 수정")
            btnSave.setText("수정")
            if (dayFlag) {
                dayContext = intent.getStringExtra("alarmDay")!!.split(", ").toTypedArray()
                dayText = intent.getStringExtra("alarmDay").toString()
                for (str in dayContext) {
                    when (str) {
                        "월" -> {
                            dayArray[0].isChecked = true
                            weekday[0] = "월"
                        }
                        "화" -> {
                            dayArray[1].isChecked = true
                            weekday[1] = "화"
                        }
                        "수" -> {
                            dayArray[2].isChecked = true
                            weekday[2] = "수"
                        }
                        "목" -> {
                            dayArray[3].isChecked = true
                            weekday[3] = "목"
                        }
                        "금" -> {
                            dayArray[4].isChecked = true
                            weekday[4] = "금"
                        }
                        "토" -> {
                            dayArray[5].isChecked = true
                            weekday[5] = "토"
                        }
                        "일" -> {
                            dayArray[6].isChecked = true
                            weekday[6] = "일"
                        }
                    }
                }
            } else {
                dayContext = intent.getStringExtra("alarmDay")!!.split("-").toTypedArray()
                cYear = dayContext[0]
                cMonth = dayContext[1]
                cDate = dayContext[2]
            }

            timeContext = intent.getStringExtra("alarmTime")!!.split(":").toTypedArray()

            editAlarmName.setText(intent.getStringExtra("alarmName"))

            tHour[0] = timeContext[0]
            tMin[0] = timeContext[1]
            tPicker[0].setCurrentHour(tHour[0].toInt())
            tPicker[0].setCurrentMinute(tMin[0].toInt())
        }

        //DB
        var myHelper = MainActivity.myDBHelper(this)
        sqlDB = myHelper.readableDatabase
        val cursor: Cursor = sqlDB.rawQuery("SELECT * FROM Alarm;", null)
        var count = cursor.count
        var id: Int = count + 1
        if (editFlag) {
            id = intent.getIntExtra("alarmID", id)
        }
        cursor.close();

        var db_day = findViewById<TextView>(R.id.db_day)

        textInfor.text =
            cYear + "년 " + cMonth + "월 " + cDate + "일 " + tAM_PM[0] + " " + tHour[0] + "시 " + tMin[0] + "분에 울림"

        cMonth = String.format("%02d", cMonth.toInt())
        cDate = String.format("%02d", cDate.toInt())
        db_day.text = "$cYear-$cMonth-$cDate"



        for (i in tPicker.indices) {
            if (tPicker[i].currentHour > 12) {
                tAM_PM[i] = "오후"
            } else {
                tAM_PM[i] = "오전"
            }
            tHour[i] = tPicker[i].currentHour.toString()
        }

        fun info() { //textInfor 변경
            if (tAM_PM[0] == "오후") {
                tHour[0] = (tHour[0].toInt() - 12).toString()
            }
            if (dayFlag && dayText.isNotEmpty()) {
                textInfor.text =
                    dayText + "요일 " + tAM_PM[0] + " " + tHour[0] + "시 " + tMin[0] + "분에 울림"
                db_day.text = dayText
            } else {
                if (dayFlag) {
                    dayFlag = false
                }
                textInfor.text =
                    cYear + "년 " + cMonth + "월 " + cDate + "일 " + tAM_PM[0] + " " + tHour[0] + "시 " + tMin[0] + "분에 울림"
                cMonth = String.format("%02d", cMonth.toInt())
                cDate = String.format("%02d", cDate.toInt())
                db_day.text = "$cYear-$cMonth-$cDate"
            }
            if (tAM_PM[0] == "오후") {
                tHour[0] = (tHour[0].toInt() + 12).toString()
            }
        }

        info()

        //그룹or개별 라디오 버튼에 따라 달라지는 화면
        val radio = findViewById<RadioGroup>(R.id.radio)
        val intervalLayout = findViewById<LinearLayout>(R.id.intervalLayout)
        val textSta = findViewById<TextView>(R.id.textSta)
        val textFin = findViewById<TextView>(R.id.textFin)
        val toggles = findViewById<LinearLayout>(R.id.toggles)
        val buttons = findViewById<LinearLayout>(R.id.buttons)
        val calendar = findViewById<LinearLayout>(R.id.calendar)

        radio.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.groupAlarm -> {
                    calendar.visibility = View.VISIBLE
                    toggles.visibility = View.VISIBLE
                    buttons.visibility = View.VISIBLE
                    intervalLayout.visibility = View.VISIBLE
                    tPicker[0].visibility = View.VISIBLE
                    tPicker[1].visibility = View.VISIBLE
                    textSta.visibility = View.VISIBLE
                    textFin.visibility = View.VISIBLE
                    btnSave.isEnabled = true
                    groupFlag = true

                }
                R.id.indivAlarm -> {
                    calendar.visibility = View.VISIBLE
                    toggles.visibility = View.VISIBLE
                    buttons.visibility = View.VISIBLE
                    intervalLayout.visibility = View.GONE
                    tPicker[0].visibility = View.VISIBLE
                    tPicker[1].visibility = View.GONE
                    textSta.visibility = View.GONE
                    textFin.visibility = View.GONE
                    btnSave.isEnabled = true
                    groupFlag = false

                }
            }
        }

        //수정일때 값 가져오는 것.
        if (editFlag) {
            if (intent.getBooleanExtra("alarmgroup", false)) {
                gro.isChecked = true
                idv.isEnabled = false
                interval.setText(intent.getIntExtra("groupInter", 0).toString())
                timeContext = intent.getStringExtra("groupLast")!!.split(":").toTypedArray()
                tHour[1] = timeContext[0]
                tMin[1] = timeContext[1]
                tPicker[1].setCurrentHour(tHour[1].toInt())
                tPicker[1].setCurrentMinute(tMin[1].toInt())

            } else {
                idv.isChecked = true
                gro.isEnabled = false
            }
        }

        //groupflag=true일때 실행시켜서 그룹 만들면될듯
        //이 함수 쓸때 매개변수로는 interval.text.toString().toInt() 넣으면 됩니당
        fun getGroup(interval: Int): Array<String> {
            val gap = (tHour[1].toInt() - tHour[0].toInt()) * 60 +
                    (tMin[1].toInt() - tMin[0].toInt())

            var group = Array<String>((gap / interval)+1) { "" }
            var h = tHour[0].toInt()
            var m = tMin[0].toInt()
            for (i in group.indices) {
                if (m > 59) {
                    h++
                    m -= 60
                }
                var hour = String.format("%02d", h)
                var min = String.format("%02d", m)
                group[i] = "$hour:$min" //hh:mm 형태로 저장됨
                Log.d("print", group[i])
                m += interval
            }
            return group
        }


        //타임피커
        for (i in tPicker.indices) {
            tPicker[i].setOnTimeChangedListener { view, hourOfDay, minute ->
                if (hourOfDay > 12) {
                    tAM_PM[i] = "오후"
                } else {
                    tAM_PM[i] = "오전"
                }
                tHour[i] = hourOfDay.toString()
                tMin[i] = minute.toString()
                info()
            }
        }

        for (i in dayIDArray.indices) {
            dayArray[i].setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    if (!dayFlag) {
                        dayFlag = true
                    }
                    weekday[i] = dayArray[i].text.toString()

                } else {
                    weekday[i] = ""
                }

                dayText = ""

                for (k in weekday.indices) {

                    if (dayText.length == 0) {
                        dayText = weekday[k]
                    } else if (weekday[k] != "") {
                        dayText = dayText + ", " + weekday[k]
                    }
                }

                info()
            }
        }

        btncal.setOnClickListener {//캘린더를 대화창으로 띄움
            val cal = Calendar.getInstance()
            var calDialog = DatePickerDialog(
                this, DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
                    cYear = y.toString()
                    cMonth = (m + 1).toString() //0부터 시작해서 1 더해줌
                    cDate = d.toString()
                    for (i in dayArray.indices) {
                        dayArray[i].isChecked = false
                    }
                    if (dayFlag) {
                        dayFlag = false
                    }
                    info()
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)
            )
            calDialog.show()

        }

        //===================================================================================
        var cal = Calendar.getInstance()

        fun setAlarm(id_: Int, time: String, dayText: String) {
            val alarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmintent = Intent(applicationContext, AlarmReceiver::class.java)
            alarmintent.putExtra("alarmID", id_)
            alarmintent.putExtra("alarmTime",time)

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext, id_, alarmintent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )


            var Alarm_Hour = time.split(":")[0].toInt()
            var Alarm_Minute = time.split(":")[1].toInt()

            if (dayFlag) { //요일 알람
                var weekday = dayText.split(", ")


                for (i in weekday.indices) {
                    cal = Calendar.getInstance()
                    var gap = when (weekday[i]) {
                        "일" -> {
                            Calendar.SUNDAY - cal.get(Calendar.DAY_OF_WEEK)
                        }
                        "월" -> {
                            Calendar.MONDAY - cal.get(Calendar.DAY_OF_WEEK)
                        }
                        "화" -> {
                            Calendar.TUESDAY - cal.get(Calendar.DAY_OF_WEEK)
                        }
                        "수" -> {
                            Calendar.WEDNESDAY - cal.get(Calendar.DAY_OF_WEEK)
                        }
                        "목" -> {
                            Calendar.THURSDAY - cal.get(Calendar.DAY_OF_WEEK)
                        }
                        "금" -> {
                            Calendar.FRIDAY - cal.get(Calendar.DAY_OF_WEEK)
                        }
                        "토" -> {
                            Calendar.SATURDAY - cal.get(Calendar.DAY_OF_WEEK)
                        }
                        else -> {
                            0
                        }
                    }
                    if (gap < 0) {
                        cal.add(Calendar.DATE, (gap + 7))
                    } else {
                        cal.add(Calendar.DATE, gap)
                    }

                    cal.set(Calendar.HOUR_OF_DAY, Alarm_Hour)
                    cal.set(Calendar.MINUTE, Alarm_Minute)
                    cal.set(Calendar.SECOND, 0)

                    Log.d(
                        "요일 알람설정 시간확인",
                        "${cal.get(Calendar.YEAR)}년 ${cal.get(Calendar.MONTH) + 1}월 ${cal.get(
                            Calendar.DATE
                        )}일" +
                                " ${cal.get(Calendar.HOUR_OF_DAY)}시 ${cal.get(Calendar.MINUTE)}분 ${cal.get(
                                    Calendar.SECOND
                                )}초"
                    )

                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(),
                        INTERVAL_DAY * 7,
                        pendingIntent
                    )
                }

            } else { //날짜 알람
                var Alarm_Month =
                    dayText.split("-")[1].toInt() - 1 // 2021-06-04 -> 5 0이 1월이여서 1뺴줘야함
                var Alarm_Date = dayText.split("-")[2].toInt() // 2021-06-04 -> 4


                cal.set(cYear.toInt(), Alarm_Month, Alarm_Date);
                cal.set(Calendar.HOUR_OF_DAY, Alarm_Hour)
                cal.set(Calendar.MINUTE, Alarm_Minute)
                cal.set(Calendar.SECOND, 0)

                Log.d(
                    "날짜 알람설정 시간확인",
                    "$id_ :: ${cal.get(Calendar.YEAR)}년 ${cal.get(Calendar.MONTH) + 1}월 ${cal.get(
                        Calendar.DATE
                    )}일" +
                            " ${cal.get(Calendar.HOUR_OF_DAY)}시 ${cal.get(Calendar.MINUTE)}분 ${cal.get(
                                Calendar.SECOND
                            )}초"
                )

                Log.d(
                    "알람설정 시간확인 ",
                    "밀리초 : ${cal.getTimeInMillis()}, 현재시간 밀리초 변환 : ${System.currentTimeMillis()}"
                )
                Log.d(
                    "알람설정 시간확인 ",
                    "${(cal.getTimeInMillis() - System.currentTimeMillis()) / 1000}초 뒤 알람"
                )

                alarmManager.setExactAndAllowWhileIdle( //한번만
                    AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(),
                    pendingIntent
                )
            }
        }

        fun offAlarm(id_:Int){  //알람끄기
            val alarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmintent = Intent(applicationContext, AlarmReceiver::class.java)
            alarmintent.putExtra("alarmID", id_)

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext, id_, alarmintent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
            Log.d("알람 off", id_.toString())

        }


        //=====================================================================================================================================
        btnSave.setOnClickListener {//알람저장
            var alarmName = editAlarmName.text.toString()
            if (alarmName == "") alarmName = "null"
            var hour = String.format("%02d", tHour[0].toInt())
            var min = String.format("%02d", tMin[0].toInt())
            var time = "$hour:$min"
            var ringday = db_day.text.toString()
            var state = 1
            var groupFlag2: Int
            if (groupFlag) groupFlag2 = 1
            else groupFlag2 = 0

            myHelper = MainActivity.myDBHelper(this)
            sqlDB = myHelper.writableDatabase
            myHelper.onUpgrade(sqlDB, 1, 1)

            var title = findViewById<TextView>(R.id.topTitle)
            // 개별 알람 저장
            if (idv.isChecked) {
                cal.set(cYear.toInt(), cMonth.toInt()-1, cDate.toInt());
                cal.set(Calendar.HOUR_OF_DAY, hour.toInt())
                cal.set(Calendar.MINUTE, min.toInt())
                cal.set(Calendar.SECOND, 0)

                if(!dayFlag && (cal.getTimeInMillis() < System.currentTimeMillis())){
                    Toast.makeText(applicationContext, "이전 시간에는 알람을 설정할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    if (title.text == "알람 수정")
                        sqlDB.execSQL("UPDATE Alarm SET name='$alarmName', ringtime='$time', day='$ringday', state=$state, groupFlag=$groupFlag2 WHERE id=$id;")
                    else
                        sqlDB.execSQL("INSERT OR REPLACE INTO Alarm VALUES ($id, '$alarmName', '$time', '$ringday', $state, $groupFlag2);")
                    Toast.makeText(applicationContext, "알람 저장 성공", Toast.LENGTH_SHORT).show()
                    Log.d("알람 저장", "$id")
                    sqlDB.close()
                    setAlarm(id, time, ringday)
                    finish()

                }

                // 그룹 알람 저장
            } else {
                cal.set(cYear.toInt(), cMonth.toInt()-1, cDate.toInt());
                cal.set(Calendar.HOUR_OF_DAY, hour.toInt())
                cal.set(Calendar.MINUTE, min.toInt())
                cal.set(Calendar.SECOND, 0)

                if (alarmName == "null") Toast.makeText(
                    applicationContext,
                    "알람 이름을 작성해주세요",
                    Toast.LENGTH_SHORT
                ).show()

                else if(!dayFlag && (cal.getTimeInMillis() < System.currentTimeMillis())){
                    Toast.makeText(applicationContext, "이전 시간에는 알람을 설정할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }

                else if(interval.text.isEmpty() || interval.text.toString().toIntOrNull() == null){
                    Toast.makeText(applicationContext, "시간 간격을 확인해주세요.", Toast.LENGTH_SHORT).show()
                }

                else {
                    val gap = (tHour[1].toInt() - tHour[0].toInt()) * 60 +
                            (tMin[1].toInt() - tMin[0].toInt())

                    if (gap <= 0)  //시작과 종료가 같거나 종료가 더 이른 시간일때
                        Toast.makeText(
                            applicationContext,
                            "시작 시각과 종료 시각을\n다시 확인해주세요",
                            Toast.LENGTH_SHORT
                        ).show()
                    else {
                        var childGroup = getGroup(interval.text.toString().toInt())
                        if (title.text == "알람 수정") {
                            sqlDB.execSQL("DELETE from Alarm WHERE name IN (SELECT name FROM Alarm WHERE id=$id)")
                            offAlarm(id)
                            var groupIDs=intent.getIntArrayExtra("groupIDs")
                            for (i in groupIDs!!) {
                                offAlarm(i)
                            }


                            for (i in childGroup.indices) {
                                var childTime = childGroup[i]
                                sqlDB.execSQL("INSERT INTO Alarm VALUES ($id, '$alarmName', '$childTime', '$ringday', $state, $groupFlag2);")
                                setAlarm(id, childTime, ringday)
                                id++
                            }
                        } else {
                            for (i in childGroup.indices) {
                                var childTime = childGroup[i]
                                sqlDB.execSQL("INSERT OR REPLACE INTO Alarm VALUES ($id, '$alarmName', '$childTime', '$ringday', $state, $groupFlag2);")
                                setAlarm(id, childTime, ringday)
                                id++
                            }
                        }
                        Toast.makeText(applicationContext, "알람 저장 성공", Toast.LENGTH_SHORT).show()
                        sqlDB.close()
                        finish()
                    }
                }
            }

            //=======================================================================================
            myHelper = MainActivity.myDBHelper(this)
            sqlDB = myHelper.readableDatabase
            val sqlCursor: Cursor = sqlDB.rawQuery("SELECT * FROM Alarm;", null)
            var total = sqlCursor.count
            sqlCursor.close();
            sqlDB.close()

            myHelper = MainActivity.myDBHelper(this)
            sqlDB = myHelper.writableDatabase
            for (i in 0 until total)
                sqlDB.execSQL("UPDATE Alarm SET id=rowid")

            sqlDB.close()
        }

        btnCancel.setOnClickListener { //취소
            finish()
        }


    }
}

