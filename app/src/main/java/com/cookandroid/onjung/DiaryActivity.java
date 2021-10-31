package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

@RequiresApi(api = Build.VERSION_CODES.N)
public class DiaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
    }

    public void diaryCreateClicked(View view) {
        Intent intent = new Intent(this, DiaryCreateActivity.class);
        startActivity(intent);
    }

    public void diaryReadClicked(View view) {
        Intent intent = new Intent(this, DiaryReadActivity.class);
        startActivity(intent);
    }
}


