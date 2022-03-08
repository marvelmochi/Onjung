package com.cookandroid.onjung;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SelectActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback {

    // 조건에 맞는 산책로 추천 구현 방식 (SelectActivity에서 API 호출 후 받아온 데이터 값 ResultActivity로 넘김)

    // 첫 번째 경로: 현 위치 - API 7번 (현 위치와 반경 내에서 가장 가까운 경유지)
    // 두 번째 경로: 현 위치 - API 4번 (현 위치와 가장 가까운 경유지)
    // 세 번째 경로: 현 위치 - API 4번 (현 위치와 가장 가까운 경유지)

    // 인텐트로 액티비티 간 데이터 전달
    String data = "success";

    // 체크박스 선언
    CheckBox mountain, park, lake, forest, bridge, river;
    CheckBox heritage, hanok, mural, market, castle, stonewall;

    // 체크된 값들 담을 ArrayList
    ArrayList<CheckBox> spot_c = new ArrayList<>();
    ArrayList<String> spot = new ArrayList<>();

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    // T Map GPS
    TMapGpsManager tMapGPS = null;
    // 현위치 좌표 저장할 변수 선언;
    double lat_d;
    double lon_d;
    String lat;
    String lon;

    // 파싱을 위한 변수 선언
    JSONArray jsonArraydata = new JSONArray();

    // 인텐트에 담아 전달할 데이터 배열 선언

    ArrayList<String> recentPosition = new ArrayList(); // 현위치 좌표 {"위도", "경도"}

    ArrayList<String> FirstName = new ArrayList<>();
    ArrayList<String> FirstLat = new ArrayList<>();
    ArrayList<String> FirstLon = new ArrayList<>();
    ArrayList<String> FirstSpotId = new ArrayList<>();

    ArrayList<String> SecondName = new ArrayList<>();
    ArrayList<String> SecondLat = new ArrayList<>();
    ArrayList<String> SecondLon = new ArrayList<>();
    ArrayList<String> SecondSpotId = new ArrayList<>();

    ArrayList<String> ThirdName = new ArrayList<>();
    ArrayList<String> ThirdLat = new ArrayList<>();
    ArrayList<String> ThirdLon = new ArrayList<>();
    ArrayList<String> ThirdSpotId = new ArrayList<>();

    // 반경을 입력 받을 시크바
    SeekBar seekBar;
    TextView seekText;
    //원하는 반경 값
    String radius;

    // 호출할 때 필요한 변수 선언
    String type; // 경유지 타입
    String lat1, lat2;
    String lon1, lon2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        Intent intent = getIntent();
        recentPosition = intent.getStringArrayListExtra("recentPosition");
        lat = recentPosition.get(0);
        lon = recentPosition.get(1);

        // 체크박스 접근
        mountain = (CheckBox) findViewById(R.id.mountain);
        park = (CheckBox) findViewById(R.id.park);
        lake = (CheckBox) findViewById(R.id.lake);
        forest = (CheckBox) findViewById(R.id.forest);
        bridge = (CheckBox) findViewById(R.id.bridge);
        river= (CheckBox) findViewById(R.id.river);

        heritage= (CheckBox) findViewById(R.id.heritage);
        hanok= (CheckBox) findViewById(R.id.hanok);
        mural= (CheckBox) findViewById(R.id.mural);
        market= (CheckBox) findViewById(R.id.market);
        castle= (CheckBox) findViewById(R.id.castle);
        stonewall= (CheckBox) findViewById(R.id.stonewall);


        spot_c.add(mountain);
        spot_c.add(park);
        spot_c.add(lake);
        spot_c.add(forest);
        spot_c.add(bridge);
        spot_c.add(river);

        spot_c.add(heritage);
        spot_c.add(hanok);
        spot_c.add(mural);
        spot_c.add(market);
        spot_c.add(castle);
        spot_c.add(stonewall);

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);
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

        tMapGPS.OpenGps();

        // 시크바 접근 및 이벤트 등록
        seekBar = (SeekBar)findViewById(R.id.radiusSeekbar);
        seekText = (TextView)findViewById(R.id.seekbarText);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekText.setText(String.format("%dkm", seekBar.getProgress()));
                radius = Integer.toString(seekBar.getProgress());
            }
        });

        // 툴바: 뒤로가기 버튼
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                // 액티비티 이동
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChange(Location location) {
        /* 위치 좌표 찾는 시간 텀이 길어져서 메인에서 받아온 위치를 현위치로.
        // 내 위치 좌표 home에 저장
        TMapPoint home = tMapGPS.getLocation();
        lat_d = home.getLatitude();
        lon_d = home.getLongitude();
        lat = Double.toString(lat_d);
        lon = Double.toString(lon_d);

        // 배열에 현위치 담기
        recentPosition.add(lat);
        recentPosition.add(lon);

        System.out.println("로그: 현위치 좌표 " + lat + ", " + lon);

         */
    }

    public void resultbtnClicked(View view) {

        for (int i = 0; i < spot_c.size(); i++) {
            if (spot_c.get(i).isChecked()) {
                String spots = spot_c.get(i).getText().toString();
                spot.add(spots);
                //System.out.println("로그: 산책지 " + spot.get(i));}
            }
        }


        // 한글 -> type명으로
        //System.out.println("로그: 체크된 선택지: ");
        for (int i = 0; i < spot.size(); i++) {
            if (spot.get(i).equals("산")) spot.set(i, "mountain");
            if (spot.get(i).equals("호수/저수지")) spot.set(i, "lake");
            if (spot.get(i).equals("공원"))spot.set(i,"park");
            if (spot.get(i).equals("숲"))spot.set(i,"forest");
            if (spot.get(i).equals("대교"))spot.set(i,"bridge");
            if (spot.get(i).equals("강/하천"))spot.set(i,"river");

            if (spot.get(i).equals("문화재"))spot.set(i,"heritage");
            if (spot.get(i).equals("한옥마을"))spot.set(i,"hanok");
            if (spot.get(i).equals("벽화"))spot.set(i,"mural");
            if (spot.get(i).equals("시장"))spot.set(i,"market");
            if (spot.get(i).equals("성곽"))spot.set(i,"castle");
            if (spot.get(i).equals("돌담길"))spot.set(i,"stonewall");
            //System.out.println("로그: "+spot.get(i));

        }
        HttpConnectorSelect selectThread = new HttpConnectorSelect();
        selectThread.start();




        // 인텐트 전달
/*
        Intent intentResult = new Intent(SelectActivity.this, ResultActivity.class);
        intentResult.putExtra("recentPosition", recentPosition);
        intentResult.putStringArrayListExtra("spotName", spotName);
        intentResult.putStringArrayListExtra("spotLat", spotLat);
        intentResult.putStringArrayListExtra("spotLon", spotLon);
        intentResult.putExtra("spotId", spotId);
        startActivity(intentResult);

*/

    }

    class HttpConnectorSelect extends Thread {
        Message message;
        Bundle bundle = new Bundle();

        @Override
        public void run() {
            // 첫 번째 경유지
            for (int i = 0; i < spot.size(); i++) {
                try {

                    // 첫 번째 경로
                    String type = spot.get(i);
                    URL url = new URL("http://smwu.onjung.tk/spot/" + radius + "?latitude=" + lat
                            + "&longitude=" + lon + "&type=" + type);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String returnMsg = in.readLine();
                    System.out.println("로그: 요청 URL1: " + url);
                    System.out.println("로그: 응답 메시지1: " + returnMsg);

                    // 파싱 메소드 호출
                    jsonParserFirst(returnMsg);

                    /* (세 번째 파싱 이후에 넣기)
                    Intent intentResult = new Intent(SelectActivity.this, ResultActivity.class);
                    intentResult.putExtra("mydata", data);
                    intentResult.putExtra("recentPosition", recentPosition);
                    intentResult.putExtra("spotName", spotName);
                    intentResult.putExtra("spotLat", spotLat);
                    intentResult.putExtra("spotLon", spotLon);
                    intentResult.putExtra("spotId", spotId);

                    startActivity(intentResult);

                     */


                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("로그: 산책지 불러오기 예외발생1");
                }
            }

            // 두 번째 경유지
            for (int i = 0; i < spot.size(); i++) {
                try {
                    String type = spot.get(i);
                    URL url = new URL("http://smwu.onjung.tk/spot/?latitude=" + lat1
                            + "&longitude=" + lon1 + "&type=" + type);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String returnMsg = in.readLine();
                    System.out.println("로그: 요청 URL2: " + url);
                    System.out.println("로그: 응답 메시지2: " + returnMsg);

                    // 파싱 메소드 호출
                    jsonParserSecond(returnMsg);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("로그: 산책지 불러오기 예외발생2");
                }
            }

            // 세 번째 경유지
            for (int i = 0; i < spot.size(); i++) {
                try {
                    String type = spot.get(i);
                    URL url = new URL("http://smwu.onjung.tk/spot/?latitude=" + lat2
                            + "&longitude=" + lon2 + "&type=" + type);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String returnMsg = in.readLine();
                    System.out.println("로그: 요청 URL3: " + url);
                    System.out.println("로그: 응답 메시지3: " + returnMsg);

                    // 파싱 메소드 호출
                    jsonParserThird(returnMsg);

                    // 인텐트에 데이터를 담아 전달
                    Intent intentResult = new Intent(SelectActivity.this, ResultActivity.class);
                    intentResult.putExtra("mydata", data);
                    intentResult.putExtra("recentPosition", recentPosition);
                    intentResult.putStringArrayListExtra("FirstName", FirstName);
                    intentResult.putStringArrayListExtra("SecondName", SecondName);
                    intentResult.putStringArrayListExtra("ThirdName", ThirdName);
                    intentResult.putStringArrayListExtra("FirstLat", FirstLat);
                    intentResult.putStringArrayListExtra("SecondLat", SecondLat);
                    intentResult.putStringArrayListExtra("ThirdLat", ThirdLat);
                    intentResult.putStringArrayListExtra("FirstLon", FirstLon);
                    intentResult.putStringArrayListExtra("SecondLon", SecondLon);
                    intentResult.putStringArrayListExtra("ThirdLon", ThirdLon);
                    intentResult.putStringArrayListExtra("FirstSpotId", FirstSpotId);
                    intentResult.putStringArrayListExtra("SecondSpotId", SecondSpotId);
                    intentResult.putStringArrayListExtra("ThirdSpotId", ThirdSpotId);

                    startActivity(intentResult);

                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("로그: 산책지 불러오기 예외발생3");
                }
            }

        }

        public void jsonParserFirst(String resultJson) {
            try {
                JSONObject jsonObject = new JSONObject(resultJson);
                String data = jsonObject.getString("data");
                jsonArraydata.put(data);

                JSONObject dataObject = new JSONObject(data);
                System.out.println("로그: dataObject: " + dataObject);

                String spotj = dataObject.getString("spot");
                JSONObject spotObject = new JSONObject(spotj);

                String name = spotObject.getString("name");
                String lat = spotObject.getString("latitude");
                String lon = spotObject.getString("longitude");
                int spotid_int = spotObject.getInt("spotId");
                String spotid = Integer.toString(spotid_int);

                FirstName.add(name);
                FirstLat.add(lat);
                FirstLon.add(lon);
                FirstSpotId.add(spotid);

                lat1 = lat;
                lon1 = lon;

                System.out.println("로그: lat1, lon1: " + lat1 + ", " + lon1);


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 파싱 예외발생");
            }
            /*
            for (int i=0; i<spotId.size(); i++){
                System.out.println("로그: 스팟아이디: "+spotId.get(i));
            }

             */
        }

        public void jsonParserSecond(String resultJson) {
            try {
                JSONObject jsonObject = new JSONObject(resultJson);
                String data = jsonObject.getString("data");
                jsonArraydata.put(data);

                JSONObject dataObject = new JSONObject(data);
                System.out.println("로그: dataObject: " + dataObject);

                String spotj = dataObject.getString("spot");
                JSONObject spotObject = new JSONObject(spotj);

                String name = spotObject.getString("name");
                String lat = spotObject.getString("latitude");
                String lon = spotObject.getString("longitude");
                int spotid_int = spotObject.getInt("spotId");
                String spotid = Integer.toString(spotid_int);

                SecondName.add(name);
                SecondLat.add(lat);
                SecondLon.add(lon);
                SecondSpotId.add(spotid);

                lat2 = lat;
                lon2 = lon;

                System.out.println("로그: lat2, lon2: " + lat2 + ", " + lon2);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 파싱 예외발생2");
            }

        }

        public void jsonParserThird(String resultJson) {
            try {
                JSONObject jsonObject = new JSONObject(resultJson);
                String data = jsonObject.getString("data");
                jsonArraydata.put(data);

                JSONObject dataObject = new JSONObject(data);
                System.out.println("로그: dataObject: " + dataObject);

                String spotj = dataObject.getString("spot");
                JSONObject spotObject = new JSONObject(spotj);

                String name = spotObject.getString("name");
                String lat = spotObject.getString("latitude");
                String lon = spotObject.getString("longitude");
                int spotid_int = spotObject.getInt("spotId");
                String spotid = Integer.toString(spotid_int);

                ThirdName.add(name);
                ThirdLat.add(lat);
                ThirdLon.add(lon);
                ThirdSpotId.add(spotid);

                System.out.println("로그: lat3, lon3: " + lat + ", " + lon);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 파싱 예외발생3");
            }

        }
    }



}
