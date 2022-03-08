package com.cookandroid.onjung;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

//@RequiresApi(api = Build.VERSION_CODES.N)
public class DiaryActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences preferences;

    String memberId;

    //다이어리 리스트
    private ListView listview = null;
    private ListViewAdapter adapter = null;

    private ArrayList<DiaryItem> diary = null;

    //DB 연결
    DbOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");

        //산책 다이어리
        listview = (ListView) findViewById(R.id.diarylist);
        diary = new ArrayList<>();

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();

        showDatabase("date");

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DiaryReadActivity.class);
                /* putExtra의 첫 값은 식별 태그, 뒤에는 다음 화면에 넘길 값 */
                intent.putExtra("index", diary.get(position).getIndex());
                intent.putExtra("date", diary.get(position).getDate());
                intent.putExtra("title", diary.get(position).getTitle());
                intent.putExtra("contents", diary.get(position).getContents());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    public void diaryCreateClicked(View view) {
        Intent intent = new Intent(this, DiaryCreateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                // 액티비티 이동
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //다이어리 정렬
    public void showDatabase(String sort){
        Cursor iCursor = mDbOpenHelper.sortColumn(memberId, sort);
        Log.d("showDatabase", "DB Size: " + iCursor.getCount());

        adapter = new ListViewAdapter();
        diary.clear();

        while(iCursor.moveToNext()){
            Integer tempIndex = iCursor.getInt(iCursor.getColumnIndex("_id"));
            String tempDate = iCursor.getString(iCursor.getColumnIndex("date"));
            String tempTitle = iCursor.getString(iCursor.getColumnIndex("title"));
            String tempContents = iCursor.getString(iCursor.getColumnIndex("contents"));

            String year_s = tempDate.substring(0,4);
            String month_s = tempDate.substring(4,6);
            String day_s = tempDate.substring(6,8);

            String date = year_s + "/" + month_s + "/" + day_s;

            adapter.addItem(new DiaryItem(tempIndex, date, tempTitle, tempContents));
            diary.add(new DiaryItem(tempIndex, date, tempTitle, tempContents));
        }
        //System.out.println(arrayData);
        listview.setAdapter(adapter);
    }

    //다이어리 리스트뷰
    public class DiaryItem {
        /* 아이템의 정보를 담기 위한 클래스 */

        Integer index;
        String date;
        String title;
        String contents;

        public DiaryItem(Integer index, String date, String title, String contents) {
            this.index = index;
            this.date = date;
            this.title = title;
            this.contents = contents;
        }

        public Integer getIndex() {
            return index;
        }
        public void setIndex(String index) {
            this.date = index;
        }

        public String getDate() {
            return date;
        }
        public void setDate(String date) {
            this.date = date;
        }

        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }

        public String getContents() {
            return contents;
        }
        public void setContents(String contents) {
            this.contents = contents;
        }
    }

    public class ListViewAdapter extends BaseAdapter {
        ArrayList<DiaryItem> items = new ArrayList<DiaryItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(DiaryItem item) { items.add(item); }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final Context context = viewGroup.getContext();
            final DiaryItem title = items.get(position);

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_diary, viewGroup, false);

            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            TextView listitem_date = (TextView) convertView.findViewById(R.id.listitem_date);
            TextView listitem_title = (TextView) convertView.findViewById(R.id.listitem_title);

            listitem_date.setText(items.get(position).getDate());
            listitem_title.setText(items.get(position).getTitle());

            return convertView;  //뷰 객체 반환
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDatabase("date");

    }
}


