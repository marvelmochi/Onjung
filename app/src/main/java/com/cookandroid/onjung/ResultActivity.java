package com.cookandroid.onjung;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class ResultActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback {

    // 조건에 맞는 산책로 추천 구현 방식 (SelectActivity에서 API 호출 후 받아온 데이터 값 ResultActivity로 넘김)

    // 첫 번째 경로: 현 위치 - API 7번 (현 위치와 반경 내에서 가장 가까운 경유지)
    // 두 번째 경로: 현 위치 - API 4번 (현 위치와 가장 가까운 경유지)
    // 세 번째 경로: 현 위치 - API 4번 (현 위치와 가장 가까운 경유지)

    // 인텐트로 액티비티 간 데이터 전달
    String myData;
    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;
    // T Map GPS
    TMapGpsManager tMapGPS = null;

    // 전역변수 선언
    ArrayList<String> recentPosition;
    TMapPoint home;

    ArrayList<String> FirstName = new ArrayList<>();
    ArrayList<String> FirstLat = new ArrayList<>();
    ArrayList<String> FirstLon = new ArrayList<>();
    ArrayList<String> FirstSpotId = new ArrayList<>();

    ArrayList<String> SecondName = new ArrayList<>();
    ArrayList<String> SecondLat = new ArrayList<>();
    ArrayList<String> SecondLon = new ArrayList<>();
    ArrayList<String> SecondSpotId = new ArrayList<>();

    ArrayList<String> ThirdName = new ArrayList<>();
    ArrayList<String> ThirdLat = new ArrayList<>();
    ArrayList<String> ThirdLon = new ArrayList<>();
    ArrayList<String> ThirdSpotId = new ArrayList<>();

    ArrayList<TMapPoint> FirstPoint = new ArrayList<>();
    ArrayList<TMapPoint> SecondPoint = new ArrayList<>();
    ArrayList<TMapPoint> ThirdPoint = new ArrayList<>();

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

    // 산책 일정 저장 다이얼로그
    Dialog saveDialog;

    // 회원 정보(유저 아이디) 불러오기 위한 SharedPreferences
    //SharedPreferences preference = getSharedPreferences("UserInfo", MODE_PRIVATE);

    String memberId;

    // DatePicker 띄울 다이얼로그
    //Dialog dateDialog;
    TextView dateText;
    EditText titleText;
    String date;
    String title;

    DatePickerDialog datePickerDialog;

    int mYear, mMonth, mDay;
    String sYear, sMonth, sDay;

    //플로팅
    private Animation fab_open, fab_close, fab_rotate_open, fab_rotate_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;

    // 경로 flag
    int flag = 1;
    //TextView flagText;

    //
    Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 이전 액티비티에서 데이터 받아오기
        Intent intent = getIntent();
        myData = intent.getStringExtra("mydata");
        System.out.println("로그: mydata: " + myData);
        recentPosition = intent.getStringArrayListExtra("recentPosition");
        System.out.println("로그: 현위치: " + recentPosition.get(0) + ", " + recentPosition.get(1));

        Double home_lat = Double.parseDouble(recentPosition.get(0));
        Double home_lon = Double.parseDouble(recentPosition.get(1));
        home = new TMapPoint(home_lat, home_lon);

        FirstName = intent.getStringArrayListExtra("FirstName");
        FirstLat = intent.getStringArrayListExtra("FirstLat");
        FirstLon = intent.getStringArrayListExtra("FirstLon");
        FirstSpotId = intent.getStringArrayListExtra("FirstSpotId");

        SecondName = intent.getStringArrayListExtra("SecondName");
        SecondLat = intent.getStringArrayListExtra("SecondLat");
        SecondLon = intent.getStringArrayListExtra("SecondLon");
        SecondSpotId = intent.getStringArrayListExtra("SecondSpotId");

        ThirdName = intent.getStringArrayListExtra("ThirdName");
        ThirdLat = intent.getStringArrayListExtra("ThirdLat");
        ThirdLon = intent.getStringArrayListExtra("ThirdLon");
        ThirdSpotId = intent.getStringArrayListExtra("ThirdSpotId");

        // TmapPoint로 만들어 배열에 담기
        for (int i = 0; i < FirstLat.size(); i++) {
            Double lat = Double.parseDouble(FirstLat.get(i));
            Double lon = Double.parseDouble(FirstLon.get(i));
            TMapPoint tMapPoint = new TMapPoint(lat, lon);
            FirstPoint.add(tMapPoint);
        }
        for (int i = 0; i < SecondLat.size(); i++) {
            Double lat = Double.parseDouble(SecondLat.get(i));
            Double lon = Double.parseDouble(SecondLon.get(i));
            TMapPoint tMapPoint = new TMapPoint(lat, lon);
            SecondPoint.add(tMapPoint);
        }
        for (int i = 0; i < ThirdLat.size(); i++) {
            Double lat = Double.parseDouble(ThirdLat.get(i));
            Double lon = Double.parseDouble(ThirdLon.get(i));
            TMapPoint tMapPoint = new TMapPoint(lat, lon);
            ThirdPoint.add(tMapPoint);
        }

        // SharedPreferences Test
        SharedPreferences preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");
        System.out.println("로그: 멤버아이디 불러오기(Result): " + memberId);

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

        // 지도 초기 위치 설정
        tMapView.setCenterPoint(home_lon, home_lat);
        tMapView.setTrackingMode(false);
        LinearLayout Tmap = (LinearLayout) findViewById(R.id.map);
        Tmap.addView(tMapView);

        // 다이얼로그
        saveDialog = new Dialog(ResultActivity.this);
        saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveDialog.setContentView(R.layout.dialog_save_walk);

        // 다이얼로그
        // dateText = (EditText)saveDialog.findViewById(R.id.dateText);
        // 날짜, 산책 제목 받아올 변수 선언
        dateText = (TextView) saveDialog.findViewById(R.id.dateText);
        titleText = (EditText) saveDialog.findViewById(R.id.titleText);

        // 경로 변경 버튼
        retryButton = findViewById(R.id.retry);

        //flagText = findViewById(R.id.flagText);
        //flagText.setText(flag + "번째");

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());

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



