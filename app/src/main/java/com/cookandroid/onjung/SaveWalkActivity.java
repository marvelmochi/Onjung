package com.cookandroid.onjung;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class SaveWalkActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback {

    String myData;
    // T Map 뷰
    TMapView tMapView = null;
    // T Map GPS
    TMapGpsManager tMapGPS = null;

    // 전역변수 선언
    ArrayList<String> recentPosition;
    ArrayList<String> spotName;
    ArrayList<String> spotLat;
    ArrayList<String> spotLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_walk);

        // 이전 액티비티에서 데이터 받아오기
        Intent intent = getIntent();
        myData = intent.getStringExtra("mydata");
        System.out.println("로그: mydata: "+myData);
        recentPosition = intent.getStringArrayListExtra("recentPosition");
        System.out.println("로그: 현위치: " + recentPosition.get(0) + ", " + recentPosition.get(1));
        spotName = intent.getStringArrayListExtra("spotName");
        spotLat = intent.getStringArrayListExtra("spotLat");
        spotLon = intent.getStringArrayListExtra("spotLon");

        // 티맵
        tMapView = new TMapView(this);

        tMapView.setZoomLevel(17);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        LinearLayout Tmap = (LinearLayout) findViewById(R.id.map);
        Tmap.addView(tMapView);

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(1000);
        tMapGPS.setMinDistance(10);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        //tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);

        tMapGPS.OpenGps();
        //

        // 저장 버튼 클릭 시 finish()
        Button save = (Button) findViewById(R.id.save_schedule_btn);
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // 데이터가 저장되는 코드?
                finish();
            }
        });

        // 취소 버튼 클릭 시 finish()
        Button cancel = (Button) findViewById(R.id.cancel_save_schedule);
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void saveWalkClicked(View view) {
        Intent intent = new Intent(this, SaveWalkActivity.class);
        startActivity(intent);
    }
    //
    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
    }

}