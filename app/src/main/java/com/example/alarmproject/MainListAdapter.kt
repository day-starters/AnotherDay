package com.example.alarmproject

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

import java.net.URL

class MainListAdapter (val context: Context, val news_List: ArrayList<MainActivity.news_item>) : BaseAdapter() {

    //xml 파일의 View와 데이터를 연결하는 핵심 역할을 하는 메소드
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        // LayoutInflater는 item을 Adapter에서 사용할 View로 부풀려주는(inflate) 역할
        val view: View = LayoutInflater.from(context).inflate(R.layout.news_list, null)

        // view를 news_list.xml 파일의 각 View와 연결하는 과정
        val newscount = view.findViewById<TextView>(R.id.count)

        val newssearch_title = view.findViewById<TextView>(R.id.search_title)
        val newssearch_traffic = view.findViewById<TextView>(R.id.search_traffic)

        val newsnews_title = view.findViewById<TextView>(R.id.news_title)
        val newsnews_where = view.findViewById<TextView>(R.id.news_where)
        val newsnews_time = view.findViewById<TextView>(R.id.news_time)

        val newsimageView = view.findViewById<ImageView>(R.id.imageView)

        // ArrayList<news>의 변수 news 이미지와 데이터를 ImageView와 TextView에 담는다.
        val news = news_List[position]

        newscount.text = news.Rank
        newssearch_title.text = news.item_title
        newssearch_traffic.text = news.item_traffic

        // 뉴스 제목이 길어지면 다른 오브젝트들을 가리게 돼서 글자수 제한을 거는 코드임
        if(news.item_news_title.length > 16) {
            if(news.item_news_title.length >= 36)
                newsnews_title.text = news.item_news_title.substring(0..16)+"\n"+news.item_news_title.substring(17..35)
            else
                newsnews_title.text = news.item_news_title.substring(0..16)+"\n"+news.item_news_title.substring(17..(news.item_news_title.length-1))
        }
        else {
            try {
                newsnews_title.text = news.item_news_title.substring(0..16)+"\n"+news.item_news_title.substring(17..(news.item_news_title.length-1))
            } catch (e: IndexOutOfBoundsException) {
                newsnews_title.text = news.item_news_title.substring(0..15)+"\n"+news.item_news_title.substring(16..(news.item_news_title.length-1))
            }
        }

        newsnews_where.text = news.item_news_source
        newsnews_time.text = news.News_Day_Hour

        // news.nowtime, news.times 로 몇 시간 전으로 나오게 처리하는 코드
        // news.nowtime값이 avd상과 폰에서 다르게 나옴 avd는 +9시간을 해줘야하고 폰은 +9시간이 이미 되어있음
        var nowtime = news.nowtime
        var times = news.News_Day_Hour
        // Log.e("asdsdd", nowtime)
        // Log.e("asdsdd", times)

        SetDate(nowtime, times, newsnews_time)

        // 이미지-비트맵 처리 ver-3 - 속도 제일 빠름 이미지 화질 중간 - (avd에선 많은 렉 / 폰에서는 렉 없음)
        Glide.with(view).load(news.item_img).into(newsimageView)

        return view
    }

    // 해당 위치의 item을 메소드이다. Int 형식으로 된 position을 파라미터로 갖는다. 예를 들어 1번째 Dog item을 선택하고 싶으면 코드에서 getItem(0)과 같이 쓸 수 있을 것이다.
    override fun getItem(position: Int): Any {
        return news_List[position]
    }

    // 해당 위치의 item id를 반환하는 메소드이다. 이 예제에서는 실질적으로 id가 필요하지 않아서 0을 반환하도록 설정했다.
    override fun getItemId(position: Int): Long {
        return 0
    }

    // ListView에 속한 item의 전체 수를 반환한다.
    override fun getCount(): Int {
        return news_List.size
    }

    fun SetDate(nowtime:String, times:String, newsnews_time:TextView) {

        var nowDate = nowtime.split(":")[0].toInt()
        var nowTime = nowtime.split(":")[1].toInt()
        var newsDate = times.split(":")[0].toInt()
        var newsTime = times.split(":")[1].toInt()

        if(nowDate == newsDate) { // 날짜가 같을 때
            newsnews_time.text = (nowTime-newsTime).toString() + "시간 전"
        }
        else if(nowDate-1 == newsDate) {
            if(nowTime - newsTime < 0)
                newsnews_time.text = (nowTime+24-newsTime).toString() + "시간 전"
            else
                newsnews_time.text = "1일 전"
        }
        else if(nowDate-2 == newsDate)
            newsnews_time.text = "2일 전"
    }
}

