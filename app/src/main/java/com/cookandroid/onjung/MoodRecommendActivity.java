package com.cookandroid.onjung;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    // <이전 액티비티로부터 받아올 데이터들 선언>
    // userId(memberId)
    String userId;
    // Using SharedPreferences
    SharedPreferences preferences;
    // 경유지 티맵 포인트 ArrayList
    ArrayList<TMapPoint> firstPoint = new ArrayList<>();
    ArrayList<TMapPoint> secondPoint = new ArrayList<>();
    ArrayList<TMapPoint> thirdPoint = new ArrayList<>();

    ArrayList<String> firstLat = new ArrayList<>();
    ArrayList<String> secondLat = new ArrayList<>();
    ArrayList<String> thirdLat = new ArrayList<>();
    ArrayList<String> firstLon = new ArrayList<>();
    ArrayList<String> secondLon = new ArrayList<>();
    ArrayList<String> thirdLon = new ArrayList<>();
    // spotId
    ArrayList<String> firstSpotId = new ArrayList<>();
    ArrayList<String> secondSpotId = new ArrayList<>();
    ArrayList<String> thirdSpotId = new ArrayList<>();
    // name
    ArrayList<String> firstName = new ArrayList<>();
    ArrayList<String> secondName = new ArrayList<>();
    ArrayList<String> thirdName = new ArrayList<>();
    // 현위치 좌표 전달
    double home_lat, home_lon;
    String home_lat_s, home_lon_s;

    // <T Map 관련>
    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    // T Map GPS
    TMapGpsManager tMapGPS = null;
    TMapView tMapView = null;
    TMapView tMapView_x = null;
    TMapData tmapdata;
    TMapPoint home;


    // <경로 전환 관련>
    int flag = 1; // 경로 flag
    TextView flagText;
    Button retryButton;

    // <산책로 정보 보여주기 관련>
    TextView courseInfo;
    TextView saveInfo;
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

    // <일정 저장 다이얼로그 관련>
    // 산책 일정 저장 다이얼로그
    Dialog saveDialog;
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

    // 아이콘 표시를 위한 컨텍스트 선언
    Context context;

    ArrayList<String> timeList = new ArrayList<>(); // 소요시간

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_recommend);

        // Using SharedPreferences / memberId 받아오기
        preferences = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        userId = preferences.getString("memberId", "");
        String home_lat_s = preferences.getString("home_lat", "");
        String home_lon_s = preferences.getString("home_lon", "");

        recentPosition = new ArrayList<>();
        recentPosition.add(home_lat_s);
        recentPosition.add(home_lon_s);
        home_lat = Double.parseDouble(recentPosition.get(0));
        home_lon = Double.parseDouble(recentPosition.get(1));

        // 이전 액티비티에서 데이터 받아오기
        Intent intent = getIntent();

        firstLat = intent.getStringArrayListExtra("firstLat");
        secondLat = intent.getStringArrayListExtra("secondLat");
        thirdLat = intent.getStringArrayListExtra("thirdLat");
        firstLon = intent.getStringArrayListExtra("firstLon");
        secondLon = intent.getStringArrayListExtra("secondLon");
        thirdLon = intent.getStringArrayListExtra("thirdLon");
        firstName = intent.getStringArrayListExtra("firstName");
        secondName = intent.getStringArrayListExtra("secondName");
        thirdName = intent.getStringArrayListExtra("thirdName");
        firstSpotId = intent.getStringArrayListExtra("firstSpotId");
        secondSpotId = intent.getStringArrayListExtra("secondSpotId");
        thirdSpotId = intent.getStringArrayListExtra("thirdSpotId");
        timeList = intent.getStringArrayListExtra("timeList");
        System.out.println("로그: timeList(보내기 후): " + timeList);
        timeConverter(timeList);

        //
        for (int i = 0; i < 2; i++) {
            TMapPoint tMapPoint1 = new TMapPoint(Double.parseDouble(firstLat.get(i)), Double.parseDouble(firstLon.get(i)));
            TMapPoint tMapPoint2 = new TMapPoint(Double.parseDouble(secondLat.get(i)), Double.parseDouble(secondLon.get(i)));
            TMapPoint tMapPoint3 = new TMapPoint(Double.parseDouble(thirdLat.get(i)), Double.parseDouble(thirdLon.get(i)));
            firstPoint.add(tMapPoint1);
            secondPoint.add(tMapPoint2);
            thirdPoint.add(tMapPoint3);
        }


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
        // 가짜 티맵 뷰
        tMapView_x = new TMapView(this);

        // 티맵 데이터
        tmapdata = new TMapData();

        // 홈 티맵포인트 생성
        home = new TMapPoint(home_lat, home_lon);

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

        context = this; // Bitmap 아이콘 설정을 위해

        // 2번째 경로 데이터 미리 받아오기
        TMapData tMapData2 = new TMapData();
        tMapData2.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, secondPoint, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView_x.addTMapPath(tMapPolyLine);
                double distance = tMapPolyLine.getDistance() * 0.001;
                distance2 = Double.toString(Math.round(distance * 100) / 100.0);

            }
        });

        // 3번째 경로 데이터 미리 받아오기
        TMapData tMapData3 = new TMapData();
        tMapData3.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, thirdPoint, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView_x.addTMapPath(tMapPolyLine);
                double distance = tMapPolyLine.getDistance() * 0.001;
                distance3 = Double.toString(Math.round(distance * 100) / 100.0);

            }
        });

        // 초기에 표시되는 1번째 경로
        tmapdata = new TMapData();
        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, firstPoint, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                Bitmap marker_start = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_start);
                tMapView.setTMapPathIcon(marker_start, marker_start); // 출발지 아이콘 설정
                tMapView.addTMapPath(tMapPolyLine);

                double distance = tMapPolyLine.getDistance() * 0.001;
                distance1 = Double.toString(Math.round(distance * 100) / 100.0);

                for (int i = 0; i < firstName.size(); i++) {
                    if (i == firstName.size() - 1) {

                        spotName1 += firstName.get(i);
                        break;
                    }
                    spotName1 += firstName.get(i) + ", ";
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

        // 다시 버튼 눌렀을 때 이벤트 처리
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

                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, secondPoint, 10, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapView.addTMapPath(tMapPolyLine);
                                //System.out.println("로그: 경로2 거리: "+ tMapPolyLine.getDistance());
                                //double distance = tMapPolyLine.getDistance() * 0.001;
                                //distance2 = Double.toString(distance);

                            }
                        });

                        for (int i = 0; i < secondName.size(); i++) {
                            if (i == secondName.size() - 1) {
                                spotName2 += secondName.get(i);
                                break;
                            }
                            spotName2 += secondName.get(i) + ", ";
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

                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, thirdPoint, 10, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapView.addTMapPath(tMapPolyLine);
                                //System.out.println("로그: 경로3 거리: "+ tMapPolyLine.getDistance());
                                //double distance = tMapPolyLine.getDistance() * 0.001;
                                //distance3 = Double.toString(distance);

                            }
                        });

                        for (int i = 0; i < thirdName.size(); i++) {
                            if (i == thirdName.size() - 1) {
                                spotName3 += thirdName.get(i);
                                break;
                            }
                            spotName3 += thirdName.get(i) + ", ";
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

                    click += 1;
                    System.out.println("로그 click: " + click);
                }

                // 한 바퀴 돌고 난 후
                else {
                    // 1->2번째
                    if (flag == 1) {
                        flag += 1;
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, secondPoint, 10, new TMapData.FindPathDataListenerCallback() {
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
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, thirdPoint, 10, new TMapData.FindPathDataListenerCallback() {
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
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, firstPoint, 10, new TMapData.FindPathDataListenerCallback() {
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


    }


    public void saveWalkClicked(View view) {
        saveDialog.show();

        saveInfo = saveDialog.findViewById(R.id.saveText);

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

}