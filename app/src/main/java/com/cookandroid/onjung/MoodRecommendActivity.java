package com.cookandroid.onjung;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MoodRecommendActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback {

    // 취향을 분석한 산책로 추천 (평가 이후 ver.)
    // MoodRecommendaActivitiy에서 API 통신 후 경로 표시

    // API 15-2번(평가 이후) 사용 하여 경로 3개 구현

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

    // 변경 전
    ArrayList<String> spotId = new ArrayList<>();
    ArrayList<String> spotName = new ArrayList<>(); // 경유지 이름 ArrayList
    ArrayList<String> spotLat = new ArrayList<>();  // 경유지 위도 ArrayList
    ArrayList<String> spotLon = new ArrayList<>(); // 경유지 경도 ArrayList

    // 변경 후

    // 경유지 티맵 포인트 ArrayList
    ArrayList<TMapPoint> firstPoint = new ArrayList<>();
    ArrayList<TMapPoint> secondPoint = new ArrayList<>();
    ArrayList<TMapPoint> thirdPoint = new ArrayList<>();


    ValueHandler handler = new ValueHandler();
    TMapPoint home;

    TextView courseInfo;
    double distance;

    // JSON parsing에 필요한 변수 선언
    JSONArray firstTypes, secondTypes, thirdTypes;

    // spotId
    ArrayList<String> firstSpotId, secondSpotId, thirdSpotId;

    // 경로 flag
    int flag = 1;
    TextView flagText;

    //
    Button retryButton;

    // 산책 일정 저장 다이얼로그
    Dialog saveDialog;

    // DatePicker 띄울 다이얼로그
    //Dialog dateDialog;
    TextView dateText;
    EditText titleText;
    String date;
    String title;

    DatePickerDialog datePickerDialog;

    int mYear, mMonth, mDay;
    String sYear, sMonth, sDay;

    private Toast toast;

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;

    ArrayList<String> recentPosition;

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

        // 이전 액티비티에서 데이터 받아오기

        recentPosition = new ArrayList<>();

        Intent intent = getIntent();
        recentPosition = intent.getStringArrayListExtra("recentPosition");
        home_lat = Double.parseDouble(recentPosition.get(0));
        home_lon = Double.parseDouble(recentPosition.get(1));

        // ArrayList 생성
        firstSpotId = new ArrayList<>();
        secondSpotId = new ArrayList<>();
        thirdSpotId = new ArrayList<>();

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

        TMapData tmapdata = new TMapData();
        // 산책로 정보 보여줄 텍스트뷰
        courseInfo = (TextView) findViewById(R.id.courseInfo);

        // 경로 변경 버튼
        retryButton = findViewById(R.id.retry);

        //flagText = findViewById(R.id.flagText);
        //flagText.setText(flag + "번째");

        // 다이얼로그
        saveDialog = new Dialog(MoodRecommendActivity.this);
        saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveDialog.setContentView(R.layout.dialog_save_walk);

        // 다이얼로그
        // dateText = (EditText)saveDialog.findViewById(R.id.dateText);
        // 날짜, 산책 제목 받아올 변수 선언
        dateText = (TextView) saveDialog.findViewById(R.id.dateText);
        titleText = (EditText) saveDialog.findViewById(R.id.titleText);

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());


        retryButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots2, 10, new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine tMapPolyLine) {
                        tMapView.addTMapPath(tMapPolyLine);

                        //distance = tMapPolyLine.getDistance();
                        //courseInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                        //saveInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                        //courseInformation = courseInfo.getText().toString();
                    }
                });*/

                // 1->2번째
                if (flag == 1) {
                    flag += 1;
                    //flagText.setText(flag + "번째");

                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, secondPoint, 10, new TMapData.FindPathDataListenerCallback() {
                        @Override
                        public void onFindPathData(TMapPolyLine tMapPolyLine) {
                            tMapView.addTMapPath(tMapPolyLine);

                            //distance = tMapPolyLine.getDistance();
                            //courseInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                            //saveInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                            //courseInformation = courseInfo.getText().toString();
                        }
                    });
                }

                // 2->3번째
                else if (flag == 2) {
                    flag += 1;
                    //flagText.setText(flag + "번째");

                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, thirdPoint, 10, new TMapData.FindPathDataListenerCallback() {
                        @Override
                        public void onFindPathData(TMapPolyLine tMapPolyLine) {
                            tMapView.addTMapPath(tMapPolyLine);

                            //distance = tMapPolyLine.getDistance();
                            //courseInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                            //saveInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                            //courseInformation = courseInfo.getText().toString();
                        }
                    });
                }

                // 3->1번째
                else if (flag == 3) {
                    flag = 1;
                    //flagText.setText(flag + "번째");

                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, firstPoint, 10, new TMapData.FindPathDataListenerCallback() {
                        @Override
                        public void onFindPathData(TMapPolyLine tMapPolyLine) {
                            tMapView.addTMapPath(tMapPolyLine);

                            //distance = tMapPolyLine.getDistance();
                            //courseInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                            //saveInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                            //courseInformation = courseInfo.getText().toString();
                        }
                    });
                }

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

            // 첫 번째
            if (!totalObject.isNull("1")) {
                firstTypes = totalObject.getJSONArray("1");

                if (!firstTypes.isNull(0)) {
                    JSONObject firstOfFirst = firstTypes.getJSONObject(0);
                    String lat = firstOfFirst.getString("latitude");
                    Double lat_d = Double.parseDouble(lat);
                    String lon = firstOfFirst.getString("longitude");
                    Double lon_d = Double.parseDouble(lon);
                    TMapPoint tMapPoint = new TMapPoint(lat_d, lon_d);
                    firstPoint.add(tMapPoint);

                    // spotId
                    int spotId1_i = firstOfFirst.getInt("spotId");
                    String spotId1 = Integer.toString(spotId1_i);
                    firstSpotId.add(spotId1);
                }
                if (!firstTypes.isNull(1)) {
                    JSONObject secondOfFirst = firstTypes.getJSONObject(1);
                    String lat = secondOfFirst.getString("latitude");
                    Double lat_d = Double.parseDouble(lat);
                    String lon = secondOfFirst.getString("longitude");
                    Double lon_d = Double.parseDouble(lon);
                    TMapPoint tMapPoint = new TMapPoint(lat_d, lon_d);
                    secondPoint.add(tMapPoint);

                    // spotId
                    int spotId2_i = secondOfFirst.getInt("spotId");
                    String spotId2 = Integer.toString(spotId2_i);
                    secondSpotId.add(spotId2);
                }
                if (!firstTypes.isNull(2)) {
                    JSONObject thirdOfFirst = firstTypes.getJSONObject(2);
                    String lat = thirdOfFirst.getString("latitude");
                    Double lat_d = Double.parseDouble(lat);
                    String lon = thirdOfFirst.getString("longitude");
                    Double lon_d = Double.parseDouble(lon);
                    TMapPoint tMapPoint = new TMapPoint(lat_d, lon_d);
                    thirdPoint.add(tMapPoint);

                    // spotId
                    int spotId3_i = thirdOfFirst.getInt("spotId");
                    String spotId3 = Integer.toString(spotId3_i);
                    thirdSpotId.add(spotId3);
                }

            }

            // 두 번째
            if (!totalObject.isNull("2")) {
                secondTypes = totalObject.getJSONArray("2");

                if (!secondTypes.isNull(0)) {
                    JSONObject firstOfSecond = secondTypes.getJSONObject(0);
                    String lat = firstOfSecond.getString("latitude");
                    Double lat_d = Double.parseDouble(lat);
                    String lon = firstOfSecond.getString("longitude");
                    Double lon_d = Double.parseDouble(lon);
                    TMapPoint tMapPoint = new TMapPoint(lat_d, lon_d);
                    firstPoint.add(tMapPoint);

                    // spotId
                    int spotId1_i = firstOfSecond.getInt("spotId");
                    String spotId1 = Integer.toString(spotId1_i);
                    firstSpotId.add(spotId1);

                }
                if (!secondTypes.isNull(1)) {
                    JSONObject secondOfSecond = secondTypes.getJSONObject(1);
                    String lat = secondOfSecond.getString("latitude");
                    Double lat_d = Double.parseDouble(lat);
                    String lon = secondOfSecond.getString("longitude");
                    Double lon_d = Double.parseDouble(lon);
                    TMapPoint tMapPoint = new TMapPoint(lat_d, lon_d);
                    secondPoint.add(tMapPoint);

                    // spotId
                    int spotId2_i = secondOfSecond.getInt("spotId");
                    String spotId2 = Integer.toString(spotId2_i);
                    secondSpotId.add(spotId2);
                }
                if (!secondTypes.isNull(2)) {
                    JSONObject thirdOfSecond = secondTypes.getJSONObject(2);
                    String lat = thirdOfSecond.getString("latitude");
                    Double lat_d = Double.parseDouble(lat);
                    String lon = thirdOfSecond.getString("longitude");
                    Double lon_d = Double.parseDouble(lon);
                    TMapPoint tMapPoint = new TMapPoint(lat_d, lon_d);
                    thirdPoint.add(tMapPoint);

                    // spotId
                    int spotId3_i = thirdOfSecond.getInt("spotId");
                    String spotId3 = Integer.toString(spotId3_i);
                    thirdSpotId.add(spotId3);
                }
            }

            // 세 번째
            if (!totalObject.isNull("3")) {
                thirdTypes = totalObject.getJSONArray("3");

                if (!thirdTypes.isNull(0)) {
                    JSONObject firstOfThird = thirdTypes.getJSONObject(0);
                    String lat = firstOfThird.getString("latitude");
                    Double lat_d = Double.parseDouble(lat);
                    String lon = firstOfThird.getString("longitude");
                    Double lon_d = Double.parseDouble(lon);
                    TMapPoint tMapPoint = new TMapPoint(lat_d, lon_d);
                    firstPoint.add(tMapPoint);

                    // spotId
                    int spotId1_i = firstOfThird.getInt("spotId");
                    String spotId1 = Integer.toString(spotId1_i);
                    firstSpotId.add(spotId1);
                }
                if (!thirdTypes.isNull(1)) {
                    JSONObject secondOfThird = thirdTypes.getJSONObject(1);
                    String lat = secondOfThird.getString("latitude");
                    Double lat_d = Double.parseDouble(lat);
                    String lon = secondOfThird.getString("longitude");
                    Double lon_d = Double.parseDouble(lon);
                    TMapPoint tMapPoint = new TMapPoint(lat_d, lon_d);
                    secondPoint.add(tMapPoint);

                    // spotId
                    int spotId2_i = secondOfThird.getInt("spotId");
                    String spotId2 = Integer.toString(spotId2_i);
                    secondSpotId.add(spotId2);
                }
                if (!thirdTypes.isNull(2)) {
                    JSONObject thirdOfThird = thirdTypes.getJSONObject(2);
                    String lat = thirdOfThird.getString("latitude");
                    Double lat_d = Double.parseDouble(lat);
                    String lon = thirdOfThird.getString("longitude");
                    Double lon_d = Double.parseDouble(lon);
                    TMapPoint tMapPoint = new TMapPoint(lat_d, lon_d);
                    thirdPoint.add(tMapPoint);

                    // spotId
                    int spotId3_i = thirdOfThird.getInt("spotId");
                    String spotId3 = Integer.toString(spotId3_i);
                    thirdSpotId.add(spotId3);
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

            tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, firstPoint, 10, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine tMapPolyLine) {
                    tMapView.addTMapPath(tMapPolyLine);
                    distance = tMapPolyLine.getDistance();

                }
            });

            String spotnameString = "";
            // 텍스트뷰에 코스 정보 표시하기
            for (int i = 0; i < spotNameList.size(); i++) {
                if (i == spotNameList.size() - 1) {
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

    public void saveWalkClicked(View view) {
        saveDialog.show();

        //saveInfo = saveDialog.findViewById(R.id.saveText);
        //saveInfo.setText(courseInformation);

        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(MoodRecommendActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dateText.setText(year + "/" + (month + 1) + "/" + dayOfMonth);
                sYear = Integer.toString(year);
                sMonth = Integer.toString((month + 1));
                sDay = Integer.toString(dayOfMonth);
            }
        }, mYear, mMonth, mDay);

        Button noBtn = saveDialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("로그: 저장 다이얼로그 닫음");
                saveDialog.dismiss();
            }
        });

        Button saveBtn = saveDialog.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save 통신
                // 날짜, 산책 제목 받아오기

                // date String으로 변경하기
                if (sMonth.length() == 1) {
                    sMonth = "0" + sMonth;
                }
                if (sDay.length() == 1) {
                    sDay = "0" + sDay;
                }
                date = sYear + sMonth + sDay;

                title = titleText.getText().toString();
                System.out.println("로그: save 클릭");
                System.out.println("로그: date: " + date);
                System.out.println("로그: title: " + title);

                MoodRecommendActivity.HttpConnectorSaveCourse saveCourseThread = new MoodRecommendActivity.HttpConnectorSaveCourse();
                saveCourseThread.start();

            }

        });


    }

    public void dateTextClicked(View view) {
        datePickerDialog.show();
    }

    // 서버 통신부
    class HttpConnectorSaveCourse extends Thread {
        JSONObject data;
        URL url;
        HttpURLConnection conn;

        public HttpConnectorSaveCourse() {
            try {
                data = new JSONObject();
                data.put("userId", userId);
                data.put("walkDate", date);
                data.put("title", title);

                // 경로에 따라 다른 경유지 배열을 데이터에 넣어 보냄

                if (flag == 1) {
                    // jsonArray에 경유지 넣기
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < firstSpotId.size(); i++) {
                        jsonArray.put(firstSpotId.get(i));
                    }
                    data.put("wayPoint", jsonArray);
                    data.put("wayPoint", jsonArray);
                    data.put("latitude", recentPosition.get(0));
                    data.put("longitude", recentPosition.get(1));
                    System.out.println("로그: 산책로 저장 post data: " + data);
                    url = new URL("http://smwu.onjung.tk/walk");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                } else if (flag == 2) {
                    // jsonArray에 경유지 넣기
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < secondSpotId.size(); i++) {
                        jsonArray.put(secondSpotId.get(i));
                    }
                    data.put("wayPoint", jsonArray);
                    data.put("wayPoint", jsonArray);
                    data.put("latitude", recentPosition.get(0));
                    data.put("longitude", recentPosition.get(1));
                    System.out.println("로그: 산책로 저장 post data: " + data);
                    url = new URL("http://smwu.onjung.tk/walk");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                } else if (flag == 3) {
                    // jsonArray에 경유지 넣기
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < thirdSpotId.size(); i++) {
                        jsonArray.put(thirdSpotId.get(i));
                    }
                    data.put("wayPoint", jsonArray);
                    data.put("wayPoint", jsonArray);
                    data.put("latitude", recentPosition.get(0));
                    data.put("longitude", recentPosition.get(1));
                    System.out.println("로그: 산책로 저장 post data: " + data);
                    url = new URL("http://smwu.onjung.tk/walk");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 산책 코스 POST 예외 발생");
            }

        }

        @Override
        public void run() {
            try {
                conn.setDoOutput(true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                bw.write(data.toString());
                bw.flush();
                bw.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                int responseCode = conn.getResponseCode();
                System.out.println("로그: 응답 메시지: " + returnMsg);
                System.out.println("로그: responseCode: " + responseCode);

                // Toast 띄우기
                JSONObject jsonObject = new JSONObject(returnMsg);
                String detail = jsonObject.getString("detail");
                ToastMessage(detail);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 산책 코스 저장 연결 예외 발생");
            }
        }
    }


    // 스레드 위에서 토스트 메시지를 띄우기 위한 메소드
    public void ToastMessage(String message) {

        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
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