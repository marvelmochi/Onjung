package com.cookandroid.onjung;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class PreferenceTestResultActivity extends AppCompatActivity {

    // 전역변수 선언
    ArrayList<String> recentPosition;
    ArrayList<String> spotName;
    ArrayList<String> spotLat;
    ArrayList<String> spotLon;
    ArrayList<String> spotId;

    // UserId(memberId)
    String memberId;

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;

    // 홈 위치 좌표
    Double home_lat, home_lon;

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
        spotName = intent.getStringArrayListExtra("spotName");
        spotLat = intent.getStringArrayListExtra("spotLat");
        spotLon = intent.getStringArrayListExtra("spotLon");
        spotId = intent.getStringArrayListExtra("spotId");

        // 티맵 관련
        tMapView = new TMapView(this);// 티맵 뷰 생성
        tMapView.setSKTMapApiKey(API_Key); // 앱 키 등록
        // 맵뷰 기본 설정
        tMapView.setZoomLevel(16);
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

        ArrayList<TMapPoint> spots = new ArrayList<>();
        TMapData tmapdata = new TMapData();

        /*
        for (int i = 0; i < spotLat.size(); i++) {
            Double dlat = Double.parseDouble(spotLat.get(i));
            Double dlon = Double.parseDouble(spotLon.get(i));
            TMapPoint spot = new TMapPoint(dlat, dlon);
            spots.add(spot);
        }*/

        // 경로 3개 --> 수정 요함 (하드코딩ed)

        for (int i=0; i<spotLat.size();i+=3){

            Double lat = Double.parseDouble(spotLat.get(i));
            Double lon = Double.parseDouble(spotLon.get(i));
            TMapPoint spot = new TMapPoint(lat, lon);
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
    }

    public void okClicked(View view) {
        finish();
    }
}