package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PreferenceTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test);

        CheckBox mountain, park, lake, forest, river;
        CheckBox crowded, quiet, pet, scenery, show, statue, exercise, walk;

        mountain = (CheckBox) findViewById(R.id.mountain);
        park = (CheckBox) findViewById(R.id.park);
        lake = (CheckBox) findViewById(R.id.lake);
        forest = (CheckBox) findViewById(R.id.forest);
        river= (CheckBox) findViewById(R.id.river);

        crowded = (CheckBox) findViewById(R.id.crowded);
        quiet = (CheckBox) findViewById(R.id.quiet);
        pet = (CheckBox) findViewById(R.id.pet);
        scenery = (CheckBox) findViewById(R.id.scenery);
        show= (CheckBox) findViewById(R.id.show);
        statue = (CheckBox) findViewById(R.id.statue);
        exercise = (CheckBox) findViewById(R.id.exercise);
        walk = (CheckBox) findViewById(R.id.walk);

        // 툴바: 뒤로가기 버튼
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    public void preferenceResultbtnClicked(View view) {
        Intent intent = new Intent(this, PreferenceTestResultActivity.class);
        startActivity(intent);
    }

    public void HomebtnClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
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
}