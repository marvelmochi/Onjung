package com.cookandroid.onjung;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class FriendActivity extends AppCompatActivity {

    // 친구 위치 보기 다이얼로그 선언
    //Dialog friendDialog;
    Dialog searchDialog;

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;

    EditText id;
    String idCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        searchDialog = new Dialog(FriendActivity.this);
        searchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        searchDialog.setContentView(R.layout.dialog_save_walk);


        /* 친구 위치 보기
        friendDialog = new Dialog(FriendActivity.this);
        friendDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        friendDialog.setContentView(R.layout.dialog_friend_location);


        findViewById(R.id.location_btn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                friendDialog.show();
            }
        });
        */

        toastHandler = new Handler(Looper.getMainLooper());

        // 툴바
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    // 스레드 위에서 토스트 메시지를 띄우기 위한 메소드
    public void ToastMessage(String message) {

        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
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

    public void SearchFriend(View view) {
        searchDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        searchDialog.show();

        id = (EditText)findViewById(R.id.id);
    }

}
