package com.cookandroid.onjung;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class FriendActivity extends AppCompatActivity {

    // 친구 위치 보기 다이얼로그 선언
    //Dialog friendDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);


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
    }

}