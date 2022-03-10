package com.cookandroid.onjung;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

public class WalkingActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback{
    // <T Map 관련>
    String API_Key = "l7xxe7f1a877c28248569287eb5a543c89e9";
    TMapView tMapView = null;
    // T Map GPS
    TMapGpsManager tMapGPS = null;
    TMapData tmapdata;
    TMapPolyLine polyline;
    TMapPoint home;

    Context context;

    TextView timeText;
    Button startBtn;
    Button finishBtn;


    Thread timeThread;
    boolean isRunning = true;
    int flag_watch = 0;

    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);

        preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String home_lat = preferences.getString("home_lat","");
        String home_lon = preferences.getString("home_lon","");
        home = new TMapPoint(Double.parseDouble(home_lat), Double.parseDouble(home_lon));

        context = this;
        timeText = (TextView) findViewById(R.id.timeText);
        startBtn = (Button) findViewById(R.id.startBtn);
        finishBtn = (Button) findViewById(R.id.finishBtn);

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);
        // Initial Setting
        tMapGPS.setMinTime(1); // default 1000
        tMapGPS.setMinDistance((float) 0.1); // default 10
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
        tMapView.setCenterPoint(home.getLongitude(), home.getLatitude());
        tMapView.setTrackingMode(false);
        LinearLayout Tmap = (LinearLayout) findViewById(R.id.map);
        Tmap.addView(tMapView);
        Bitmap marker_start = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_start);
        tMapView.setTMapPathIcon(marker_start, marker_start); // 출발지 아이콘 설정

        tmapdata = new TMapData();

        /*
        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, home, home, passList, 10, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapPolyLine.setLineColor(Color.BLUE);
                tMapView.addTMapPath(tMapPolyLine);

            }

        });

         */
        polyline = new TMapPolyLine();
        polyline.setLineColor(Color.BLUE);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag_watch == 0) {
                    timeThread = new Thread(new timeThread());
                    timeThread.start();
                }
                flag_watch = 1;
                System.out.println("로그: flag_watch: " + flag_watch);
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag_watch = 0;
                System.out.println("로그: flag_watch: " + flag_watch);
                timeThread.interrupt();
                tMapGPS.CloseGps();
            }
        });



    }

    @Override
    public void onLocationChange(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        TMapPoint point = new TMapPoint(lat, lon);

        polyline.addLinePoint(point);
        tMapView.addTMapPolyLine("realtime", polyline);

        System.out.println("로그: flag_watch: " + flag_watch);
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //int mSec = msg.arg1 % 100;
            int sec = (msg.arg1 / 100) % 60;
            int min = (msg.arg1 / 100) / 60;
            int hour = (msg.arg1 / 100) / 360;
            //1000이 1초 1000*60 은 1분 1000*60*10은 10분 1000*60*60은 한시간
            @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d:%02d", hour, min, sec);
            timeText.setText(result);
        }
    };

    public class timeThread implements Runnable {
        @Override
        public void run() {
            int i = 0;

            while (true) {
                while (isRunning) { //일시정지를 누르면 멈춤
                    Message msg = new Message();
                    msg.arg1 = i++;
                    handler.sendMessage(msg);

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //mTimeTextView.setText("");
                                //mTimeTextView.setText("00:00:00:00");
                            }
                        });
                        return; // 인터럽트 받을 경우 return
                    }
                }
            }
        }


    }
}
