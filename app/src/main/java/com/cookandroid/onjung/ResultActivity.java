package com.cookandroid.onjung;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapView;

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
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey(API_Key);

        tMapView.setZoomLevel(17);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

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


        /* 보행자 경로 표시하기
        TMapPoint startPoint = new TMapPoint(37.532448, 126.846433);
        TMapPoint endPoint = new TMapPoint(37.527586, 126.8298543);
        TMapData tmapdata = null;

        try {
            TMapPolyLine tMapPolyLine = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startPoint, endPoint);
            tMapPolyLine.setLineColor(Color.BLUE);
            tMapPolyLine.setLineWidth(5);
            tMapView.addTMapPolyLine("onjung", tMapPolyLine);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
         */


        /*new Thread(){
            @Override
            public void run(){
                try {
                    TMapPolyLine tMapPolyLine = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startPoint, endPoint);
                    tMapPolyLine.setLineColor(Color.BLUE);
                    tMapPolyLine.setLineWidth(5);
                    tMapView.addTMapPolyLine("Line1", tMapPolyLine);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start(); */

        //
    }



    public void saveWalkClicked(View view) {
        Intent intent = new Intent(this, SaveWalkActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
    }


}

