package com.cookandroid.onjung;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
implements TMapGpsManager.onLocationChangedCallback {

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

    // 현위치 좌표 전달
    double home_lat, home_lon;
    String home_lat_s, home_lon_s;

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    // T Map GPS
    TMapGpsManager tMapGPS = null;

    // 인텐트에 담아 전달할 데이터 배열 선언
    ArrayList recentPosition = new ArrayList(); // 현위치 좌표 {"위도", "경도"}

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위치정보 액세스 권한 요청
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(1000);
        tMapGPS.setMinDistance(10);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);

        tMapGPS.OpenGps();

        //만보기
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){

            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }


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
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment2, "home").commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                switch (menuItem.getItemId()) {
                    case R.id.tab1: {
                        if (fragmentManager.findFragmentByTag("schedule") != null) {
                            //프래그먼트가 존재한다면 보여준다.
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("schedule")).commit();
                        } else {
                            //getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment1).commitAllowingStateLoss();
                            fragmentManager.beginTransaction().add(R.id.main_layout, new ScheduleFragment(), "schedule").commit();
                        }
                        if (fragmentManager.findFragmentByTag("menu") != null) {
                            //다른프래그먼트가 보이면 가려준다.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("menu")).commit();
                        }
                        if (fragmentManager.findFragmentByTag("home") != null) {
                            //다른프래그먼트가 보이면 가려준다.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("home")).commit();
                        }
                        return true;
                    }

                    case R.id.tab2: {
                        if (fragmentManager.findFragmentByTag("home") != null) {
                            //if the fragment exists, show it.
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("home")).commit();
                        } else {
                            //getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment2).commitAllowingStateLoss();
                            fragmentManager.beginTransaction().add(R.id.main_layout, new HomeFragment(), "home").commit();
                        }
                        if (fragmentManager.findFragmentByTag("schedule") != null) {
                            //if the other fragment is visible, hide it.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("schedule")).commit();
                        }
                        if (fragmentManager.findFragmentByTag("menu") != null) {
                            //if the other fragment is visible, hide it.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("menu")).commit();
                        }
                        return true;
                    }

                    case R.id.tab3: {
                        if (fragmentManager.findFragmentByTag("menu") != null) {
                            //if the fragment exists, show it.
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("menu")).commit();
                        } else {
                            //getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment3).commitAllowingStateLoss();
                            fragmentManager.beginTransaction().add(R.id.main_layout, new MenuFragment(), "menu").commit();
                        }
                        if (fragmentManager.findFragmentByTag("schedule") != null) {
                            //if the other fragment is visible, hide it.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("schedule")).commit();
                        }
                        if (fragmentManager.findFragmentByTag("home") != null) {
                            //if the other fragment is visible, hide it.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("home")).commit();
                        }
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

    @Override
    public void onLocationChange(Location location) {
        // 내 위치 좌표 home에 저장
        TMapPoint home = tMapGPS.getLocation();
        home_lat = home.getLatitude();
        home_lon = home.getLongitude();
        home_lat_s = Double.toString(home_lat);
        home_lon_s = Double.toString(home_lon);
        // 배열에 현위치 담기
        recentPosition.add(home_lat_s);
        recentPosition.add(home_lon_s);

        System.out.println("로그: 현위치 좌표 " + home_lat_s + ", " + home_lon_s);
    }

    // 조건에 맞는 산책로 추천 클릭
    public void selectbtnClicked(View view) {
        Intent intent = new Intent(this, SelectActivity.class);
        intent.putStringArrayListExtra("recentPosition", recentPosition);
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
            intent.putExtra("recentPosition", recentPosition);
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



