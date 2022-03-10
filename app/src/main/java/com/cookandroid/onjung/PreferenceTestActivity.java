package com.cookandroid.onjung;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

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


public class PreferenceTestActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback {

    // 취향을 분석한 산책로 추천 (평가 이전 ver.)
    // (PreferenceTestActivity에서 API 호출 후
    // 받아온 데이터 값 PreferenceTestResultActivity로 넘김)

    // API 15-1번(평가 이전) 사용 하여 경로 3개 구현

    // T Map 앱 키
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";

    // 체크박스 선언
    CheckBox mountain, river, forest, lake, park;
    CheckBox active, quiet, walkable, sight, pet, sightseeing, exercise;

    // 체크박스 체크 여부(분위기)를 담을 ArrayList
    ArrayList<String> checkedMoodList;
    // 타입을 담을 ArrayList
    ArrayList<CheckBox> typeList;
    // 분위기 타입을 담을 ArrayList
    ArrayList<CheckBox> moodList;

    // 체크된 값들 담을 ArrayList
    ArrayList<String> checkedTypeList = new ArrayList<>();

    // 체크된 분위기 타입
    String c_active, c_quiet, c_walkable, c_sight, c_pet, c_sightseeing, c_exercise;

    // userId(memberId)
    String userId;
    // Using SharedPreferences
    SharedPreferences preferences;

    // T Map 앱 키 등록
    //String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    // T Map GPS
    TMapGpsManager tMapGPS = null;

    ArrayList<String> recentPosition = new ArrayList<>(); // 현위치 좌표 {"위도", "경도"}

    // 인텐트에 담아 전달할 데이터 배열 선언
    // 첫 번째 경로 경유지 위경도 배열
    ArrayList<String> firstPoint_lat = new ArrayList<>();
    ArrayList<String> firstPoint_lon = new ArrayList<>();
    // 두 번째 경로 경유지 위경도 배열
    ArrayList<String> secondPoint_lat = new ArrayList<>();
    ArrayList<String> secondPoint_lon = new ArrayList<>();
    // 세 번째 경로 경유지 위경도 배열
    ArrayList<String> thirdPoint_lat = new ArrayList<>();
    ArrayList<String> thirdPoint_lon = new ArrayList<>();

    // 경유지 이름 배열
    ArrayList<String> FirstName = new ArrayList<>();
    ArrayList<String> SecondName = new ArrayList<>();
    ArrayList<String> ThirdName = new ArrayList<>();

    // spotId 배열(받을 때: int, 보낼 때: String)
    ArrayList<String> firstSpotId = new ArrayList<>();
    ArrayList<String> secondSpotId = new ArrayList<>();
    ArrayList<String> thirdSpotId = new ArrayList<>();

