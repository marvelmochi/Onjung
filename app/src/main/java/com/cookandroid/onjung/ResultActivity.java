package com.cookandroid.onjung;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

    // 인텐트로 액티비티 간 데이터 전달
    String myData;
    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // SharedPreferences Test
        SharedPreferences preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");
        System.out.println("로그: 멤버아이디 불러오기(Result): " + memberId);

        // 다이얼로그
        saveDialog = new Dialog(ResultActivity.this);
        saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveDialog.setContentView(R.layout.dialog_save_walk);

        // 다이얼로그
        // dateText = (EditText)saveDialog.findViewById(R.id.dateText);
        // 날짜, 산책 제목 받아올 변수 선언
        dateText = (TextView) saveDialog.findViewById(R.id.dateText);
        titleText = (EditText) saveDialog.findViewById(R.id.titleText);

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

        //saveInfo = (TextView)findViewById(R.id.saveText);
        //saveInfo.setText(spotnameString);


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


    public void saveWalkClicked(View view) {
        /*
        Intent intentResult = new Intent(ResultActivity.this, SaveWalkActivity.class);
        intentResult.putExtra("mydata", myData);
        intentResult.putExtra("recentPosition", recentPosition);
        intentResult.putExtra("spotName", spotName);
        intentResult.putExtra("spotLat", spotLat);
        intentResult.putExtra("spotLon", spotLon);
        startActivity(intentResult);
        */

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
                dateText.setText(year + "/" + (month+1) + "/" + dayOfMonth);
                sYear = Integer.toString(year);
                sMonth = Integer.toString((month+1));
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
                if (sMonth.length()==1){
                    sMonth = "0"+sMonth;
                }
                if (sDay.length()==1){
                    sDay = "0"+sDay;
                }
                date = sYear+sMonth+sDay;

                title = titleText.getText().toString();
                System.out.println("로그: save 클릭");
                System.out.println("로그: date: "+date);
                System.out.println("로그: title: "+title);

                HttpConnectorSaveCourse saveCourseThread = new HttpConnectorSaveCourse();
                saveCourseThread.start();

            }

        });


    }

    public void dateTextClicked(View view) {
        datePickerDialog.show();
    }

    // 로딩중 표시할 프로그레스 다이얼로그
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

    // 서버 통신부

    class HttpConnectorSaveCourse extends Thread {
        JSONObject data;
        URL url;
        HttpURLConnection conn;
        public HttpConnectorSaveCourse(){
            try{
                data = new JSONObject();
                data.put("userId", memberId);
                data.put("walkDate", date);
                data.put("title", title);
                // jsonArray에 경유지 넣기
                JSONArray jsonArray = new JSONArray();
                for (int i=0; i<spotId.size(); i++){
                    jsonArray.put(spotId.get(i));
                }
                data.put("wayPoint", jsonArray);
                data.put("latitude", recentPosition.get(0));
                data.put("longitude", recentPosition.get(1));
                System.out.println("로그: 산책로 저장 post data: "+data);
                url = new URL("http://smwu.onjung.tk/walk");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

            }catch (Exception e){
                e.printStackTrace();
                System.out.println("로그: 산책 코스 POST 예외 발생");
            }
        }
        @Override
        public void run(){
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

            } catch (Exception e){
                e.printStackTrace();
                System.out.println("로그: 산책 코스 저장 연결 예외 발생");
            }
        }
    }

}

