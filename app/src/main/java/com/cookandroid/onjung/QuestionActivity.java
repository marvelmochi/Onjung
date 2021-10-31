package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
    }

    public void resultbtnClicked(View view) {
        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
    }
}