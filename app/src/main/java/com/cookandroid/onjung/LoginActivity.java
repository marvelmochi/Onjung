package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LoginActivity extends AppCompatActivity {


    // 뒤로가기 2회로 종료 하기 위한 변수
    private long backKeyPressedTime = 0; // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private Toast toast; // 첫 번째 뒤로가기 버튼을 누를때 표시

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


    }

    public void loginClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // 뒤로가기 두 번 눌러 종료
    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            //finish();
            toast.cancel();
            // 앱 완전 종료
            ActivityCompat.finishAffinity(this);
            System.exit(0);
        }
    }

    public void signupClicked(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
}