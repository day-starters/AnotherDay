package com.example.alarmproject


import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.set_location.*


class myDBHelper(context: Context): SQLiteOpenHelper(context, "locDB", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        // 데이터베이스 없으면 생성
        db.execSQL("CREATE TABLE IF NOT EXISTS saveLocation(place TEXT PRIMARY KEY);")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
        // "위치 불러오기" 행 없으면 생성
        db.execSQL("INSERT OR IGNORE INTO saveLocation VALUES ('위치 불러오기');")

        // "위치 불러오기" 행 제외하고 모두 지우기 (테스트용)
//        db.execSQL("DELETE FROM saveLocation WHERE place != '위치 불러오기';")
    }
}
class SetLocation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.set_location)

        lateinit var myHelper : myDBHelper
        lateinit var sqlDB : SQLiteDatabase
        val spin_sido = findViewById<Spinner>(R.id.spin_sido)
        val spin_sigungu = findViewById<Spinner>(R.id.spin_sigungu)
        val spin_db = findViewById<Spinner>(R.id.spin_db)
        val selected_sido = findViewById<TextView>(R.id.selected_sido)
        val selected_sigungu = findViewById<TextView>(R.id.selected_sigungu)
        val selected_db = findViewById<TextView>(R.id.selected_db)
        val save_loc = findViewById<TextView>(R.id.save_loc)
        val radigrp = findViewById<RadioGroup>(R.id.radiogrp)
        val chooseDirect = findViewById<RadioButton>(R.id.chooseDirect)
        val chooseDirectLayout = findViewById<LinearLayout>(R.id.chooseDirectLayout)
        val chooseDBLayout = findViewById<LinearLayout>(R.id.chooseDBLayout)
        var sido = "시·도"
        var sigungu = "시·군·구"
        var loc = ""

        myHelper = myDBHelper(this)
        // 데이터베이스 비우기 (테스트용)
//        sqlDB = myHelper.writableDatabase
//        myHelper.onUpgrade(sqlDB, 1, 1)
        //////////////////////////////////
        sqlDB = myHelper.readableDatabase
        var cursor : Cursor
        cursor = sqlDB.rawQuery("SELECT place FROM saveLocation;", null)
        var dbArray  = arrayListOf<String>()
        while (cursor.moveToNext())
            dbArray.add(cursor.getString(cursor.getColumnIndex("place")))

        // 직접 지정 (chooseDirect) 또는 데이터베이스에서 불러오기 (chooseDB)
        radigrp.setOnCheckedChangeListener { buttonView, isChecked ->
            if (chooseDirect.isChecked) {
                chooseDirectLayout.visibility = View.VISIBLE
                chooseDBLayout.visibility = View.GONE

                // 위치 설정 1 (시·도)
                spin_sido.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selected_sido.text = spin_sido.selectedItem.toString()
                        sido = selected_sido.text.toString()
                        var sigunguArr = arrayOf("시·군·구")

                        when (sido) {
                            "강원도" -> sigunguArr = resources.getStringArray(R.array.gangwon)
                            "경기도" -> sigunguArr = resources.getStringArray(R.array.gyeonggi)
                            "경상남도" -> sigunguArr = resources.getStringArray(R.array.gyeongsangnamdo)
                            "경상북도" -> sigunguArr = resources.getStringArray(R.array.gyeongsangbukdo)
                            "광주광역시" -> sigunguArr = resources.getStringArray(R.array.gwangju)
                            "대구광역시" -> sigunguArr = resources.getStringArray(R.array.daegu)
                            "대전광역시" -> sigunguArr = resources.getStringArray(R.array.daejeon)
                            "부산광역시" -> sigunguArr = resources.getStringArray(R.array.busan)
                            "서울특별시" -> sigunguArr = resources.getStringArray(R.array.seoul)
                            "세종특별자치시" -> sigunguArr = resources.getStringArray(R.array.sejong)
                            "울산광역시" -> sigunguArr = resources.getStringArray(R.array.ulsan)
                            "인천광역시" -> sigunguArr = resources.getStringArray(R.array.incheon)
                            "전라남도" -> sigunguArr = resources.getStringArray(R.array.jeollanamdo)
                            "전라북도" -> sigunguArr = resources.getStringArray(R.array.jeollabukdo)
                            "제주특별자치도" -> sigunguArr = resources.getStringArray(R.array.jeju)
                            "충청남도" -> sigunguArr = resources.getStringArray(R.array.chuncheongnamdo)
                            "충청북도" -> sigunguArr = resources.getStringArray(R.array.chungcheongbukdo)
                        }

                        val adapter = ArrayAdapter(
                            applicationContext,
                            android.R.layout.simple_dropdown_item_1line,
                            sigunguArr
                        )
                        spin_sigungu.adapter = adapter
                        loc = "$sido $sigungu"
                    }
                }

                // 위치 설정 2 (시·군·구)
                spin_sigungu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        sigungu = spin_sigungu.selectedItem.toString()
                        selected_sigungu.text = sigungu
                        save_loc.isEnabled = selected_sido.text != "시·도" && selected_sigungu.text != "시·군·구"

                        if (save_loc.isEnabled) save_loc.setBackgroundColor(Color.parseColor("#FFD8D8"))
                        else save_loc.setBackgroundColor(Color.parseColor("#BDBDBD"))
                        loc = "$sido $sigungu"
                    }
                }
            }
            else {
                chooseDirectLayout.visibility = View.GONE
                chooseDBLayout.visibility = View.VISIBLE

                spin_db.adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dbArray)

                spin_db.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val db = spin_db.selectedItem.toString()
                        selected_db.text = db
                        save_loc.isEnabled = selected_db.text != "위치 불러오기"

                        if (save_loc.isEnabled) save_loc.setBackgroundColor(Color.parseColor("#FFD8D8"))
                        else save_loc.setBackgroundColor(Color.parseColor("#BDBDBD"))
                        loc = db
                    }
                }
            }
        }

        // 위치 저장 & 액티비티 종료
        save_loc.setOnClickListener {
            myHelper = myDBHelper(this)
            sqlDB = myHelper.writableDatabase
            myHelper.onUpgrade(sqlDB, 1, 1)
            sqlDB.execSQL("INSERT OR IGNORE INTO saveLocation VALUES ('$loc');")


            val pref = this.getSharedPreferences("MessageKey", 0)
            val editor = pref.edit()
            editor.putString("MessageKey", loc).apply()

            var intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("Location", loc)
            setResult(200, intent)
            finish()
        }

        // 위치 목록 초기화
        delete_loc.setOnClickListener {

            var dlg = AlertDialog.Builder(this)
            dlg.setTitle("위치 목록을 초기화 하시겠습니까?")
            dlg.setMessage("")

            dlg.setPositiveButton("예") { dialog, which ->
                myHelper = myDBHelper(this)
                sqlDB = myHelper.writableDatabase
                sqlDB.execSQL("DELETE FROM saveLocation")
                dbArray  = arrayListOf("위치 불러오기")
                spin_db.adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dbArray)
                Toast.makeText(this, "위치 목록 초기화 완료", Toast.LENGTH_SHORT).show()
            }

            dlg.setNegativeButton("아니요") { dialog, which ->
            }
            dlg.show()
        }
    }
}