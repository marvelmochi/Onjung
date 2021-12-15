package com.cookandroid.onjung;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // 하단 내비게이션 바 선언
    BottomNavigationView bottomNavigationView;
    ScheduleFragment fragment1;
    HomeFragment fragment2;
    MenuFragment fragment3;


    // 뒤로가기 두 번 눌러 종료
    private long backKeyPressedTime = 0;
    private Toast toast;

    // 멤버 아이디
    String memberId;

    // 평가 이력 개수
    int ratingNumber;

    TextView userNameText;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // SharedPreferences Test
        SharedPreferences preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");

        System.out.println("로그: 멤버아이디 불러오기(Main): " + memberId);



        // 평가 횟수 받아오기
        HttpConnectorRatingNumber ratingNumberThread = new HttpConnectorRatingNumber();
        ratingNumberThread.start();

        // 하단 내비게이션 바 생성
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 프래그먼트 생성 (캘린더/홈/메뉴)
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






        /*
        preferences = getSharedPreferences("UserInfo", MODE_MULTI_PROCESS);
        TextView UserId = (TextView) findViewById(R.id.UserId);
        String id = preferences.getString("id", "");
        UserId.setText(id);
         */


    }

    // 조건에 맞는 산책로 추천 클릭
    public void selectbtnClicked(View view) {
        Intent intent = new Intent(this, SelectActivity.class);
        startActivity(intent);
    }

    // 취향을 분석한 산책로 추천 클릭
    public void preferencebtnClicked(View view) {

        if (ratingNumber == 0) {
            // 평가 내역이 없을 경우
            Intent intent = new Intent(this, PreferenceTestActivity.class);
            startActivity(intent);
        } else {
            // 평가 내역이 있을 경우
            Intent intent = new Intent(this, MoodRecommendActivity.class);
            startActivity(intent);
        }

    }

    class HttpConnectorRatingNumber extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {
                System.out.println("로그: memberId: " + memberId);
                url = new URL("http://smwu.onjung.tk/rating/" + memberId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 응답 메시지: " + returnMsg);
                jsonParserRatingNumber(returnMsg);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 평가 개수 확인 예외 발생");
            }

        }
    }

    public void jsonParserRatingNumber(String resultJson) {
        try {
            // 응답으로 받은 데이터를 JSONObject에 넣음
            JSONObject dataObject = new JSONObject(resultJson);
            ratingNumber = dataObject.getInt("data");
            System.out.println("로그: data: " + ratingNumber);

            // 평가 횟수를 핸들러로 전달


        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void editInfoClicked(View view) {
        Intent intent = new Intent(this, EditUserInfoActivity.class);
        startActivity(intent);
    }

    // [메뉴] - 로그아웃 클릭

    public void logoutClicked(View view) {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // 홈에서 뒤로가기 막기
    @Override
    public void onBackPressed() {
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


}



