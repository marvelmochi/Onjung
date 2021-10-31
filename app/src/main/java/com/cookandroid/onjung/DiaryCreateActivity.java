package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class DiaryCreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_create);
    }

    public void tastetestbtnClicked(View view) {
        Intent intent = new Intent(this, PreferenceTestResultActivity.class);
        startActivity(intent);
    }
}
