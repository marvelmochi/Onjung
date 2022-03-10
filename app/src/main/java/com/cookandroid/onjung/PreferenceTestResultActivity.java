package com.cookandroid.onjung;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class PreferenceTestResultActivity extends AppCompatActivity {

    // 취향을 분석한 산책로 추천 (평가 이전 ver.)
    // (PreferenceTestActivity에서 API 호출 후
    // 받아온 데이터 값 PreferenceTestResultActivity로 넘김)

    // API 15-1번(평가 이전) 사용 하여 경로 3개 구현

    // <이전 액티비티로부터 받아올 데이터들 선언>
    ArrayList<String> recentPosition;
    // 첫 번째 경로 경유지 위경도 배열
    ArrayList<String> firstPoint_lat = new ArrayList<>();
    ArrayList<String> firstPoint_lon = new ArrayList<>();
    // 두 번째 경로 경유지 위경도 배열
    ArrayList<String> secondPoint_lat = new ArrayList<>();
    ArrayList<String> secondPoint_lon = new ArrayList<>();
    // 세 번째 경로 경유지 위경도 배열
    ArrayList<String> thirdPoint_lat = new ArrayList<>();
    ArrayList<String> thirdPoint_lon = new ArrayList<>();
    // 경유지 이름 배열
    ArrayList<String> FirstName = new ArrayList<>();
    ArrayList<String> SecondName = new ArrayList<>();
    ArrayList<String> ThirdName = new ArrayList<>();
    // spotId 배열(받을 때: int, 보낼 때: String)
    ArrayList<String> firstSpotId = new ArrayList<>();
    ArrayList<String> secondSpotId = new ArrayList<>();
    ArrayList<String> thirdSpotId = new ArrayList<>();
    // UserId(memberId)
    String memberId;
    // 홈 위치 좌표
    Double home_lat, home_lon;


    // <T MAP 관련>
    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;
    TMapView tMapView_x = null;
    TMapData tmapdata;

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
    // 산책로 정보값 전달할 핸들러
    CourseHandler1 courseHandler1 = new CourseHandler1();
    int click = 1;


    // 뒤로가기 두 번 눌러 종료
    private long backKeyPressedTime = 0;
    private Toast toast;


    // <retry 관련>
    Button retryButton;
    // 경로 flag
    int flag = 1;
    //TextView flagText;

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

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;

    //플로팅
    private Animation fab_open, fab_close, fab_rotate_open, fab_rotate_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test_result);

        // SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");
        System.out.println("로그: 멤버아이디 불러오기(Result): " + memberId);

        // 이전 액티비티에서 데이터 받아오기
        Intent intent = getIntent();
        recentPosition = intent.getStringArrayListExtra("recentPosition");
        System.out.println("로그: 취향 결과 현위치: " + recentPosition.get(0) + ", " + recentPosition.get(1));
        home_lat = Double.parseDouble(recentPosition.get(0));
        home_lon = Double.parseDouble(recentPosition.get(1));
        System.out.println("로그: 현위치: " + recentPosition.get(0) + ", " + recentPosition.get(1));

        firstPoint_lat = intent.getStringArrayListExtra("firstPoint_lat");
        firstPoint_lon = intent.getStringArrayListExtra("firstPoint_lon");
        secondPoint_lat = intent.getStringArrayListExtra("secondPoint_lat");
        secondPoint_lon = intent.getStringArrayListExtra("secondPoint_lon");
        thirdPoint_lat = intent.getStringArrayListExtra("thirdPoint_lat");
        thirdPoint_lon = intent.getStringArrayListExtra("thirdPoint_lon");

        firstSpotId = intent.getStringArrayListExtra("firstSpotId");
        secondSpotId = intent.getStringArrayListExtra("secondSpotId");
        thirdSpotId = intent.getStringArrayListExtra("thirdSpotId");
        FirstName = intent.getStringArrayListExtra("FirstName");
        SecondName = intent.getStringArrayListExtra("SecondName");
        ThirdName = intent.getStringArrayListExtra("ThirdName");



        //
        retryButton = (Button) findViewById(R.id.retry);
        courseInfo = (TextView) findViewById(R.id.courseInfo);

        //flagText = findViewById(R.id.flagText);
        //flagText.setText(flag + "번째");

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
        TMapPoint home = new TMapPoint(home_lat, home_lon);

        // 경로1. 경유지 배열
        ArrayList<TMapPoint> spots1 = new ArrayList<>();
        // 경로2. 경유지 배열
        ArrayList<TMapPoint> spots2 = new ArrayList<>();
        // 경로3. 경유지 배열
        ArrayList<TMapPoint> spots3 = new ArrayList<>();

        // 다이얼로그
        saveDialog = new Dialog(PreferenceTestResultActivity.this);
        saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveDialog.setContentView(R.layout.dialog_save_walk);

        // 다이얼로그
        // dateText = (EditText)saveDialog.findViewById(R.id.dateText);
        // 날짜, 산책 제목 받아올 변수 선언
        dateText = (TextView) saveDialog.findViewById(R.id.dateText);
        titleText = (EditText) saveDialog.findViewById(R.id.titleText);

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());

        // 경로1. 경유지 TmapPoint 배열에 넣기
        for (int i = 0; i < firstPoint_lat.size(); i++) {
            Double lat = Double.parseDouble(firstPoint_lat.get(i));
            Double lon = Double.parseDouble(firstPoint_lon.get(i));
            TMapPoint spot = new TMapPoint(lat, lon);
            spots1.add(spot);
        }

        // 경로2. 경유지 TmapPoint 배열에 넣기
        for (int i = 0; i < secondPoint_lat.size(); i++) {
            Double lat = Double.parseDouble(secondPoint_lat.get(i));
            Double lon = Double.parseDouble(secondPoint_lon.get(i));
            TMapPoint spot = new TMapPoint(lat, lon);
            spots2.add(spot);
        }

        // 경로3. 경유지 TmapPoint 배열에 넣기
        for (int i = 0; i < thirdPoint_lat.size(); i++) {
            Double lat = Double.parseDouble(thirdPoint_lat.get(i));
            Double lon = Double.parseDouble(thirdPoint_lon.get(i));
            TMapPoint spot = new TMapPoint(lat, lon);
            spots3.add(spot);
        }




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

                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots2, 10, new TMapData.FindPathDataListenerCallback() {
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
                        courseInfo2.append("km");
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

                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots3, 10, new TMapData.FindPathDataListenerCallback() {
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
                        courseInfo3.append("km");
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
                    System.out.println("로그 click: " +click);
                }

                // 한 바퀴 돌고 난 후

                else {
                    // 1->2번째
                    if (flag == 1) {
                        flag += 1;
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots2, 10, new TMapData.FindPathDataListenerCallback() {
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
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots3, 10, new TMapData.FindPathDataListenerCallback() {
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
                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots1, 10, new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapView.addTMapPath(tMapPolyLine);
                            }
                        });
                        courseInfo.setText(courseInfo1);
                    }
                    click += 1;
                    System.out.println("로그 click: "+click );
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
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
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

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 홈으로 이동합니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            //finish();
            toast.cancel();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        }
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

        datePickerDialog = new DatePickerDialog(PreferenceTestResultActivity.this, new DatePickerDialog.OnDateSetListener() {
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

                PreferenceTestResultActivity.HttpConnectorSaveCourse saveCourseThread = new PreferenceTestResultActivity.HttpConnectorSaveCourse();
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
                data.put("userId", memberId);
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
            try{
                conn.setDoOutput(true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                bw.write(data.toString());
                bw.flush();
                bw.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                int responseCode = conn.getResponseCode();
                System.out.println("로그: 응답 메시지: "+returnMsg);
                System.out.println("로그: responseCode: "+responseCode);

                // Toast 띄우기
                JSONObject jsonObject = new JSONObject(returnMsg);
                String detail = jsonObject.getString("detail");
                ToastMessage(detail);

            }catch (Exception e){
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