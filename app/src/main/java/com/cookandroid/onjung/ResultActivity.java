package com.cookandroid.onjung;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback {

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;
    // T Map GPS
    TMapGpsManager tMapGPS = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //
        tMapView = new TMapView(this); // 티맵 뷰 생성
        tMapView.setSKTMapApiKey(API_Key); // 앱 키 등록

        // 맵뷰 기본 설정
        tMapView.setZoomLevel(15);
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

        // 내 위치 좌표 home에 저장
        TMapPoint home = tMapGPS.getLocation();

        // 예시 좌표 세팅
        // 공원 좌표 예시 5개
        TMapPoint park1 = new TMapPoint(37.534820, 126.846728); // 더부리
        TMapPoint park2 = new TMapPoint(37.535637, 126.850723); // 안골
        TMapPoint park3 = new TMapPoint(37.537834, 126.844670); // 까치산근린
        TMapPoint park4 = new TMapPoint(37.532089, 126.848260); // 배다리
        TMapPoint park5 = new TMapPoint(37.533392, 126.841898); // 까치
        // 공원 좌표 예시 5개 (도서관 주변)
        TMapPoint park6 = new TMapPoint(37.479805, 126.974018); // 양지
        TMapPoint park7 = new TMapPoint(37.479890, 126.973135); // 새싹
        TMapPoint park8 = new TMapPoint(37.480291, 126.969957); // 꿈돌이
        TMapPoint park9 = new TMapPoint(37.478126, 126.970183); // 토끼
        TMapPoint park10 = new TMapPoint(37.477632, 126.967286); // 까치산근린(사당)

        // 산 좌표 예시 4개
        TMapPoint mountain1 = new TMapPoint(37.541086, 126.855847); // 봉제산
        TMapPoint mountain2 = new TMapPoint(37.536362, 126.844170); // 까치산
        TMapPoint mountain3 = new TMapPoint(37.551230, 126.842854); // 우장산
        TMapPoint mountain4 = new TMapPoint(37.543322, 126.876912); // 용왕산
        // 산 좌표 예시 4개 (도서관 주변)
        TMapPoint mountain5 = new TMapPoint(37.497045, 126.961787); // 서달산
        TMapPoint mountain6 = new TMapPoint(37.507337, 126.954357); // 수도산
        TMapPoint mountain7 = new TMapPoint(37.506520, 126.954529); // 고구동산
        TMapPoint mountain8 = new TMapPoint(37.510297, 126.957362); // 안산

        // 공원들을 공원 배열에 추가
        ArrayList<TMapPoint> park = new ArrayList<TMapPoint>();
        park.add(park1);
        park.add(park2);
        park.add(park3);
        park.add(park4);
        park.add(park5);
        park.add(park6);
        park.add(park7);
        park.add(park8);
        park.add(park9);
        park.add(park10);

        // 산들을 산 배열에 추가
        ArrayList<TMapPoint> mountain = new ArrayList<TMapPoint>();
        mountain.add(mountain1);
        mountain.add(mountain2);
        mountain.add(mountain3);
        mountain.add(mountain4);
        mountain.add(mountain5);
        mountain.add(mountain6);
        mountain.add(mountain7);
        mountain.add(mountain8);


        TMapData tmapdata = new TMapData();


        // 폴리라인 배열에 넣기;
        ArrayList<TMapPolyLine> polyLinesPark = new ArrayList<TMapPolyLine>();

        for (int i = 0; i < park.size(); i++) {
            TMapPolyLine line = new TMapPolyLine();
            line.addLinePoint(home);
            line.addLinePoint(park.get(i));
            polyLinesPark.add(line);
        }

        ArrayList<TMapPolyLine> polyLinesMountain = new ArrayList<TMapPolyLine>();

        for (int i = 0; i < mountain.size(); i++) {
            TMapPolyLine line = new TMapPolyLine();
            line.addLinePoint(home);
            line.addLinePoint(mountain.get(i));
            polyLinesMountain.add(line);
        }


        // 가장 가까운 경유지 찾는 메소드 이용
        TMapPoint spotPark = closestSpot(polyLinesPark);
        TMapPoint spotMoutain = closestSpot(polyLinesMountain);


        // 선택된 경유지 배열에 넣기
        ArrayList<TMapPoint> spot = new ArrayList<TMapPoint>();
        spot.add(spotPark);
        spot.add(spotMoutain);

        // 폴리라인 객체 받아와서 맵뷰에 그리기
        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, spot, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView.addTMapPath(tMapPolyLine);
            }
        });


    }

    // 가장 가까운 거리에 있는 경유지 찾기

    // Arraylist lines = {A', B', C', D', E'}
    public TMapPoint closestSpot(ArrayList<TMapPolyLine> lines) {
        TMapPolyLine min = lines.get(0);

        for (int i = 1; i < lines.size(); i++) {
            if (min.getDistance() > lines.get(i).getDistance()) {
                min = lines.get(i);
            }
        }

        ArrayList<TMapPoint> result = min.getLinePoint();
        TMapPoint spot = result.get(1);

        return spot;
    }

    public void saveWalkClicked(View view) {
        Intent intent = new Intent(this, SaveWalkActivity.class);
        startActivity(intent);
    }


}

