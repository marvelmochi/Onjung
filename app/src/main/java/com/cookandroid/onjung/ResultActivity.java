package com.cookandroid.onjung;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
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
import com.skt.Tmap.TMapMarkerItem;
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

    // <T Map 관련>
    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;
    TMapView tMapView_x = null;
    // T Map GPS
    TMapGpsManager tMapGPS = null;
    TMapData tmapdata;

    // <경로 관련 변수 선언>
    ArrayList<String> recentPosition = new ArrayList<>();
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

    // 편의사항 관련 데이터

    ArrayList<String> convName_toilet = new ArrayList<>();
    ArrayList<String> convLat_toilet = new ArrayList<>();
    ArrayList<String> convLon_toilet = new ArrayList<>();
    ArrayList<String> convName_bell = new ArrayList<>();
    ArrayList<String> convLat_bell = new ArrayList<>();
    ArrayList<String> convLon_bell = new ArrayList<>();
    ArrayList<String> convName_trash = new ArrayList<>();
    ArrayList<String> convLat_trash = new ArrayList<>();
    ArrayList<String> convLon_trash = new ArrayList<>();
    ArrayList<String> convName_exercise = new ArrayList<>();
    ArrayList<String> convLat_exercise = new ArrayList<>();
    ArrayList<String> convLon_exercise = new ArrayList<>();
    ArrayList<String> convName_light = new ArrayList<>();
    ArrayList<String> convLat_light = new ArrayList<>();
    ArrayList<String> convLon_light = new ArrayList<>();

    ArrayList<TMapPoint> convPoint_toilet = new ArrayList<>();
    ArrayList<TMapPoint> convPoint_bell = new ArrayList<>();
    ArrayList<TMapPoint> convPoint_trash = new ArrayList<>();
    ArrayList<TMapPoint> convPoint_exercise = new ArrayList<>();
    ArrayList<TMapPoint> convPoint_light = new ArrayList<>();

    // <코스 정보 보여주기>
    TextView courseInfo; // 액티비티에 코스 정보를 보여줄 텍스트뷰
    TextView saveInfo; // 산책 일정 저장 다이얼로그에 띄울 코스 정보 텍스트뷰
    // 경로 정보를 담을 StringBuffer
    StringBuffer courseInfo1 = new StringBuffer();
    StringBuffer courseInfo2 = new StringBuffer();
    StringBuffer courseInfo3 = new StringBuffer();
    String spotName1 = ""; // 경유지 이름을 저장할 문자열
    String spotName2 = "";
    String spotName3 = "";
    String distance1; // 산책코스 거리를 저장할 문자열
    String distance2;
    String distance3;
    String totalTime1 = ""; // 소요시간을 저장할 문자열
    String totalTime2 = "";
    String totalTime3 = "";
    // 산책로 정보값 전달할 핸들러
    CourseHandler1 courseHandler1 = new CourseHandler1();
    Message message1 = courseHandler1.obtainMessage();
    Bundle bundle1 = new Bundle();

    int click = 1;

    // 회원 정보(유저 아이디) 불러오기 위한 SharedPreferences
    //SharedPreferences preference = getSharedPreferences("UserInfo", MODE_PRIVATE);
    String memberId;

    // <산책 일정 저장 다이얼로그>
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

    // 아이콘 표시를 위한 컨텍스트 선언
    Context context;

    int flag_conv;

    ArrayList<String> timeList = new ArrayList<>();

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        /*
        recentPosition = intent.getStringArrayListExtra("recentPosition");
        System.out.println("로그: 현위치: " + recentPosition.get(0) + ", " + recentPosition.get(1));
         */
        preferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String home_lat_s = preferences.getString("home_lat", "");
        String home_lon_s = preferences.getString("home_lon", "");
        recentPosition.add(home_lat_s);
        recentPosition.add(home_lon_s);

        Double home_lat = Double.parseDouble(recentPosition.get(0));
        Double home_lon = Double.parseDouble(recentPosition.get(1));
        home = new TMapPoint(home_lat, home_lon);

        // 이전 액티비티에서 데이터 받아오기
        Intent intent = getIntent();
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

        convLat_toilet = intent.getStringArrayListExtra("convLat_toilet");
        convLon_toilet = intent.getStringArrayListExtra("convLon_toilet");
        convLat_bell = intent.getStringArrayListExtra("convLat_bell");
        convLon_bell = intent.getStringArrayListExtra("convLon_bell");
        convLat_trash = intent.getStringArrayListExtra("convLat_trash");
        convLon_trash = intent.getStringArrayListExtra("convLon_trash");
        convLat_exercise = intent.getStringArrayListExtra("convLat_exercise");
        convLon_exercise = intent.getStringArrayListExtra("convLon_exercise");
        convLat_light = intent.getStringArrayListExtra("convLat_light");
        convLon_light = intent.getStringArrayListExtra("convLon_light");

        convName_toilet = intent.getStringArrayListExtra("convName_toilet");
        convName_bell = intent.getStringArrayListExtra("convName_bell");
        convName_trash = intent.getStringArrayListExtra("convName_trash");
        convName_exercise = intent.getStringArrayListExtra("convName_exercise");
        convName_light = intent.getStringArrayListExtra("convName_light");

        flag_conv = intent.getIntExtra("flag_conv", 0);

        timeList = intent.getStringArrayListExtra("timeList");
        System.out.println("로그: timeList(보내기 후): " + timeList);
        timeConverter(timeList);

        context = this; // Bitmap icon 설정을 위해 필요

        // 산책로 정보 보여줄 텍스트뷰 접근
        courseInfo = (TextView) findViewById(R.id.courseInfo);

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

        tMapView_x = new TMapView(this);


        // 맵뷰 기본 설정
        tMapView.setZoomLevel(15);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        Bitmap marker_start = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_start);
        tMapView.setTMapPathIcon(marker_start, marker_start); // 출발지 아이콘 설정

        // 지도 초기 위치 설정
        tMapView.setCenterPoint(home_lon, home_lat);
        tMapView.setTrackingMode(false);
        LinearLayout Tmap = (LinearLayout) findViewById(R.id.map);
        Tmap.addView(tMapView);
        /*
        // 트래킹모드 (화면중심을 단말의 현재위치로 이동)
        tMapView.setTrackingMode(true);
         */

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

        // <마커 표시 관련>
        if (flag_conv == 1) {
            // toilet
            if (convLat_toilet.size() > 0) {
                for (int i = 0; i < convLat_toilet.size(); i++) {
                    TMapPoint tMapPoint = new TMapPoint(Double.parseDouble(convLat_toilet.get(i)),
                            Double.parseDouble(convLon_toilet.get(i)));
                    convPoint_toilet.add(tMapPoint);
                }
                for (int i = 0; i < convPoint_toilet.size(); i++) {
                    TMapMarkerItem marker = new TMapMarkerItem();
                    marker.setTMapPoint(convPoint_toilet.get(i));
                    marker.setName(convName_toilet.get(i));
                    marker.setVisible(TMapMarkerItem.VISIBLE);
                    marker.setPosition((float) 0.5, (float) 1.0);// 마커의 중심점을 하단, 중앙으로 설정
                    // 마커에 설정할 아이콘 필요
                    Bitmap marker_toilet = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_toilet);
                    marker.setIcon(marker_toilet);
                    tMapView.addMarkerItem(convName_toilet.get(i), marker);
                }
            }
            // bell
            if (convLat_bell.size() > 0) {
                for (int i = 0; i < convLat_bell.size(); i++) {
                    TMapPoint tMapPoint = new TMapPoint(Double.parseDouble(convLat_bell.get(i)),
                            Double.parseDouble(convLon_bell.get(i)));
                    convPoint_bell.add(tMapPoint);
                }
                for (int i = 0; i < convPoint_bell.size(); i++) {
                    TMapMarkerItem marker = new TMapMarkerItem();
                    marker.setTMapPoint(convPoint_bell.get(i));
                    marker.setName(convName_bell.get(i));
                    marker.setVisible(TMapMarkerItem.VISIBLE);
                    marker.setPosition((float) 0.5, (float) 1.0);// 마커의 중심점을 하단, 중앙으로 설정
                    // 마커에 설정할 아이콘 필요
                    Bitmap marker_bell = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bell);
                    marker.setIcon(marker_bell);
                    tMapView.addMarkerItem(convName_bell.get(i), marker);
                }
            }
            // trash
            if (convLat_trash.size() > 0) {
                for (int i = 0; i < convLat_trash.size(); i++) {
                    TMapPoint tMapPoint = new TMapPoint(Double.parseDouble(convLat_trash.get(i)),
                            Double.parseDouble(convLon_trash.get(i)));
                    convPoint_trash.add(tMapPoint);
                }
                for (int i = 0; i < convPoint_trash.size(); i++) {
                    TMapMarkerItem marker = new TMapMarkerItem();
                    marker.setTMapPoint(convPoint_trash.get(i));
                    marker.setName(convName_trash.get(i));
                    marker.setVisible(TMapMarkerItem.VISIBLE);
                    marker.setPosition((float) 0.5, (float) 1.0);// 마커의 중심점을 하단, 중앙으로 설정
                    // 마커에 설정할 아이콘 필요
                    Bitmap marker_trash = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_trash);
                    marker.setIcon(marker_trash);
                    tMapView.addMarkerItem(convName_trash.get(i), marker);
                }
            }
            // exercise
            if (convLat_exercise.size() > 0) {
                for (int i = 0; i < convLat_exercise.size(); i++) {
                    TMapPoint tMapPoint = new TMapPoint(Double.parseDouble(convLat_exercise.get(i)),
                            Double.parseDouble(convLon_exercise.get(i)));
                    convPoint_exercise.add(tMapPoint);
                }
                for (int i = 0; i < convPoint_exercise.size(); i++) {
                    TMapMarkerItem marker = new TMapMarkerItem();
                    marker.setTMapPoint(convPoint_exercise.get(i));
                    marker.setName(convName_exercise.get(i));
                    marker.setVisible(TMapMarkerItem.VISIBLE);
                    marker.setPosition((float) 0.5, (float) 1.0);// 마커의 중심점을 하단, 중앙으로 설정
                    // 마커에 설정할 아이콘 필요
                    Bitmap marker_exercise = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_exercise);
                    marker.setIcon(marker_exercise);
                    tMapView.addMarkerItem(convName_exercise.get(i), marker);
                }
            }
            // light
            if (convLat_light.size() > 0) {
                for (int i = 0; i < convLat_light.size(); i++) {
                    TMapPoint tMapPoint = new TMapPoint(Double.parseDouble(convLat_light.get(i)),
                            Double.parseDouble(convLon_light.get(i)));
                    convPoint_light.add(tMapPoint);
                }
                for (int i = 0; i < convPoint_light.size(); i++) {
                    TMapMarkerItem marker = new TMapMarkerItem();
                    marker.setTMapPoint(convPoint_light.get(i));
                    marker.setName(convName_light.get(i));
                    marker.setVisible(TMapMarkerItem.VISIBLE);
                    marker.setPosition((float) 0.5, (float) 1.0);// 마커의 중심점을 하단, 중앙으로 설정
                    // 마커에 설정할 아이콘 필요
                    Bitmap marker_light = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_light);
                    marker.setIcon(marker_light);
                    tMapView.addMarkerItem(convName_light.get(i), marker);
                }
            }
        }

        //HttpConnectorTime timeThread = new HttpConnectorTime();
        //timeThread.start();

        TMapData tMapData2 = new TMapData();
        tMapData2.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, SecondPoint, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView_x.addTMapPath(tMapPolyLine);
                double distance = tMapPolyLine.getDistance() * 0.001;
                distance2 = Double.toString(Math.round(distance * 100) / 100.0);

            }
        });

        TMapData tMapData3 = new TMapData();
        tMapData3.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, ThirdPoint, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView_x.addTMapPath(tMapPolyLine);
                double distance = tMapPolyLine.getDistance() * 0.001;
                distance3 = Double.toString(Math.round(distance * 100) / 100.0);

            }
        });


        tmapdata = new TMapData();
        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, FirstPoint, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView.addTMapPath(tMapPolyLine);
                double distance = tMapPolyLine.getDistance() * 0.001;
                distance1 = Double.toString(Math.round(distance * 100) / 100.0);

                for (int i = 0; i < FirstName.size(); i++) {
                    if (i == FirstName.size() - 1) {

                        spotName1 += FirstName.get(i);
                        break;
                    }
                    spotName1 += FirstName.get(i) + ", ";
                }
                courseInfo1.append(spotName1);
                courseInfo1.append("을(를) 경유하는 산책로입니다. \n총 거리: ");
                courseInfo1.append(distance1);
                courseInfo1.append("km\n");
                courseInfo1.append("예상 소요시간: " + totalTime1);

                //Message message = courseHandler.obtainMessage();
                //Bundle bundle = new Bundle();
                bundle1.putString("courseInfo1", String.valueOf(courseInfo1));
                message1.setData(bundle1);
                courseHandler1.sendMessage(message1);

                //courseInfo.setText(courseInfo1);

            }

        });


        //

        retryButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                //한 바퀴 돌기 전
                if (click < 3) {
                    // 1->2번째
                    if (flag == 1) {
                        spotName2 = "";
                        flag += 1;
                        //flagText.setText(flag + "번째");

                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, SecondPoint, 10, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapView.addTMapPath(tMapPolyLine);
                                //System.out.println("로그: 경로2 거리: "+ tMapPolyLine.getDistance());
                                //double distance = tMapPolyLine.getDistance() * 0.001;
                                //distance2 = Double.toString(distance);

                            }
                        });

                        for (int i = 0; i < SecondName.size(); i++) {
                            if (i == SecondName.size() - 1) {
                                spotName2 += SecondName.get(i);
                                break;
                            }
                            spotName2 += SecondName.get(i) + ", ";
                        }
                        courseInfo2.append(spotName2);
                        courseInfo2.append("을(를) 경유하는 산책로입니다. \n총 거리: ");
                        courseInfo2.append(distance2);
                        courseInfo2.append("km\n");
                        courseInfo2.append("예상 소요시간: " + totalTime2);
                        //bundle2.putString("courseInfo2", String.valueOf(courseInfo2));
                        //message2.setData(bundle2);
                        //courseHandler2.sendMessage(message2);

                        courseInfo.setText(courseInfo2);
                    }

                    // 2->3번째
                    else if (flag == 2) {

                        flag += 1;
                        spotName3 = "";
                        //flagText.setText(flag + "번째");

                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, ThirdPoint, 10, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapView.addTMapPath(tMapPolyLine);
                                //System.out.println("로그: 경로3 거리: "+ tMapPolyLine.getDistance());
                                //double distance = tMapPolyLine.getDistance() * 0.001;
                                //distance3 = Double.toString(distance);

                            }
                        });

                        for (int i = 0; i < ThirdName.size(); i++) {
                            if (i == ThirdName.size() - 1) {
                                spotName3 += ThirdName.get(i);
                                break;
                            }
                            spotName3 += ThirdName.get(i) + ", ";
                        }
                        courseInfo3.append(spotName3);
                        courseInfo3.append("을(를) 경유하는 산책로입니다. \n총 거리: ");
                        courseInfo3.append(distance3);
                        courseInfo3.append("km\n");
                        courseInfo3.append("예상 소요시간: " + totalTime3);
                        //bundle3.putString("courseInfo3", String.valueOf(courseInfo3));
                        //message3.setData(bundle3);
                        //courseHandler3.sendMessage(message3);

                        courseInfo.setText(courseInfo3);
                    }

                    // 3->1번째
                    /*
                    else if (flag == 3) {
                        System.out.println("로그 =3");
                        flag = 1;
                        spotName1 = "";
                        //courseInfo1.delete(0,courseInfo1.length());
                        //flagText.setText(flag + "번째");

                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, FirstPoint, 10, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapView.addTMapPath(tMapPolyLine);

                            }
                        });

                        //courseInfo.setText(courseInfo1);
                    }

                     */
                    click += 1;
                    //System.out.println("로그 click: " + click);
                }

                // 한 바퀴 돌고 난 후

                else {
                    // 1->2번째
                    if (flag == 1) {
                        flag += 1;
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, SecondPoint, 10, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapView.addTMapPath(tMapPolyLine);
                            }
                        });
                        courseInfo.setText(courseInfo2);
                    }
                    // 2->3번째
                    else if (flag == 2) {
                        flag += 1;
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, ThirdPoint, 10, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapView.addTMapPath(tMapPolyLine);
                            }
                        });
                        courseInfo.setText(courseInfo3);
                    }
                    // 3->1번째
                    else if (flag == 3) {
                        flag = 1;
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, FirstPoint, 10, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapView.addTMapPath(tMapPolyLine);
                            }
                        });
                        courseInfo.setText(courseInfo1);
                    }
                    click += 1;
                    System.out.println("로그 click: " + click);
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
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
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

    public void timeConverter(ArrayList<String> arrayList) {

        if (arrayList.get(0).equals("?")) {
            totalTime1 = "일시적인 오류로 도출 불가";
            totalTime2 = "일시적인 오류로 도출 불가";
            totalTime3 = "일시적인 오류로 도출 불가";
        } else {
            String time1_s = arrayList.get(0);
            int time1 = Integer.parseInt(time1_s);
            int hour1 = time1 / (60 * 60);
            int min1 = time1 / 60 - (hour1 * 60);
            int sec1 = time1 % 60;
            totalTime1 = hour1 + "시간 " + min1 + "분 " + sec1 + "초";

            String time2_s = arrayList.get(1);
            int time2 = Integer.parseInt(time2_s);
            int hour2 = time2 / (60 * 60);
            int min2 = time2 / 60 - (hour2 * 60);
            int sec2 = time2 % 60;
            totalTime2 = hour2 + "시간 " + min2 + "분 " + sec2 + "초";

            String time3_s = arrayList.get(2);
            int time3 = Integer.parseInt(time3_s);
            int hour3 = time3 / (60 * 60);
            int min3 = time3 / 60 - (hour3 * 60);
            int sec3 = time3 % 60;
            totalTime3 = hour3 + "시간 " + min3 + "분 " + sec3 + "초";

        }
    }


    public void saveWalkClicked(View view) {

        saveDialog.show();

        saveInfo = saveDialog.findViewById(R.id.saveText);


        //saveInfo.setText(courseInformation);

        // 다이얼로그에서 산책로 정보 보여주기
        if (flag == 1) {
            saveInfo.setText(courseInfo1);
        } else if (flag == 2) {
            saveInfo.setText(courseInfo2);
        } else if (flag == 3) {
            saveInfo.setText(courseInfo3);
        }


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
    /*
    @Override
    protected Dialog onCreateDialog(int id) {
        ProgressDialog dialog = new ProgressDialog(this); // 사용자에게 보여줄 대화상자
        dialog.setTitle("산책 코스를 불러오는 중...");
        dialog.setMessage("잠시만 기다려주세요...");
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
        );

        return dialog;
    }*/
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
                    for (int i = 0; i < SecondSpotId.size(); i++) {
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
                } else if (flag == 3) {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < ThirdSpotId.size(); i++) {
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

    class CourseHandler1 extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            Bundle bundle = msg.getData();
            String course1 = bundle.getString("courseInfo1");

            if (flag == 1) {
                courseInfo.setText(course1);
            }
        }
    }



}

