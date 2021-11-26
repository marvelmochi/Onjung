package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toolbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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

        // 뒤로가기 버튼
    }

    public void preferenceResultbtnClicked(View view) {
        Intent intent = new Intent(this, PreferenceTestResultActivity.class);
        startActivity(intent);
    }

    public void HomebtnClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}