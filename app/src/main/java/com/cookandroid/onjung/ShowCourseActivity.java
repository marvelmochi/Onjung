package com.cookandroid.onjung;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class ShowCourseActivity extends AppCompatActivity {

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;

    // Using SharedPreferences
    SharedPreferences preferences;

    // Preference에서 꺼내어 저장할 String 변수
    String title;
    String home_lat, home_lon;
    String jname_s, jlat_s, jlon_s, jspot_s;
    String completeFlag;
    // 경로 정보 저장할 ArrayList
    ArrayList<String> nameList, latList, lonList, spotIdList;
    ArrayList<String> scoreList;

    // 산책 완료
    Button completeBtn;
    String httpCompleteMsg;
    String walkId;

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;

    // 만족도 조사 다이얼로그
    Dialog satisfactionDialog;

    // 만족도 조사 다이얼로그에 올릴 리니어 레이아웃
    LinearLayout linearRating;

    // 만족도 통신에 필요한 변수들
    // UserId
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_course);


        // 경로 정보 저장할 ArrayList
        nameList = new ArrayList<>();
        latList = new ArrayList<>();
        lonList = new ArrayList<>();
        spotIdList = new ArrayList<>();
        scoreList = new ArrayList<>();

        // Using SharedPreferences
        preferences = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        title = preferences.getString("title", "");
        System.out.println("로그: 산책제목 불러오기(ShowCourse): " + title);
        userId = preferences.getString("memberId", "");
        System.out.println("로그: userId: " + userId);

        TextView titleText = (TextView) findViewById(R.id.titleText);
        titleText.setText(title);

        home_lat = preferences.getString("home_lat", "");
        home_lon = preferences.getString("home_lon", "");
        jname_s = preferences.getString("jname", "");
        jlat_s = preferences.getString("jlat", "");
        jlon_s = preferences.getString("jlon", "");
        walkId = preferences.getString("walkId", "");
        jspot_s = preferences.getString("jspot", "");
        //completeFlag = preferences.getString("completeFlag", "");

        System.out.println("로그: 홈 위도 불러오기: " + home_lat);
        System.out.println("로그: 경유지명 불러오기: " + jname_s);
        System.out.println("로그: 경유지 위도 불러오기: " + jlat_s);
        System.out.println("로그: 경유지 경도 불러오기: " + jlon_s);

        jsonParserSharedPreferences();

        // 티맵 관련
        tMapView = new TMapView(this);// 티맵 뷰 생성
        tMapView.setSKTMapApiKey(API_Key); // 앱 키 등록
        // 맵뷰 기본 설정
        tMapView.setZoomLevel(16);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        //tMapView.setCenterPoint( 126.851523,37.534732);
        // 지도 초기 위치 설정
        tMapView.setCenterPoint(Double.parseDouble(home_lon), Double.parseDouble(home_lat));
        tMapView.setTrackingMode(false);
        LinearLayout Tmap = (LinearLayout) findViewById(R.id.map);
        Tmap.addView(tMapView);


        // Request For GPS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


        // 홈 티맵포인트 생성
        TMapPoint home = new TMapPoint(Double.parseDouble(home_lat), Double.parseDouble(home_lon));


        ArrayList<TMapPoint> spots = new ArrayList<>();
        TMapData tmapdata = new TMapData();

        for (int i = 0; i < latList.size(); i++) {
            Double dlat = Double.parseDouble(latList.get(i));
            Double dlon = Double.parseDouble(lonList.get(i));
            TMapPoint spot = new TMapPoint(dlat, dlon);
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


        // 산책 완료 버튼 접근 및 통신 시작
        completeBtn = findViewById(R.id.completeBtn);
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 산책 완료 통신
                HttpConnectorComplete completeThread = new HttpConnectorComplete();
                completeThread.start();

                // 만족도 조사 다이얼로그 띄우기
                showSatisfactionDialog();

            }
        });

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());

    }


    public void jsonParserSharedPreferences() {
        try {
            JSONArray nameArray = new JSONArray(jname_s);
            JSONArray latArray = new JSONArray(jlat_s);
            JSONArray lonArray = new JSONArray(jlon_s);
            JSONArray spotIdArray = new JSONArray(jspot_s);

            for (int i = 0; i < latArray.length(); i++) {
                String name = nameArray.get(i).toString();
                nameList.add(name);
                String lat = latArray.get(i).toString();
                latList.add(lat);
                String lon = lonArray.get(i).toString();
                lonList.add(lon);
                String spotid = spotIdArray.get(i).toString();
                spotIdList.add(spotid);

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("로그: 받아온 데이터 파싱 예외 발생");
        }

    }

    public void okClicked(View view) {
        finish();
    }

    public void scoreClicked(View view) {

        for (int i = 0; i < nameList.size(); i++) {
            RatingBar ratingBar = satisfactionDialog.findViewById(i);
            float score = ratingBar.getRating();
            String score_s = Float.toString(score);
            scoreList.add(score_s);
            System.out.println("로그: score: " + score_s);
        }
        HttpConnectorScore scoreThread = new HttpConnectorScore();
        scoreThread.start();
    }

    class HttpConnectorComplete extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {

                System.out.println("로그: walkId: " + walkId);
                url = new URL("http://smwu.onjung.tk/walk/toggle/" + walkId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 응답 메시지: " + returnMsg);

                JSONObject jsonObject = new JSONObject(returnMsg);
                httpCompleteMsg = jsonObject.getString("detail");
                ToastMessage(httpCompleteMsg);


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 산책 저장 예외 발생");
            }
        }
    }

    public void showSatisfactionDialog() {

        // 만족도 조사 다이얼로그 띄우기
        satisfactionDialog = new Dialog(ShowCourseActivity.this);
        satisfactionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        satisfactionDialog.setContentView(R.layout.dialog_satisfaction);
        satisfactionDialog.show();

        // 텍스트 뷰 & RatingBar 동적 생성

        // 산책지 명 : nameList

        linearRating = satisfactionDialog.findViewById(R.id.linear_rating);

        for (int i = 0; i < nameList.size(); i++) {
            TextView nameText = new TextView(getApplicationContext());
            nameText.setGravity(Gravity.CENTER);
            nameText.setTextSize(22);
            nameText.setText(nameList.get(i));
            linearRating.addView(nameText);

            RatingBar ratingBar = new RatingBar(this);
            ratingBar.setRating(0);
            ratingBar.setNumStars(5);
            ratingBar.setStepSize((float) 0.5);
            ratingBar.setMax(5);
            ratingBar.setId(i);
            LayerDrawable drawable = (LayerDrawable) ratingBar.getProgressDrawable();
            drawable.getDrawable(0).setColorFilter(Color.parseColor("#B1BCBE"), PorterDuff.Mode.SRC_ATOP);
            drawable.getDrawable(1).setColorFilter(Color.parseColor("#ffff00"), PorterDuff.Mode.SRC_ATOP);
            drawable.getDrawable(2).setColorFilter(Color.parseColor("#ffff00"), PorterDuff.Mode.SRC_ATOP);

            //ratingBar.set
                    //olor.parseColor("#B1BCBE"));

            //ratingBar.setProgress(1);
            ratingBar.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            );
            linearRating.addView(ratingBar);


        }
        // 만족도 통신
        /*
        Button satisfactionCompleteBtn = (Button)satisfactionDialog.findViewById(R.id.satisfactionCompleteBtn);
        satisfactionCompleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                for (int i=0; i<nameList.size();i++){
                    RatingBar ratingBar = findViewById(i);
                    float score = ratingBar.getRating();
                    //String score_s = Float.toString(score);
                    //scoreList.add(score_s);
                    System.out.println("로그: score: "+score);
                }
                HttpConnectorScore scoreThread = new HttpConnectorScore();
                scoreThread.start();
            }
        });*/


        // 닫기 이벤트
        Button dismissBtn = (Button) satisfactionDialog.findViewById(R.id.dismissBtn);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                satisfactionDialog.dismiss();
            }
        });


    }

    class HttpConnectorScore extends Thread {
        JSONObject data;
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {
                // 산책지 수만큼 반복
                for (int i = 0; i < spotIdList.size(); i++) {
                    data = new JSONObject();
                    data.put("userId", userId);
                    data.put("spotId", spotIdList.get(i));
                    data.put("score", scoreList.get(i));
                    url = new URL("http://smwu.onjung.tk/rating");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                    bw.write(data.toString());
                    bw.flush();
                    bw.close();
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String returnMsg = in.readLine();
                    //int responseCode = conn.getResponseCode();
                    System.out.println("로그: 응답 메시지: " + returnMsg);
                    //System.out.println("로그: responseCode: " + responseCode);

                    // 토스트 메시지를 위해 응답 메시지 파싱
                    JSONObject detailObject = new JSONObject(returnMsg);
                    String detail = detailObject.getString("detail");
                    ToastMessage(detail);


                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 만족도 통신 예외 발생");
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