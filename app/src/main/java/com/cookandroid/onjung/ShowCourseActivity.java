package com.cookandroid.onjung;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ShowCourseActivity extends AppCompatActivity  {

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;

    // Using SharedPreferences
    SharedPreferences preferences;

    // Preference에서 꺼내어 저장할 String 변수
    String title;
    String home_lat, home_lon;
    String jname_s, jlat_s, jlon_s;
    String completeFlag;
    // 경로 정보 저장할 ArrayList
    ArrayList<String> nameList, latList, lonList;

    // 산책 완료
    Button completeBtn;
    String httpCompleteMsg;
    String walkId;

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_course);


        // 경로 정보 저장할 ArrayList
        nameList = new ArrayList<>();
        latList = new ArrayList<>();
        lonList = new ArrayList<>();


        // Using SharedPreferences
        preferences = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        title = preferences.getString("title", "");
        System.out.println("로그: 산책제목 불러오기(ShowCourse): " + title);

        TextView titleText = (TextView) findViewById(R.id.titleText);
        titleText.setText(title);

        home_lat = preferences.getString("home_lat","");
        home_lon = preferences.getString("home_lon","");
        jname_s = preferences.getString("jname", "");
        jlat_s = preferences.getString("jlat", "");
        jlon_s = preferences.getString("jlon", "");
        walkId = preferences.getString("walkId","");
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
        tMapView.setCenterPoint(Double.parseDouble(home_lon),Double.parseDouble(home_lat));
        tMapView.setTrackingMode(false);
        LinearLayout Tmap = (LinearLayout) findViewById(R.id.map);
        Tmap.addView(tMapView);
        // 트래킹모드 (화면중심을 단말의 현재위치로 이동)


        // Request For GPS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


        // 홈 티맵포인트 생성
        TMapPoint home = new TMapPoint(Double.parseDouble(home_lat), Double.parseDouble(home_lon));


        ArrayList<TMapPoint> spots = new ArrayList<>();
        TMapData tmapdata = new TMapData();

        for(int i=0; i<latList.size();i++){
            Double dlat = Double.parseDouble(latList.get(i));
            Double dlon = Double.parseDouble(lonList.get(i));
            TMapPoint spot = new TMapPoint(dlat, dlon);
            spots.add(spot);
        }


        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spots, 10,new TMapData.FindPathDataListenerCallback(){
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
                /*
                satisfactionDialog = new Dialog(getContext());
                satisfactionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                satisfactionDialog.setContentView(R.layout.dialog_satisfaction);
                satisfactionDialog.show();

                 */


            }
        });

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());

    }



    public void jsonParserSharedPreferences(){
        try{
            JSONArray nameArray = new JSONArray(jname_s);
            JSONArray latArray = new JSONArray(jlat_s);
            JSONArray lonArray = new JSONArray(jlon_s);

            for(int i=0; i<latArray.length(); i++){
                String name = nameArray.get(i).toString();
                nameList.add(name);
                String lat = latArray.get(i).toString();
                latList.add(lat);
                String lon = lonArray.get(i).toString();
                lonList.add(lon);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("로그: 받아온 데이터 파싱 예외 발생");
        }

    }

    public void okClicked(View view) {
        finish();
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