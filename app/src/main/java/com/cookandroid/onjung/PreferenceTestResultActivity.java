package com.cookandroid.onjung;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

    // 전역변수 선언
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

    // 경유지 이름 배열 (*연결 아직 언 되어있음)
    ArrayList<String> firstName = new ArrayList<>();
    ArrayList<String> secondName = new ArrayList<>();
    ArrayList<String> thirdName = new ArrayList<>();

    // spotId 배열(받을 때: int, 보낼 때: String)
    ArrayList<String> firstSpotId = new ArrayList<>();
    ArrayList<String> secondSpotId = new ArrayList<>();
    ArrayList<String> thirdSpotId = new ArrayList<>();

    // UserId(memberId)
    String memberId;

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;

    // 홈 위치 좌표
    Double home_lat, home_lon;

    //
    TextView courseInfo;

    // 뒤로가기 두 번 눌러 종료
    private long backKeyPressedTime = 0;
    private Toast toast;

    // 경로를 구분할 플래그

    // retry 버튼
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
        /*
        spotName = intent.getStringArrayListExtra("spotName");
        spotLat = intent.getStringArrayListExtra("spotLat");
        spotLon = intent.getStringArrayListExtra("spotLon");
        spotId = intent.getStringArrayListExtra("spotId");

         */
        firstPoint_lat = intent.getStringArrayListExtra("firstPoint_lat");
        firstPoint_lon = intent.getStringArrayListExtra("firstPoint_lon");
        secondPoint_lat = intent.getStringArrayListExtra("secondPoint_lat");
        secondPoint_lon = intent.getStringArrayListExtra("secondPoint_lon");
        thirdPoint_lat = intent.getStringArrayListExtra("thirdPoint_lat");
        thirdPoint_lon = intent.getStringArrayListExtra("thirdPoint_lon");

        firstSpotId = intent.getStringArrayListExtra("firstSpotId");
        secondSpotId = intent.getStringArrayListExtra("secondSpotId");
        thirdSpotId = intent.getStringArrayListExtra("thirdSpotId");


        //
        retryButton = findViewById(R.id.retry);

        //
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
        // 홈 티맵포인트 생성
        TMapPoint home = new TMapPoint(home_lat, home_lon);

        // 경로1. 경유지 배열
        ArrayList<TMapPoint> spots1 = new ArrayList<>();
        // 경로2. 경유지 배열
        ArrayList<TMapPoint> spots2 = new ArrayList<>();
        // 경로3. 경유지 배열
        ArrayList<TMapPoint> spots3 = new ArrayList<>();

        TMapData tmapdata = new TMapData();

        // 다이얼로그
        saveDialog = new Dialog(PreferenceTestResultActivity.this);
        saveDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveDialog.setContentView(R.layout.dialog_save_walk);

        // 다이얼로그
        // dateText = (EditText)saveDialog.findViewById(R.id.dateText);
        // 날짜, 산책 제목 받아올 변수 선언
        dateText = (TextView) saveDialog.findViewById(R.id.dateText);
        titleText = (EditText) saveDialog.findViewById(R.id.titleText);

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());

        courseInfo = findViewById(R.id.courseInfo);
        String spotnameString = "";
        // 텍스트뷰에 코스 정보 표시하기
        /*
        for (int i = 0; i < spotName.size(); i += 3) {

            spotnameString += spotName.get(i) + " ";

        }
        spotnameString += "을(를) 경유하는 산책 코스입니다. \n";
        courseInfo.setText(spotnameString);

         */

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

        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots1, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView.addTMapPath(tMapPolyLine);

                //distance = tMapPolyLine.getDistance();
                //courseInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                //saveInfo.append("총 거리: " + distance); // km 표시로 수정 필요
                //courseInformation = courseInfo.getText().toString();
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

                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots2, 10, new TMapData.FindPathDataListenerCallback() {
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

                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots3, 10, new TMapData.FindPathDataListenerCallback() {
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

                    tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots1, 10, new TMapData.FindPathDataListenerCallback() {
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

        //saveInfo = saveDialog.findViewById(R.id.saveText);
        //saveInfo.setText(courseInformation);

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

}