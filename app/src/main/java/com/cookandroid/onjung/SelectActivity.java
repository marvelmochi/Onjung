package com.cookandroid.onjung;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

    // 현위치 전달을 위한 SharedPreferences
    SharedPreferences preferences;

    //Intent intentResult = new Intent(SelectActivity.this, ResultActivity.class);

    // 체크박스 선언
    CheckBox toilet, bell, trash, exercise, light;
    CheckBox mountain, park, lake, forest, bridge, river;
    CheckBox heritage, hanok, mural, market, castle, stonewall;

    // 체크된 값들 담을 ArrayList
    ArrayList<CheckBox> spot_c = new ArrayList<>();
    ArrayList<String> spot = new ArrayList<>();
    ArrayList<CheckBox> convenience_c = new ArrayList<>();
    ArrayList<String> convenience = new ArrayList<>();

    // T Map 앱 키
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
    // <경로 관련 데이터>
    //ArrayList<String> recentPosition = new ArrayList(); // 현위치 좌표 {"위도", "경도"}

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
    // 편의사항 관련 데이터
    ArrayList<String> convName_toilet = new ArrayList<>();
    ArrayList<String> convLat_toilet = new ArrayList<>();
    ArrayList<String> convLon_toilet = new ArrayList<>();
    ArrayList<String> convName_bell = new ArrayList<>();
    ArrayList<String> convLat_bell = new ArrayList<>();
    ArrayList<String> convLon_bell = new ArrayList<>();
    ArrayList<String> convName_trash = new ArrayList<>();
    ArrayList<String> convLat_trash = new ArrayList<>();
    ArrayList<String> convLon_trash = new ArrayList<>();
    ArrayList<String> convName_exercise = new ArrayList<>();
    ArrayList<String> convLat_exercise = new ArrayList<>();
    ArrayList<String> convLon_exercise = new ArrayList<>();
    ArrayList<String> convName_light = new ArrayList<>();
    ArrayList<String> convLat_light = new ArrayList<>();
    ArrayList<String> convLon_light = new ArrayList<>();

    //
    int flag_conv;

    // 반경을 입력 받을 시크바
    SeekBar seekBar;
    TextView seekText;
    //원하는 반경 값
    String radius;

    // 호출할 때 필요한 변수 선언
    String type; // 경유지 타입
    String lat1, lat2;
    String lon1, lon2;

    String typeConv; // 편의사항 타입 저장할 변수

    // 예상 소요시간 담을 리스트
    ArrayList<String> timeList = new ArrayList<>();

    ArrayList<String> passList = new ArrayList<>();
    //passList=126.8774698,37.54228826_126.856902,37.530115
    String passList1 = "";
    String passList2 = "";
    String passList3 = "";
    String time1;
    String time2;
    String time3;

    //토스트 메시지를 띄우기 위한 핸들러
    Handler toastHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        Intent intent = getIntent();
        //recentPosition = intent.getStringArrayListExtra("recentPosition");
        //lat = recentPosition.get(0);
        //lon = recentPosition.get(1);
        preferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        lat = preferences.getString("home_lat","");
        lon = preferences.getString("home_lon","");
        System.out.println("로그: Preference로 옮긴 현위치: "+ lat+", "+lon);

        // 체크박스 접근
        toilet = (CheckBox) findViewById(R.id.toilet);
        bell = (CheckBox) findViewById(R.id.bell);
        trash = (CheckBox) findViewById(R.id.trash);
        exercise = (CheckBox) findViewById(R.id.exercise);
        light = (CheckBox) findViewById(R.id.light);

        mountain = (CheckBox) findViewById(R.id.mountain);
        park = (CheckBox) findViewById(R.id.park);
        lake = (CheckBox) findViewById(R.id.lake);
        forest = (CheckBox) findViewById(R.id.forest);
        bridge = (CheckBox) findViewById(R.id.bridge);
        river = (CheckBox) findViewById(R.id.river);

        heritage = (CheckBox) findViewById(R.id.heritage);
        hanok = (CheckBox) findViewById(R.id.hanok);
        mural = (CheckBox) findViewById(R.id.mural);
        market = (CheckBox) findViewById(R.id.market);
        castle = (CheckBox) findViewById(R.id.castle);
        stonewall = (CheckBox) findViewById(R.id.stonewall);

        //
        convenience_c.add(toilet);
        convenience_c.add(bell);
        convenience_c.add(trash);
        convenience_c.add(exercise);
        convenience_c.add(light);

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
        seekBar = (SeekBar) findViewById(R.id.radiusSeekbar);
        seekText = (TextView) findViewById(R.id.seekbarText);
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

        // 토스트핸들러
        toastHandler = new Handler(Looper.getMainLooper());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SelectActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                // 액티비티 이동
                Intent intent = new Intent(SelectActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                //finish();
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
                /////////////////////////////결과 출력시, 동일 장소 중복 출력 방지/////////////////////////////////
                if(!spot.contains(spots)) {
                    spot.add(spots);
                }
                //System.out.println("로그: 산책지 " + spot.get(i));}
            }
        }
        for (int i = 0; i < convenience_c.size(); i++) {
            if (convenience_c.get(i).isChecked()) {
                String conv = convenience_c.get(i).getText().toString();
                convenience.add(conv);
            }
        }


        // 한글 -> type명으로
        //System.out.println("로그: 체크된 선택지: ");
        for (int i = 0; i < spot.size(); i++) {
            if (spot.get(i).equals("산")) spot.set(i, "mountain");
            if (spot.get(i).equals("호수/저수지")) spot.set(i, "lake");
            if (spot.get(i).equals("공원")) spot.set(i, "park");
            if (spot.get(i).equals("숲")) spot.set(i, "forest");
            if (spot.get(i).equals("대교")) spot.set(i, "bridge");
            if (spot.get(i).equals("강/하천")) spot.set(i, "river");

            if (spot.get(i).equals("문화재")) spot.set(i, "heritage");
            if (spot.get(i).equals("한옥마을")) spot.set(i, "hanok");
            if (spot.get(i).equals("벽화")) spot.set(i, "mural");
            if (spot.get(i).equals("시장")) spot.set(i, "market");
            if (spot.get(i).equals("성곽")) spot.set(i, "castle");
            if (spot.get(i).equals("돌담길")) spot.set(i, "stonewall");
            //System.out.println("로그: "+spot.get(i));

        }
        HttpConnectorSelect selectThread = new HttpConnectorSelect();
        selectThread.start();

        for (int i = 0; i < convenience.size(); i++) {
            if (convenience.get(i).equals("공공화장실")) convenience.set(i, "toilet");
            if (convenience.get(i).equals("안심벨")) convenience.set(i, "bell");
            if (convenience.get(i).equals("쓰레기통")) convenience.set(i, "trash");
            if (convenience.get(i).equals("운동기구")) convenience.set(i, "exercise");
            if (convenience.get(i).equals("스마트가로등")) convenience.set(i, "light");
        }
        //HttpConnectorConvenience convenienceThread = new HttpConnectorConvenience();
        //convenienceThread.start();


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

        @Override
        public void run() {
            // 첫 번째 경유지
            for (int i = 0; i < spot.size(); i++) {
                try {
                    String type = spot.get(i);
                    URL url = new URL("http://smwu.onjung.tk/spot/" + radius + "?latitude=" + lat
                            + "&longitude=" + lon + "&type=" + type);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String returnMsg = in.readLine();

                    // 해당되는 산책지가 없을 경우 토스트 메시지 띄우기
                    JSONObject jsonObject = new JSONObject(returnMsg);
                    String code = jsonObject.getString("code");
                    if (code.equals("400")){
                        JSONObject jsonObject1 = new JSONObject(returnMsg);
                        String detail = jsonObject1.getString("detail");
                        ToastMessage(detail);
                        System.out.println("로그: detail: "+detail);

                    }
                    System.out.println("로그: 요청 URL1: " + url);
                    System.out.println("로그: 응답 메시지1: " + returnMsg);

                    // 파싱 메소드 호출
                    jsonParserFirst(returnMsg);


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

                    // 해당되는 산책지가 없을 경우 토스트 메시지 띄우기
                    JSONObject jsonObject = new JSONObject(returnMsg);
                    String code = jsonObject.getString("code");
                    if (code.equals("400")){
                        JSONObject jsonObject1 = new JSONObject(returnMsg);
                        String detail = jsonObject1.getString("detail");
                        ToastMessage(detail);
                        System.out.println("로그: detail: "+detail);

                    }

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

                    // 해당되는 산책지가 없을 경우 토스트 메시지 띄우기
                    JSONObject jsonObject = new JSONObject(returnMsg);
                    String code = jsonObject.getString("code");
                    if (code.equals("400")){
                        JSONObject jsonObject1 = new JSONObject(returnMsg);
                        String detail = jsonObject1.getString("detail");
                        ToastMessage(detail);
                        System.out.println("로그: detail: "+detail);

                    }

                    // 파싱 메소드 호출
                    jsonParserThird(returnMsg);

                } catch (Exception e) {
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

                passList1 += lon + "," + lat + "_";
                System.out.println("로그: passList1: " + passList1);


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

                passList2 += lon + "," + lat + "_";
                System.out.println("로그: passList2: " + passList2);

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

                passList3 += lon + "," + lat + "_";
                System.out.println("로그: passList3: " + passList3);

                HttpConnectorTime TimeThread = new HttpConnectorTime();
                TimeThread.start();


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 파싱 예외발생3");
            }

        }
    }

    class HttpConnectorConvenience extends Thread {
        @Override
        public void run() {
            System.out.println("로그: passList1: " + passList1);
            System.out.println("로그: passList2: " + passList2);
            System.out.println("로그: passList3: " + passList3);
            if (convenience.size() > 0) {
                for (int i = 0; i < convenience.size(); i++) {
                    try {
                        typeConv = convenience.get(i);
                        URL url = new URL("http://smwu.onjung.tk/spot/find/" + radius + "?latitude=" + lat
                                + "&longitude=" + lon + "&type=" + typeConv);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String returnMsg = in.readLine();
                        System.out.println("로그: 요청 URL1: " + url);
                        System.out.println("로그: 응답 메시지1: " + returnMsg);

                        jsonParserConv(returnMsg);

                        // 소요시간 받아오기

                        // 인텐트에 데이터를 담아 전달
                        Intent intentResult = new Intent(SelectActivity.this, ResultActivity.class);
                        intentResult.putExtra("mydata", data);
                        //intentResult.putExtra("recentPosition", recentPosition);
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

                        intentResult.putStringArrayListExtra("convLat_toilet", convLat_toilet);
                        intentResult.putStringArrayListExtra("convLon_toilet", convLon_toilet);
                        intentResult.putStringArrayListExtra("convLat_bell", convLat_bell);
                        intentResult.putStringArrayListExtra("convLon_bell", convLon_bell);
                        intentResult.putStringArrayListExtra("convLat_trash", convLat_trash);
                        intentResult.putStringArrayListExtra("convLon_trash", convLon_trash);
                        intentResult.putStringArrayListExtra("convLat_exercise", convLat_exercise);
                        intentResult.putStringArrayListExtra("convLon_exercise", convLon_exercise);
                        intentResult.putStringArrayListExtra("convLat_light", convLat_light);
                        intentResult.putStringArrayListExtra("convLon_light", convLon_light);

                        intentResult.putStringArrayListExtra("convName_toilet", convName_toilet);
                        intentResult.putStringArrayListExtra("convName_bell", convName_bell);
                        intentResult.putStringArrayListExtra("convName_trash", convName_trash);
                        intentResult.putStringArrayListExtra("convName_exercise", convName_exercise);
                        intentResult.putStringArrayListExtra("convName_light", convName_light);

                        timeList.add(time1);
                        timeList.add(time2);
                        timeList.add(time3);
                        System.out.println("로그: timeList(보내기 직전): " + timeList);
                        intentResult.putStringArrayListExtra("timeList", timeList);

                        flag_conv = 1;
                        intentResult.putExtra("flag_conv", flag_conv);
                        startActivity(intentResult);

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("로그: 편의시설 예외 발생");
                    }
                }
            } else {
                // 인텐트에 데이터를 담아 전달
                Intent intentResult = new Intent(SelectActivity.this, ResultActivity.class);
                intentResult.putExtra("mydata", data);
                //intentResult.putExtra("recentPosition", recentPosition);
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

                timeList.add(time1);
                timeList.add(time2);
                timeList.add(time3);
                System.out.println("로그: timeList(보내기 직전): " + timeList);
                intentResult.putStringArrayListExtra("timeList", timeList);
                flag_conv = 0;
                intentResult.putExtra("flag_conv", flag_conv);

                startActivity(intentResult);
            }
        }

        public void jsonParserConv(String resultJson) {
            try {
                // toilet
                if (typeConv.equals("toilet")) {
                    JSONObject jsonObject = new JSONObject(resultJson);
                    String data = jsonObject.getString("data");
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String name = jsonObject1.getString("name");
                        convName_toilet.add(name);
                        String lat = jsonObject1.getString("latitude");
                        String lon = jsonObject1.getString("longitude");
                        convLat_toilet.add(lat);
                        convLon_toilet.add(lon);
                    }
                }
                // bell
                else if (typeConv.equals("bell")) {
                    JSONObject jsonObject = new JSONObject(resultJson);
                    String data = jsonObject.getString("data");
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String name = jsonObject1.getString("name");
                        convName_bell.add(name);
                        String lat = jsonObject1.getString("latitude");
                        String lon = jsonObject1.getString("longitude");
                        convLat_bell.add(lat);
                        convLon_bell.add(lon);
                    }
                }
                // trash
                else if (typeConv.equals("trash")) {
                    JSONObject jsonObject = new JSONObject(resultJson);
                    String data = jsonObject.getString("data");
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String name = jsonObject1.getString("name");
                        convName_trash.add(name);
                        String lat = jsonObject1.getString("latitude");
                        String lon = jsonObject1.getString("longitude");
                        convLat_trash.add(lat);
                        convLon_trash.add(lon);
                    }
                }
                // exercise
                else if (typeConv.equals("exercise")) {
                    JSONObject jsonObject = new JSONObject(resultJson);
                    String data = jsonObject.getString("data");
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String name = jsonObject1.getString("name");
                        convName_exercise.add(name);
                        String lat = jsonObject1.getString("latitude");
                        String lon = jsonObject1.getString("longitude");
                        convLat_exercise.add(lat);
                        convLon_exercise.add(lon);
                    }
                }
                // light
                else if (typeConv.equals("light")) {
                    JSONObject jsonObject = new JSONObject(resultJson);
                    String data = jsonObject.getString("data");
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String name = jsonObject1.getString("name");
                        convName_light.add(name);
                        String lat = jsonObject1.getString("latitude");
                        String lon = jsonObject1.getString("longitude");
                        convLat_light.add(lat);
                        convLon_light.add(lon);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 편의사항 파싱 예외 발생");
            }


        }
    }

    class HttpConnectorTime extends Thread {
        URL url;
        HttpURLConnection conn;

        int flag = 1;

        @Override
        public void run() {
            try {
                passList1 = passList1.substring(0, passList1.length() - 1);
                passList2 = passList2.substring(0, passList2.length() - 1);
                passList3 = passList3.substring(0, passList3.length() - 1);
                passList.add(passList1);
                passList.add(passList2);
                passList.add(passList3);

                for (int i = 0; i < 3; i++) {
                    url = new URL("https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json&appKey=" + API_Key
                            + "&startX=" + lon + "&startY=" + lat + "&endX=" + lon + "&endY=" + lat +
                            "&startName=start&endName=end" + "&searchOption=10" +
                            "&passList=" + passList.get(i));
                    System.out.println("로그: url: " + url);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String returnMsg = in.readLine();
                    //System.out.println("로그: 응답 메시지: " + returnMsg);
                    jsonParserTime(returnMsg);
                    flag += 1;

                }

                HttpConnectorConvenience convenienceThread = new HttpConnectorConvenience();
                convenienceThread.start();
                System.out.println("로그: 저장된 timeList: " + timeList);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 소요시간 통신 예외 발생");
                timeList.add("?");
                timeList.add("?");
                timeList.add("?");
                HttpConnectorConvenience convenienceThread = new HttpConnectorConvenience();
                convenienceThread.start();

            }
        }

        public void jsonParserTime(String resultJson) {
            try {
                // 응답으로 받은 데이터를 JSONObject에 넣음
                JSONObject jsonObject = new JSONObject(resultJson);
                String features = jsonObject.getString("features");

                JSONArray jsonArray = new JSONArray(features);
                String features2 = jsonArray.get(0).toString();

                JSONObject jsonObject1 = new JSONObject(features2);
                String properties = jsonObject1.getString("properties");

                JSONObject jsonObject2 = new JSONObject(properties);
                String totalTime_s = jsonObject2.getString("totalTime");
                System.out.println("로그: totalTime: " + totalTime_s);

                if (flag == 1) {
                    time1 = totalTime_s;
                } else if (flag == 2) {
                    time2 = totalTime_s;
                } else if (flag == 3) {
                    time3 = totalTime_s;
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 파싱 예외 발생");
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
