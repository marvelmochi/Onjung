package com.cookandroid.onjung;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MoodRecommendActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback {

    // userId(memberId)
    String userId;
    // Using SharedPreferences
    SharedPreferences preferences;

    // 현위치 좌표 전달
    double home_lat, home_lon;
    String home_lat_s, home_lon_s;

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    // T Map GPS
    TMapGpsManager tMapGPS = null;
    TMapView tMapView = null;

    ArrayList<String> spotId = new ArrayList<>();
    ArrayList<String> spotName = new ArrayList<>(); // 경유지 이름 ArrayList
    ArrayList<String> spotLat = new ArrayList<>();  // 경유지 위도 ArrayList
    ArrayList<String> spotLon = new ArrayList<>(); // 경유지 경도 ArrayList

    ValueHandler handler = new ValueHandler();
    TMapPoint home;

    TextView courseInfo;
    double distance;

    //플로팅
    private Animation fab_open, fab_close, fab_rotate_open, fab_rotate_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_recommend);

        // Using SharedPreferences / memberId 받아오기
        preferences = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        userId = preferences.getString("memberId", "");

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(1000);
        tMapGPS.setMinDistance(10);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);

        tMapGPS.OpenGps();

        // 티맵 관련
        tMapView = new TMapView(this);// 티맵 뷰 생성
        tMapView.setSKTMapApiKey(API_Key); // 앱 키 등록
        // 맵뷰 기본 설정
        tMapView.setZoomLevel(15);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        // 산책로 정보 보여줄 텍스트뷰
        courseInfo = (TextView) findViewById(R.id.courseInfo);

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
                Toast.makeText(MoodRecommendActivity.this, "친구 목록", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MoodRecommendActivity.this, FriendActivity.class);
                startActivity(intent);
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Toast.makeText(MoodRecommendActivity.this, "산책 다이어리", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MoodRecommendActivity.this, DiaryActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onLocationChange(Location location) {
        // 내 위치 좌표 home에 저장
        home = tMapGPS.getLocation();
        home_lat = home.getLatitude();
        home_lon = home.getLongitude();
        home_lat_s = Double.toString(home_lat);
        home_lon_s = Double.toString(home_lon);


        HttpConnectorMood moodThread = new HttpConnectorMood();
        moodThread.start();

        // 지도 초기 위치 설정
        tMapView.setCenterPoint(home.getLongitude(), home.getLatitude());
        tMapView.setTrackingMode(false);
        LinearLayout Tmap = (LinearLayout) findViewById(R.id.map);
        Tmap.addView(tMapView);


    }

    // 평가 이력 있는 경우 가짜 정보 URL 통신
    class HttpConnectorMood extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {
                url = new URL("http://smwu.onjung.tk/mood?active=1&quiet=1&walkable=1&sight=1&pet=1&sightseeing=1&exercise=1&latitude="
                        + home_lat_s + "&longitude=" + home_lon_s + "&type=a&userId=" + userId);
                System.out.println("로그: 통신 URL: " + url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 응답 메시지: " + returnMsg);
                jsonParser(returnMsg);


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 통신 예외 발생");
            }
        }
    }

    public void jsonParser(String resultJson) {
        try {
            // 응답으로 받은 데이터를 JSONObject에 넣음
            JSONObject jsonObject = new JSONObject(resultJson);
            // JSONObject에서 "data" 부분을 추출
            String data = jsonObject.getString("data"); // 이건 JSONArray X
            System.out.println("로그: data: " + data);
            JSONObject totalObject = new JSONObject(data);

            ArrayList<String> number = new ArrayList<>();
            number.add("1");
            number.add("2");
            number.add("3");


            // 일단 경로 1개
            // for (int i = 0; i < number.size(); i++)

            for (int i = 0; i < number.size(); i++) {
                if (totalObject.has(number.get(i))) {
                    // JsonObject에 "1", "2", "3"이 있는지 없는지 판단

                    JSONArray datas = totalObject.getJSONArray(number.get(i));
                    String data_s = datas.get(0).toString();
                    JSONObject jsonObject1 = new JSONObject(data_s);
                    String name = jsonObject1.getString("name");
                    System.out.println("로그: name: " + name);
                    spotName.add(name);
                    int spotId_i = jsonObject1.getInt("spotId");
                    String spotId_s = Integer.toString(spotId_i);
                    spotId.add(spotId_s);
                    String lat = jsonObject1.getString("latitude");
                    spotLat.add(lat);
                    String lon = jsonObject1.getString("longitude");
                    spotLon.add(lon);
                }

            }

            Message message = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("spotName", spotName);
            bundle.putStringArrayList("spotId", spotId);
            bundle.putStringArrayList("spotLat", spotLat);
            bundle.putStringArrayList("spotLon", spotLon);
            message.setData(bundle);
            handler.sendMessage(message);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("로그: 파싱 예외 발생");
        }
    }

    class ValueHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            Bundle bundle = msg.getData();

            ArrayList<String> spotNameList = bundle.getStringArrayList("spotName");
            ArrayList<String> spotIdList = bundle.getStringArrayList("spotId");
            ArrayList<String> spotLatList = bundle.getStringArrayList("spotLat");
            ArrayList<String> spotLonList = bundle.getStringArrayList("spotLon");

            ArrayList<TMapPoint> spots = new ArrayList<>();
            TMapData tmapdata = new TMapData();

            for (int i = 0; i < spotLatList.size(); i++) {

                Double lat = Double.parseDouble(spotLatList.get(i));
                Double lon = Double.parseDouble(spotLonList.get(i));
                TMapPoint spot = new TMapPoint(lat, lon);
                spots.add(spot);

            }

            tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots, 10, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine tMapPolyLine) {
                    tMapView.addTMapPath(tMapPolyLine);
                    distance = tMapPolyLine.getDistance();

                }
            });

            String spotnameString ="";
            // 텍스트뷰에 코스 정보 표시하기
            for (int i = 0; i < spotNameList.size(); i++){
                if (i == spotNameList.size() -1 ) {
                    spotnameString += spotNameList.get(i) + "을(를) 경유하는 산책 코스입니다. \n";
                    break;
                }
                spotnameString += spotNameList.get(i) + ", ";
            }

            courseInfo.setText(spotnameString);
            //courseInfo.append("총 거리: " + distance); // km 표시로 수정 필요
            //saveInfo.append("총 거리: " + distance); // km 표시로 수정 필요
            //courseInformation = courseInfo.getText().toString();
        }
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

    // 로딩중 표시할 프로그레스 다이얼로그
    @Override
    protected Dialog onCreateDialog(int id) {
        LoadingDialog dialog = new LoadingDialog(this);
        return dialog;
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
}