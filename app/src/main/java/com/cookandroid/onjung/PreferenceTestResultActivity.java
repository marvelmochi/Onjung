package com.cookandroid.onjung;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
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
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PreferenceTestResultActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback {

    // 인텐트로 액티비티 간 데이터 전달
    String myData;

    String API_Key = "l7xxa08e5b27d8fb417f9d09d0bc162c7df9";

    // T Map View
    TMapView tMapView = null;
    // T Map GPS
    TMapGpsManager tMapGPS = null;

    // 전역변수 선언
    ArrayList<String> recentPosition;
    ArrayList<String> spotName;
    ArrayList<String> spotLat;
    ArrayList<String> spotLon;
    ArrayList<String> spotId;

    // 코스 정보를 보여줄 텍스트뷰
    TextView courseInfo;
    TextView saveInfo;
    // 경유지 이름을 저장할 문자열
    String spotnameString = "";
    // 산책코스 거리를 저장할 문자열
    double distance;
    String courseDistance = Double.toString(distance);
    // 거리 km로 변환시킬 것!!
    // 산책 정보를 저장할 문자열 전체
    String courseInformation;

    String memberId;

    //플로팅
    private Animation fab_open, fab_close, fab_rotate_open, fab_rotate_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2, fab3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test_result);

        // T Map View
        tMapView = new TMapView(this);

        // API Key
        tMapView.setSKTMapApiKey(API_Key);

        // Initial Setting
        tMapView.setZoomLevel(17);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        // 트래킹모드 (화면중심을 단말의 현재위치로 이동)
        tMapView.setTrackingMode(true);

        // T Map View Using Linear Layout
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        linearLayoutTmap.addView(tMapView);

        // Request For GPS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(1000);
        tMapGPS.setMinDistance(10);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        //tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);

        tMapGPS.OpenGps();

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
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);

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
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Toast.makeText(PreferenceTestResultActivity.this, "산책 일정", Toast.LENGTH_SHORT).show();
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

        // 이전 액티비티에서 데이터 받아오기
        Intent intent = getIntent();
        myData = intent.getStringExtra("mydata");
        System.out.println("로그: mydata: " + myData);
        recentPosition = intent.getStringArrayListExtra("recentPosition");
        System.out.println("로그: 현위치: " + recentPosition.get(0) + ", " + recentPosition.get(1));
        spotName = intent.getStringArrayListExtra("spotName");
        spotLat = intent.getStringArrayListExtra("spotLat");
        spotLon = intent.getStringArrayListExtra("spotLon");
        spotId = intent.getStringArrayListExtra("spotId");

        for (int i=0; i<spotId.size(); i++) {
            System.out.println("로그: 전달받은 인텐트: " + spotId.get(i));
        }
        // 텍스트뷰에 코스 정보 표시하기
        for (int i = 0; i < spotName.size(); i++) {
            if (i == spotName.size() - 1) {
                spotnameString += spotName.get(i) + "을(를) 경유하는 산책 코스입니다. \n";

                break;
            }
            //System.out.println("로그: spotName: " + spotName.getClass().getName());
            spotnameString += spotName.get(i) + ", ";
        }

        courseInfo = (TextView) findViewById(R.id.courseInfo);
        courseInfo.setText(spotnameString);

    }

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());

        // 현 위치, 경유지 티맵포인트 설정

        TMapPoint home = new TMapPoint(Double.parseDouble(recentPosition.get(0)), Double.parseDouble(recentPosition.get(1)));


        ArrayList<TMapPoint> spots = new ArrayList<>();
        TMapData tmapdata = new TMapData();

        for (int i = 0; i < spotName.size(); i++) {
            TMapPoint spot = new TMapPoint(Double.parseDouble(spotLat.get(i)), Double.parseDouble(spotLon.get(i)));
            spots.add(spot);
        }


        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView.addTMapPath(tMapPolyLine);
                distance = tMapPolyLine.getDistance();
                courseInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                //saveInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                courseInformation = courseInfo.getText().toString();
            }
        });
    }

    // 로딩중 표시할 프로그레스 다이얼로그
    @Override
    protected Dialog onCreateDialog(int id) {
        LoadingDialog dialog = new LoadingDialog(this); // 사용자에게 보여줄 대화상자
        /*
        dialog.setTitle("산책 코스를 불러오는 중...");
        dialog.setMessage("잠시만 기다려주세요...");
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
        );

         */

        return dialog;
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
            fab3.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            isFabOpen = false;
        } else {
            fab.startAnimation(fab_rotate_open);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            isFabOpen = true;
        }
    }

}