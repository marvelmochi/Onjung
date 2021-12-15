package com.cookandroid.onjung;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PreferenceTestResultActivity extends AppCompatActivity {

    // 전역변수 선언
    ArrayList<String> recentPosition;
    ArrayList<String> spotName;
    ArrayList<String> spotLat;
    ArrayList<String> spotLon;
    ArrayList<String> spotId;

    // UserId(memberId)
    String memberId;

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;

    // 홈 위치 좌표
    Double home_lat, home_lon;

    //
    TextView courseInfo;

    //플로팅
    private Animation fab_open, fab_close, fab_rotate_open, fab_rotate_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test_result);

        // SharedPreferences Test
        SharedPreferences preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");
        System.out.println("로그: 멤버아이디 불러오기(Result): " + memberId);

        // 이전 액티비티에서 데이터 받아오기
        Intent intent = getIntent();
        recentPosition = intent.getStringArrayListExtra("recentPosition");
        home_lat = Double.parseDouble(recentPosition.get(0));
        home_lon = Double.parseDouble(recentPosition.get(1));

        System.out.println("로그: 현위치: " + recentPosition.get(0) + ", " + recentPosition.get(1));
        spotName = intent.getStringArrayListExtra("spotName");
        spotLat = intent.getStringArrayListExtra("spotLat");
        spotLon = intent.getStringArrayListExtra("spotLon");
        spotId = intent.getStringArrayListExtra("spotId");

        // 티맵 관련
        tMapView = new TMapView(this);// 티맵 뷰 생성
        tMapView.setSKTMapApiKey(API_Key); // 앱 키 등록
        // 맵뷰 기본 설정
        tMapView.setZoomLevel(15);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        // 지도 초기 위치 설정
        tMapView.setCenterPoint(home_lon, home_lat);
        tMapView.setTrackingMode(false);
        LinearLayout Tmap = (LinearLayout) findViewById(R.id.map);
        Tmap.addView(tMapView);
        // 홈 티맵포인트 생성
        TMapPoint home = new TMapPoint(home_lat, home_lon);

        ArrayList<TMapPoint> spots = new ArrayList<>();
        TMapData tmapdata = new TMapData();

        courseInfo = findViewById(R.id.courseInfo);
        String spotnameString = "";
        // 텍스트뷰에 코스 정보 표시하기
        for (int i = 0; i < spotName.size(); i += 3) {

            spotnameString += spotName.get(i) + " ";

        }
        spotnameString += "을(를) 경유하는 산책 코스입니다. \n";
        courseInfo.setText(spotnameString);
        /*
        for (int i = 0; i < spotLat.size(); i++) {
            Double dlat = Double.parseDouble(spotLat.get(i));
            Double dlon = Double.parseDouble(spotLon.get(i));
            TMapPoint spot = new TMapPoint(dlat, dlon);
            spots.add(spot);
        }*/

        // 경로 3개 --> 수정 요함 (하드코딩ed)

        for (int i = 0; i < spotLat.size(); i += 3) {

            Double lat = Double.parseDouble(spotLat.get(i));
            Double lon = Double.parseDouble(spotLon.get(i));
            TMapPoint spot = new TMapPoint(lat, lon);
            spots.add(spot);
        }

        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView.addTMapPath(tMapPolyLine);

                //distance = tMapPolyLine.getDistance();
                //courseInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                //saveInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                //courseInformation = courseInfo.getText().toString();
            }
        });

        // 툴바: 뒤로가기 버튼
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //플로팅버튼
        fab_rotate_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_open);
        fab_rotate_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_close);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);

        // 달력 프래그먼트 띄우기
        ScheduleFragment fragment1;
        fragment1 = new ScheduleFragment();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Toast.makeText(PreferenceTestResultActivity.this, "친구 목록", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PreferenceTestResultActivity.this, FriendActivity.class);
                startActivity(intent);
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Toast.makeText(PreferenceTestResultActivity.this, "산책 다이어리", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PreferenceTestResultActivity.this, DiaryActivity.class);
                startActivity(intent);
            }
        });

        // 로딩중 표시할 프로그레스 다이얼로그
        showDialog(1); // 대화상자 호출

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 20초가 지나면 다이얼로그 닫기
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        removeDialog(1);

                    }
                };

                Timer timer = new Timer();
                timer.schedule(task, 20000);
            }
        });
        thread.start();
    }

    public void okClicked(View view) {
        finish();
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

    public void anim() {

        if (isFabOpen) {
            fab.startAnimation(fab_rotate_close);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
        } else {
            fab.startAnimation(fab_rotate_open);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
        }
    }

    // 로딩중 표시할 프로그레스 다이얼로그
    @Override
    protected Dialog onCreateDialog(int id) {
        LoadingDialog dialog = new LoadingDialog(this);
        return dialog;
    }

}