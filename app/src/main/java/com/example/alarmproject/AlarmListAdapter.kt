package com.example.alarmproject

import android.app.AlarmManager
import android.app.AlarmManager.INTERVAL_DAY
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi


class AlarmListAdapter(val context: Context, val alarmList_info: Array<Alarm>) : BaseAdapter() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        /* LayoutInflater는 item을 Adapter에서 사용할 View로 부풀려주는(inflate) 역할을 한다. */
        val view: View = LayoutInflater.from(context).inflate(R.layout.alarm_item, null)

        /* 위에서 생성된 view를 res-layout-main_lv_item.xml 파일의 각 View와 연결하는 과정이다. */
        val alarmName = view.findViewById<TextView>(R.id.alarmName)
        val alarmTime = view.findViewById<TextView>(R.id.alarmTime)
        val alarmDay = view.findViewById<TextView>(R.id.alarmDay)
        val group = view.findViewById<TextView>(R.id.group_on_off)
        var alarmSwitch = view.findViewById<Switch>(R.id.alarmSwitch)
        var btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

        val alarm = alarmList_info[position]
        val alarmcontext = alarm.time.split(":")

        alarmName.text = alarm.name
        alarmTime.text = alarm.time
        alarmDay.text = alarm.day


        if (alarm.group != null) {
            group.setText("▶")
        }
        if (alarm.state) {
            alarmSwitch.isChecked = true
        }

        fun printList(scroll: LinearLayout) { //스크롤뷰 내 Linear layout에 목록만들기
            scroll.removeAllViews()


            var List = alarm.group!!.count()?.let { arrayOfNulls<TextView>(it) }
            for (i in List!!.indices) { //배열의 인덱스 번호로 접근
                List[i] = TextView(context)
                List[i]!!.setText(alarm.group!![i]?.time)
                List[i]!!.setTextSize(18F)
//                List[i]!!.setText("\t\t\t" + alarm.group!![i]?.name + "\t" + alarm.group!![i]?.time)
                scroll.addView(List[i])
            }
        }

        fun getGroupIDs(): IntArray {
            var groupIDs = IntArray(alarm.group!!.size)
            for (i in groupIDs.indices) {
                groupIDs[i] = alarm.group!![i]!!.ID
            }
            return groupIDs

        }



        group.setOnClickListener { //누르면 아래에 그룹 리스트뜨게
            var subList = view.findViewById<ScrollView>(R.id.alarmSubList)
            val subListLay = view.findViewById<LinearLayout>(R.id.subListLayout)
            when (group.text) {
                "▷" -> {
                    Toast.makeText(context, "그룹이 없습니다.", Toast.LENGTH_SHORT).show()
                }
                "▶" -> {
                    group.text = "▼"
                    subList.visibility = View.VISIBLE
                    printList(subListLay)
                }
                "▼" -> {
                    group.text = "▶"
                    subList.visibility = View.GONE
                }
            }
        }


        alarmTime.setOnClickListener { //시각 누르면 수정으로 들어감
            var editIntent = Intent(context, AlarmAddActivity::class.java)
            editIntent.putExtra("alarmID", alarm.ID)
            editIntent.putExtra("alarmName", alarm.name)
            editIntent.putExtra("alarmDay", alarm.day)
            editIntent.putExtra("alarmTime", alarm.time)
            editIntent.putExtra("alarmdayFlag", alarm.dayFlag)
            if (alarm.group != null) {
                editIntent.putExtra("alarmgroup", true)
                val one = alarm.time?.split(":")
                val two = alarm.group!![0]?.time?.split(":")
                val gap =
                    (two!![0]?.toInt() * 60 + two!![1]?.toInt()) - (one!![0].toInt() * 60 + one!![1].toInt())
                editIntent.putExtra("groupInter", gap)
                editIntent.putExtra("groupLast", alarm.group!![alarm.group!!.size - 1]?.time)
                editIntent.putExtra("groupIDs", getGroupIDs())

            } else {
                editIntent.putExtra("alarmgroup", false)
            }
            editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(editIntent)

        }


        //------알람 설정 -----//

        fun setAlarm(id_: Int, time: String, dayText: String) {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmintent = Intent(context, AlarmReceiver::class.java)
            alarmintent.putExtra("alarmID", id_)
            alarmintent.putExtra("alarmTime", time)

            val pendingIntent = PendingIntent.getBroadcast(
                context, id_, alarmintent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            var cal = java.util.Calendar.getInstance()

            var Alarm_Hour = time.split(":")[0].toInt()
            var Alarm_Minute = time.split(":")[1].toInt()

            if (alarm.dayFlag) { //요일 알람
                var weekday = dayText.split(", ")


                for (i in weekday.indices) {
                    cal = java.util.Calendar.getInstance()
                    var gap = when (weekday[i]) {
                        "일" -> {
                            java.util.Calendar.SUNDAY - cal.get(java.util.Calendar.DAY_OF_WEEK)
                        }
                        "월" -> {
                            java.util.Calendar.MONDAY - cal.get(java.util.Calendar.DAY_OF_WEEK)
                        }
                        "화" -> {
                            java.util.Calendar.TUESDAY - cal.get(java.util.Calendar.DAY_OF_WEEK)
                        }
                        "수" -> {
                            java.util.Calendar.WEDNESDAY - cal.get(java.util.Calendar.DAY_OF_WEEK)
                        }
                        "목" -> {
                            java.util.Calendar.THURSDAY - cal.get(java.util.Calendar.DAY_OF_WEEK)
                        }
                        "금" -> {
                            java.util.Calendar.FRIDAY - cal.get(java.util.Calendar.DAY_OF_WEEK)
                        }
                        "토" -> {
                            java.util.Calendar.SATURDAY - cal.get(java.util.Calendar.DAY_OF_WEEK)
                        }
                        else -> {
                            0
                        }
                    }
                    if (gap < 0) {
                        cal.add(java.util.Calendar.DATE, (gap + 7))
                    } else {
                        cal.add(java.util.Calendar.DATE, gap)
                    }

                    cal.set(java.util.Calendar.HOUR_OF_DAY, Alarm_Hour)
                    cal.set(java.util.Calendar.MINUTE, Alarm_Minute)
                    cal.set(java.util.Calendar.SECOND, 0)

                    Log.d(
                        "요일 알람설정 시간확인",
                        "${cal.get(java.util.Calendar.YEAR)}년 ${cal.get(java.util.Calendar.MONTH) + 1}월 ${cal.get(
                            java.util.Calendar.DATE
                        )}일" +
                                " ${cal.get(java.util.Calendar.HOUR_OF_DAY)}시 ${cal.get(java.util.Calendar.MINUTE)}분 ${cal.get(
                                    java.util.Calendar.SECOND
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


                cal.set(2021, Alarm_Month, Alarm_Date);
                cal.set(java.util.Calendar.HOUR_OF_DAY, Alarm_Hour)
                cal.set(java.util.Calendar.MINUTE, Alarm_Minute)
                cal.set(java.util.Calendar.SECOND, 0)

                Log.d(
                    "날짜 알람설정 시간확인",
                    "$id_ :: ${cal.get(java.util.Calendar.YEAR)}년 ${cal.get(java.util.Calendar.MONTH) + 1}월 ${cal.get(
                        java.util.Calendar.DATE
                    )}일" +
                            " ${cal.get(java.util.Calendar.HOUR_OF_DAY)}시 ${cal.get(java.util.Calendar.MINUTE)}분 ${cal.get(
                                java.util.Calendar.SECOND
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

        fun offAlarm(id_: Int) {  //알람끄기
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmintent = Intent(context, AlarmReceiver::class.java)
            alarmintent.putExtra("alarmID", id_)

            val pendingIntent = PendingIntent.getBroadcast(
                context, id_, alarmintent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)

        }

        alarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            lateinit var myHelper: MainActivity.myDBHelper
            lateinit var sqlDB: SQLiteDatabase
            myHelper = MainActivity.myDBHelper(context)
            sqlDB = myHelper.writableDatabase

            val toastMessage = if (isChecked) {
                var cal = Calendar.getInstance()

                var id = alarm.ID
                var name = alarm.name
                sqlDB.execSQL(
                    "UPDATE Alarm SET state=CASE\n" +
                            "WHEN groupFlag = 1\n" +
                            "THEN 1 ELSE 1 END\n" +
                            "WHERE name = '$name';"
                )
                sqlDB.execSQL(
                    "UPDATE Alarm SET state=CASE\n" +
                            "WHEN groupFlag = 0\n" +
                            "THEN 1 ELSE 1 END\n" +
                            "WHERE id = $id;"
                )

                setAlarm(id, alarm.time, alarm.day)
                if (alarm.group != null) {
                    for (i in alarm.group!!) {
                        setAlarm(i!!.ID, i!!.time, i!!.day)
                    }
                }

                alarm.name + " 알람이 켜졌습니다."
            } else {

                var name = alarm.name
                var id = alarm.ID
                sqlDB.execSQL(
                    "UPDATE Alarm SET state=CASE\n" +
                            "WHEN groupFlag = 1\n" +
                            "THEN 0 ELSE 0 END\n" +
                            "WHERE name = '$name';"
                )
                sqlDB.execSQL(
                    "UPDATE Alarm SET state=CASE\n" +
                            "WHEN groupFlag = 0\n" +
                            "THEN 0 ELSE 0 END\n" +
                            "WHERE id = $id;"
                )

                offAlarm(id)
                Log.d("알람 종료", alarm.ID.toString())
                if (alarm.group != null) {
                    for (i in alarm.group!!) {
                        offAlarm(i!!.ID)
                        Log.d("알람 종료", i!!.ID.toString())
                    }
                }
                alarm.name + " 알림이 꺼졌습니다."
            }

            Log.d(AlarmReceiver.TAG, toastMessage)
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()

        }

        //삭제

        var layout = view.findViewById<LinearLayout>(R.id.layout)

        btnDelete.setOnClickListener {
            var dlg = AlertDialog.Builder(context)
            dlg.setTitle("삭제하시겠습니까?")
            dlg.setMessage(alarm.day + "  " + alarm.time + " 알람이 삭제됩니다.")
            dlg.setPositiveButton("삭제") { dialog, which ->
                //삭제 시 동작되는 코드
                lateinit var myHelper: MainActivity.myDBHelper
                lateinit var sqlDB: SQLiteDatabase
                myHelper = MainActivity.myDBHelper(context)
                sqlDB = myHelper.writableDatabase

                var name = alarm.name
                var id = alarm.ID
                sqlDB.execSQL("DELETE FROM alarm WHERE name = '$name' AND groupFlag = 1;")
                sqlDB.execSQL("DELETE FROM alarm WHERE id = '$id' AND groupFlag = 0;")
                layout.visibility = View.GONE

                offAlarm(id)
                Log.d("알람 삭제", alarm.ID.toString() + " 삭제")
                if (alarm.group != null) {
                    for (i in alarm.group!!) {
                        offAlarm(i!!.ID)
                        Log.d("알람 삭제", i!!.ID.toString())
                    }
                }

            }
            dlg.setNegativeButton("취소") { dialog, which ->
            }
            dlg.show()
        }



        return view


    }

    override fun getItem(position: Int): Any {
        return alarmList_info[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return alarmList_info.size
    }
}