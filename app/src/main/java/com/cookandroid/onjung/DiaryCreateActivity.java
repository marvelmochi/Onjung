package com.cookandroid.onjung;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Calendar;

public class DiaryCreateActivity extends AppCompatActivity {

    // DatePicker 띄울 다이얼로그
    TextView dateText;
    EditText titleText, contentsText;
    String date, title, contents;

    DatePickerDialog datePickerDialog;

    int mYear, mMonth, mDay;
    String sYear, sMonth, sDay;

    SharedPreferences preferences;
    String memberId;

    //DB 연결
    DbOpenHelper mDbOpenHelper;

    //확인용
    static ArrayList<String> arrayIndex =  new ArrayList<String>();
    static ArrayList<String> arrayData = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_create);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");

        //다이어리 저장 변수
        dateText = (TextView) findViewById(R.id.date_text);
        titleText = (EditText) findViewById(R.id.title_text);
        contentsText = (EditText) findViewById(R.id.contents_text);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();

        //날짜 선택
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(DiaryCreateActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dateText.setTextSize(Dimension.SP, 20);
                dateText.setText(year + "/" + (month + 1) + "/" + dayOfMonth);
                sYear = Integer.toString(year);
                sMonth = Integer.toString((month + 1));
                sDay = Integer.toString(dayOfMonth);

            }
        }, mYear, mMonth, mDay);
    }

    public void dateTextClicked(View view) {
        datePickerDialog.show();
    }

    public void saveDiaryClicked(View view) {
        // date String으로 변경하기
        if (sMonth.length() == 1) {
            sMonth = "0" + sMonth;
        }
        if (sDay.length() == 1) {
            sDay = "0" + sDay;
        }
        date = sYear + sMonth + sDay;
        title = titleText.getText().toString();
        contents = contentsText.getText().toString();

        mDbOpenHelper.open();
        mDbOpenHelper.insertColumn(memberId, date, title, contents);

        Toast.makeText(DiaryCreateActivity.this, "산책 일기를 저장했습니다.", Toast.LENGTH_SHORT).show();

        finish();
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

}