/*
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

 */

        //saveInfo = (TextView)findViewById(R.id.saveText);
        //saveInfo.setText(spotnameString);


        /*
        // 티맵 관련
        tMapView = new TMapView(this); // 티맵 뷰 생성
        tMapView.setSKTMapApiKey(API_Key); // 앱 키 등록

        // 맵뷰 기본 설정
        tMapView.setZoomLevel(14);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        // 트래킹모드 (화면중심을 단말의 현재위치로 이동)
        tMapView.setTrackingMode(true);

        // 리니어레이아웃에 맵뷰 추가
        LinearLayout Tmap = (LinearLayout) findViewById(R.id.map);
        Tmap.addView(tMapView);

         */

        // Request For GPS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(1000);
        tMapGPS.setMinDistance(10);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        //tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);

        tMapGPS.OpenGps();

        TMapData tmapdata = new TMapData();

        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, FirstPoint, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView.addTMapPath(tMapPolyLine);

            }
        });


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

                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, SecondPoint, 10, new TMapData.FindPathDataListenerCallback() {
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

                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, ThirdPoint, 10, new TMapData.FindPathDataListenerCallback() {
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

                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, FirstPoint, 10, new TMapData.FindPathDataListenerCallback() {
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
                Toast.makeText(ResultActivity.this, "친구 목록", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResultActivity.this, FriendActivity.class);
                startActivity(intent);
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Toast.makeText(ResultActivity.this, "산책 다이어리", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResultActivity.this, DiaryActivity.class);
                startActivity(intent);
            }
        });

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

    @Override
    public void onLocationChange(Location location) {

        /*
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());



        ArrayList<TMapPoint> spots = new ArrayList<>();
        TMapData tmapdata = new TMapData();

        for (int i = 0; i < FirstName.size(); i++) {
            TMapPoint spot = new TMapPoint(Double.parseDouble(FirstLat.get(i)), Double.parseDouble(FirstLon.get(i)));
            spots.add(spot);
        }


         */


    }


    public void saveWalkClicked(View view) {

        saveDialog.show();

        saveInfo = saveDialog.findViewById(R.id.saveText);
        saveInfo.setText(courseInformation);

        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(ResultActivity.this, new DatePickerDialog.OnDateSetListener() {
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

                HttpConnectorSaveCourse saveCourseThread = new HttpConnectorSaveCourse();
                saveCourseThread.start();

                saveDialog.dismiss();
            }

        });


    }

    public void dateTextClicked(View view) {
        datePickerDialog.show();
    }

    // 로딩중 표시할 프로그레스 다이얼로그
    @Override
    protected Dialog onCreateDialog(int id) {
        LoadingDialog dialog = new LoadingDialog(this);
        return dialog;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    /*
    public void dateClicked(View view) {
        dateDialog = new Dialog(ResultActivity.this);
        dateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dateDialog.setContentView(R.layout.dialog_date_picker);
        dateDialog.show();
    }*/

    // 스레드 위에서 토스트 메시지를 띄우기 위한 메소드
    public void ToastMessage(String message) {

        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // 서버 통신부

    class HttpConnectorSaveCourse extends Thread {
        JSONObject data;
        URL url;
        HttpURLConnection conn;

        public HttpConnectorSaveCourse() {
            try {
                data = new JSONObject();
                data.put("userId", memberId);
                data.put("walkDate", date);
                data.put("title", title);

                // 경로에 따라 다른 경유지 배열을 데이터에 넣어 보냄

                if (flag == 1) {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < FirstSpotId.size(); i++) {
                        jsonArray.put(FirstSpotId.get(i));
                    }
                    data.put("wayPoint", jsonArray);
                    data.put("latitude", recentPosition.get(0));
                    data.put("longitude", recentPosition.get(1));
                    System.out.println("로그: 산책로 저장 post data: " + data);
                    url = new URL("http://smwu.onjung.tk/walk");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                } else if (flag == 2) {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < SecondSpotId.size(); i++){
                        jsonArray.put(SecondSpotId.get(i));
                    }
                    data.put("wayPoint", jsonArray);
                    data.put("latitude", recentPosition.get(0));
                    data.put("longitude", recentPosition.get(1));
                    System.out.println("로그: 산책로 저장 post data: " + data);
                    url = new URL("http://smwu.onjung.tk/walk");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                }

                else if (flag == 3){
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < ThirdSpotId.size(); i++){
                        jsonArray.put(ThirdSpotId.get(i));
                    }
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

