package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void preferencetestClicked(View view){
        Intent intent = new Intent(this, PreferenceTestActivity.class);
        startActivity(intent);
    }

}