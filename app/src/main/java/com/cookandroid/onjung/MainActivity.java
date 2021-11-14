package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // 하단 내비게이션 바 선언
    BottomNavigationView bottomNavigationView;
    ScheduleFragment fragment1;
    HomeFragment fragment2;
    MenuFragment fragment3;

    // 앱 종료 다이얼로그 선언
    //Dialog quitDialog;

    // 뒤로가기 두 번 눌러 종료
    private long backKeyPressedTime = 0;
    private Toast toast;


    // 산책일정 추가 (동적뷰)
    LinearLayout scheduleView; // 동적 뷰(일정) 추가할 부모 레이아웃


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 하단 내비게이션 바 생성
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        fragment1 = new ScheduleFragment();
        fragment2 = new HomeFragment();
        fragment3 = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment2).commitAllowingStateLoss();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.tab1: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment1)
                                .commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.tab2: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment2)
                                .commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.tab3: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment3)
                                .commitAllowingStateLoss();
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });
        //
        /* 앱 종료 다이얼로그 실패..
        quitDialog = new Dialog(MainActivity.this);
        quitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        quitDialog.setContentView(R.layout.dialog_quit);
        */


    }

    // 조건에 맞는 산책로 추천 클릭
    public void selectbtnClicked(View view) {
        Intent intent = new Intent(this, SelectActivity.class);
        startActivity(intent);
    }

    // 취향을 분석한 산책로 추천 클릭
    public void preferencebtnClicked(View view) {
        Intent intent = new Intent(this, PreferenceTestResultActivity.class);
        startActivity(intent);
    }

    // 하단 내비게이션 -> 캘린더 프래그먼트 클릭
    public void calendarClicked(View view) {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment1)
                .commitAllowingStateLoss();
    }

    // [메뉴] - 산책 다이어리 클릭
    public void diaryClicked(View view) {
        Intent intent = new Intent(this, DiaryActivity.class);
        startActivity(intent);
    }

    // 다이어리 작성 클릭
    public void diaryCreateClicked(View view) {
        Intent intent = new Intent(this, DiaryCreateActivity.class);
        startActivity(intent);
    }

    // [메뉴] - 친구 목록 클릭
    public void friendbtnClicked(View view) {
        Intent intent = new Intent(this, FriendActivity.class);
        startActivity(intent);
    }

    // [메뉴] - 회원정보 수정 클릭
    public void editInfoClicked(View view){
        Intent intent = new Intent(this, EditUserInfoActivity.class);
        startActivity(intent);
    }

    // [메뉴] - 로그아웃 클릭

    public void logoutClicked(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    // 홈에서 뒤로가기 막기
    @Override
    public void onBackPressed(){
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
        /* 앱 종료 다이얼로그 -> 실패..

        findViewById(android.R.id.home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitDialog.show();

                Button noBtn = quitDialog.findViewById(R.id.quitNo);
                noBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        quitDialog.dismiss();
                    }
                });
                quitDialog.findViewById(R.id.quitYes).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        finish();
                    }
                });
            }
        });
        */

    }


    public void satisfactionClicked(View view) {
        Intent intent = new Intent(this, SatisfactionScoreActivity.class);
        startActivity(intent);
    }

    public void addScheduleClicked(View view) {
        scheduleView = findViewById(R.id.scheduleView);
        TextView schedule = new TextView(getApplicationContext());
        schedule.setText("산책 일정 제목");
        schedule.setTextSize(20);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(10, 10, 10, 10);
        schedule.setPadding(20, 30, 20, 30);


        schedule.setBackground(ContextCompat.getDrawable(this, R.drawable.border_line));
        schedule.setLayoutParams(param);
        scheduleView.addView(schedule);
    }
}