    // <소요시간 관련 데이터 선언>
    ArrayList<String> timeList = new ArrayList<>();
    ArrayList<String> passList = new ArrayList<>();
    String passList1 = "";
    String passList2 = "";
    String passList3 = "";
    String time1;
    String time2;
    String time3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test);


        // 툴바: 뒤로가기 버튼
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Using SharedPreferences / memberId, 현위치 받아오기
        preferences = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        userId = preferences.getString("memberId", "");
        String home_lat = preferences.getString("home_lat", "");
        String home_lon = preferences.getString("home_lon", "");
        recentPosition.add(home_lat);
        recentPosition.add(home_lon);
        System.out.println("로그: 취향 테스트 현위치: " + recentPosition.get(0) + ", " + recentPosition.get(1));

        // ArrayList 생성
        typeList = new ArrayList<>();
        checkedMoodList = new ArrayList<>();
        moodList = new ArrayList<>();

        // 체크박스 접근
        mountain = (CheckBox) findViewById(R.id.mountain);
        river = (CheckBox) findViewById(R.id.mood_river);
        forest = (CheckBox) findViewById(R.id.mood_forest);
        lake = (CheckBox) findViewById(R.id.mood_lake);
        park = (CheckBox) findViewById(R.id.mood_park);

        active = (CheckBox) findViewById(R.id.active);
        quiet = (CheckBox) findViewById(R.id.quiet);
        walkable = (CheckBox) findViewById(R.id.walkable);
        sight = (CheckBox) findViewById(R.id.sight);
        pet = (CheckBox) findViewById(R.id.pet);
        sightseeing = (CheckBox) findViewById(R.id.sightseeing);
        exercise = (CheckBox) findViewById(R.id.exercise);

        typeList.add(mountain);
        typeList.add(river);
        typeList.add(forest);
        typeList.add(lake);
        typeList.add(park);

        moodList.add(active);
        moodList.add(quiet);
        moodList.add(walkable);
        moodList.add(sight);
        moodList.add(pet);
        moodList.add(sightseeing);
        moodList.add(exercise);

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(1000);
        tMapGPS.setMinDistance(10);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);

        tMapGPS.OpenGps();

    }

    //뒤로가기 툴바
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                // 액티비티 이동
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChange(Location location) {
        /*
        // 내 위치 좌표 home에 저장
        TMapPoint home = tMapGPS.getLocation();
        home_lat = home.getLatitude();
        home_lon = home.getLongitude();
        home_lat_s = Double.toString(home_lat);
        home_lon_s = Double.toString(home_lon);
        // 배열에 현위치 담기
        recentPosition.add(home_lat_s);
        recentPosition.add(home_lon_s);

        //System.out.println("로그: 현위치 좌표 " + home_lat_s + ", " + home_lon_s);
        System.out.println("로그: 취향 테스트 현위치: " + recentPosition.get(0) + ", " + recentPosition.get(1));

         */
    }

    public void testResultClicked(View view) {

        // 체크된 타입을 String 변수에 저장
        for (int i = 0; i < typeList.size(); i++) {
            if (typeList.get(i).isChecked()) {
                String type = typeList.get(i).getText().toString();
                checkedTypeList.add(type);
            }
        }

        for (int i = 0; i < checkedTypeList.size(); i++) {
            if (checkedTypeList.get(i).equals("산")) checkedTypeList.set(i, "mountain");
            if (checkedTypeList.get(i).equals("강")) checkedTypeList.set(i, "river");
            if (checkedTypeList.get(i).equals("숲")) checkedTypeList.set(i, "forest");
            if (checkedTypeList.get(i).equals("호수")) checkedTypeList.set(i, "lake");
            if (checkedTypeList.get(i).equals("공원")) checkedTypeList.set(i, "park");

        }

        // 분위기 체크 여부 ArrayList에 저장
        for (int i = 0; i < moodList.size(); i++) {
            if (moodList.get(i).isChecked()) {
                // 체크 되어있다면 1 저장
                checkedMoodList.add("1");
            } else {
                //체크 안 되어있다면 0 저장
                checkedMoodList.add("0");
            }
        }

        c_active = checkedMoodList.get(0);
        c_quiet = checkedMoodList.get(1);
        c_walkable = checkedMoodList.get(2);
        c_sight = checkedMoodList.get(3);
        c_pet = checkedMoodList.get(4);
        c_sightseeing = checkedMoodList.get(5);
        c_exercise = checkedMoodList.get(6);

        // 통신부
        HttpConnectorMood moodThread = new HttpConnectorMood();
        moodThread.start();
    }

    class HttpConnectorMood extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            for (int z = 0; z < checkedTypeList.size(); z++) {
                try {
                    String type;
                    type = checkedTypeList.get(z);
                    url = new URL("http://smwu.onjung.tk/mood?active=" + c_active + "&quiet=" + c_quiet +
                            "&walkable=" + c_walkable + "&sight=" + c_sight + "&pet=" + c_pet + "&sightseeing=" +
                            c_sightseeing + "&exercise=" + c_exercise + "&latitude=" + recentPosition.get(0) + "&longitude=" +
                            recentPosition.get(1) + "&type=" + type + "&userId=" + userId);
                    System.out.println("로그: 통신 URL: " + url);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String returnMsg = in.readLine();
                    System.out.println("로그: 응답 메시지: " + returnMsg);
                    jsonParser(returnMsg);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("로그: 분위기 추천(평가 이력X) 예외 발생");
                }
            }
            // Time Thread
            HttpConnectorTime TimeThread = new HttpConnectorTime();
            TimeThread.start();
        }

    }

    public void jsonParser(String resultJson) {
        try {
            // 응답으로 받은 데이터를 JSONObject에 넣음
            JSONObject jsonObject = new JSONObject(resultJson);
            // JSONObject에서 "data" 부분을 추출
            String data = jsonObject.getString("data");
            System.out.println("로그: data: " + data);
            JSONArray dataArray = new JSONArray(data);

            if (dataArray.length() == 3) {
                System.out.println("로그: 응답값이 3개 왔음");
                // 첫 번째 경유지
                // 이름
                String dataFirst = dataArray.get(0).toString();
                JSONObject objectFirst = new JSONObject(dataFirst);
                String name1 = objectFirst.getString("name");
                FirstName.add(name1);
                // 위경도
                String lat1_s = objectFirst.getString("latitude");
                String lon1_s = objectFirst.getString("longitude");
                firstPoint_lat.add(lat1_s);
                firstPoint_lon.add(lon1_s);
                passList1 += lon1_s + "," + lat1_s + "_";
                // spotId
                int spotId1_i = objectFirst.getInt("spotId");
                String spotId1 = Integer.toString(spotId1_i);
                firstSpotId.add(spotId1);

                // 두 번째 경유지
                // 이름
                String dataSecond = dataArray.get(1).toString();
                JSONObject objectSecond = new JSONObject(dataSecond);
                String name2 = objectSecond.getString("name");
                SecondName.add(name2);
                // 위경도
                String lat2_s = objectSecond.getString("latitude");
                String lon2_s = objectSecond.getString("longitude");
                secondPoint_lat.add(lat2_s);
                secondPoint_lon.add(lon2_s);
                passList2 += lon2_s + "," + lat2_s + "_";
                // spotId
                int spotId2_i = objectSecond.getInt("spotId");
                String spotId2 = Integer.toString(spotId2_i);
                secondSpotId.add(spotId2);

                // 세 번째 경유지
                // 이름
                String dataThird = dataArray.get(2).toString();
                JSONObject objectThird = new JSONObject(dataThird);
                String name3 = objectThird.getString("name");
                ThirdName.add(name3);
                // 위경도
                String lat3_s = objectThird.getString("latitude");
                String lon3_s = objectThird.getString("longitude");
                thirdPoint_lat.add(lat3_s);
                thirdPoint_lon.add(lon3_s);
                passList3 += lon3_s + "," + lat3_s + "_";
                // spotId
                int spotId3_i = objectThird.getInt("spotId");
                String spotId3 = Integer.toString(spotId3_i);
                thirdSpotId.add(spotId3);

            } else if (dataArray.length() == 2) {
                System.out.println("로그: 응답값이 2개 왔음");
                // 첫 번째 경유지
                // 이름
                String dataFirst = dataArray.get(0).toString();
                JSONObject objectFirst = new JSONObject(dataFirst);
                String name1 = objectFirst.getString("name");
                FirstName.add(name1);
                // 위경도
                String lat1_s = objectFirst.getString("latitude");
                String lon1_s = objectFirst.getString("longitude");
                firstPoint_lat.add(lat1_s);
                firstPoint_lon.add(lon1_s);
                passList1 += lon1_s + "," + lat1_s + "_";
                // spotId
                int spotId1_i = objectFirst.getInt("spotId");
                String spotId1 = Integer.toString(spotId1_i);
                firstSpotId.add(spotId1);

                // 두 번째 경유지
                // 이름
                String dataSecond = dataArray.get(1).toString();
                JSONObject objectSecond = new JSONObject(dataSecond);
                String name2 = objectSecond.getString("name");
                SecondName.add(name2);
                // 위경도
                String lat2_s = objectSecond.getString("latitude");
                String lon2_s = objectSecond.getString("longitude");
                secondPoint_lat.add(lat2_s);
                secondPoint_lon.add(lon2_s);
                passList2 += lon2_s + "," + lat2_s + "_";
                // spotId
                int spotId2_i = objectSecond.getInt("spotId");
                String spotId2 = Integer.toString(spotId2_i);
                secondSpotId.add(spotId2);

                // 세 번째 경유지 (임의로 첫 번째 경유지를 넣음)
                // 이름
                ThirdName.add(name1);
                // 위경도
                thirdPoint_lat.add(lat1_s);
                thirdPoint_lon.add(lon1_s);
                passList3 += lon1_s + "," + lat1_s + "_";
                // spotId
                thirdSpotId.add(spotId1);


            } else if (dataArray.length() == 1) {
                System.out.println("로그: 응답값이 1개 왔음");
                // 첫 번째 경유지
                // 이름
                String dataFirst = dataArray.get(0).toString();
                JSONObject objectFirst = new JSONObject(dataFirst);
                String name1 = objectFirst.getString("name");
                FirstName.add(name1);
                // 위경도
                String lat1_s = objectFirst.getString("latitude");
                String lon1_s = objectFirst.getString("longitude");
                firstPoint_lat.add(lat1_s);
                firstPoint_lon.add(lon1_s);
                passList1 += lon1_s + "," + lat1_s + "_";
                // spotId
                int spotId1_i = objectFirst.getInt("spotId");
                String spotId1 = Integer.toString(spotId1_i);
                firstSpotId.add(spotId1);

                // 두 번째 경유지 (임의로 첫 번째 경유지를 넣음)
                SecondName.add(name1); // 이름
                secondPoint_lat.add(lat1_s); // 위도
                secondPoint_lon.add(lon1_s); // 경도
                passList2 += lon1_s + "," + lat1_s + "_";
                secondSpotId.add(spotId1);

                // 세 번째 경유지 (임의로 첫 번째 경유지를 넣음)
                ThirdName.add(name1);
                thirdPoint_lat.add(lat1_s);
                thirdPoint_lon.add(lon1_s);
                passList3 += lon1_s + "," + lat1_s + "_";
                thirdSpotId.add(spotId1);
            }

            for (int i = 0; i < FirstName.size(); i++) {
                System.out.println("로그: FirstName: " + i + "번째: " + FirstName.get(i));
                System.out.println("로그: SecondName: " + i + "번째: " + SecondName.get(i));
                System.out.println("로그: ThirdName: " + i + "번째: " + ThirdName.get(i));

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("로그: 파싱 예외 발생");
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
                            + "&startX=" + recentPosition.get(1) + "&startY=" + recentPosition.get(0) +
                            "&endX=" + recentPosition.get(1) + "&endY=" + recentPosition.get(0) +
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
                System.out.println("로그: 저장된 timeList: " + timeList);


                // 인텐트 전달
                Intent intentResult = new Intent(PreferenceTestActivity.this, PreferenceTestResultActivity.class);

                // 경로 3개 경유지 티맵포인트 배열 전달
                intentResult.putStringArrayListExtra("firstPoint_lat", firstPoint_lat);
                intentResult.putStringArrayListExtra("firstPoint_lon", firstPoint_lon);
                intentResult.putStringArrayListExtra("secondPoint_lat", secondPoint_lat);
                intentResult.putStringArrayListExtra("secondPoint_lon", secondPoint_lon);
                intentResult.putStringArrayListExtra("thirdPoint_lat", thirdPoint_lat);
                intentResult.putStringArrayListExtra("thirdPoint_lon", thirdPoint_lon);

                intentResult.putStringArrayListExtra("FirstName", FirstName);
                intentResult.putStringArrayListExtra("SecondName", SecondName);
                intentResult.putStringArrayListExtra("ThirdName", ThirdName);

                intentResult.putStringArrayListExtra("firstSpotId", firstSpotId);
                intentResult.putStringArrayListExtra("secondSpotId", secondSpotId);
                intentResult.putStringArrayListExtra("thirdSpotId", thirdSpotId);

                timeList.add(time1);
                timeList.add(time2);
                timeList.add(time3);
                System.out.println("로그: timeList(보내기 직전): " + timeList);
                intentResult.putStringArrayListExtra("timeList", timeList);

                startActivity(intentResult);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 소요시간 통신 예외 발생");
                // 소요시간은 통신 오류가 자주 발생하므로 오류가 발생해도 다음 액티비티로 넘어갈 수 있도록 처리
                timeList.add("?");
                timeList.add("?");
                timeList.add("?");

                // 인텐트 전달
                Intent intentResult = new Intent(PreferenceTestActivity.this, PreferenceTestResultActivity.class);

                // 경로 3개 경유지 티맵포인트 배열 전달
                intentResult.putStringArrayListExtra("firstPoint_lat", firstPoint_lat);
                intentResult.putStringArrayListExtra("firstPoint_lon", firstPoint_lon);
                intentResult.putStringArrayListExtra("secondPoint_lat", secondPoint_lat);
                intentResult.putStringArrayListExtra("secondPoint_lon", secondPoint_lon);
                intentResult.putStringArrayListExtra("thirdPoint_lat", thirdPoint_lat);
                intentResult.putStringArrayListExtra("thirdPoint_lon", thirdPoint_lon);

                intentResult.putStringArrayListExtra("FirstName", FirstName);
                intentResult.putStringArrayListExtra("SecondName", SecondName);
                intentResult.putStringArrayListExtra("ThirdName", ThirdName);

                intentResult.putStringArrayListExtra("firstSpotId", firstSpotId);
                intentResult.putStringArrayListExtra("secondSpotId", secondSpotId);
                intentResult.putStringArrayListExtra("thirdSpotId", thirdSpotId);

                System.out.println("로그: timeList(보내기 직전): " + timeList);
                intentResult.putStringArrayListExtra("timeList", timeList);

                startActivity(intentResult);
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
}