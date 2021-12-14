package com.cookandroid.onjung;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

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

    // 현위치 좌표 전달
    double home_lat, home_lon;
    String home_lat_s, home_lon_s;

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    // T Map GPS
    TMapGpsManager tMapGPS = null;

    // 인텐트에 담아 전달할 데이터 배열 선언
    ArrayList recentPosition = new ArrayList(); // 현위치 좌표 {"위도", "경도"}
    ArrayList<String> spotId = new ArrayList<>();
    ArrayList<String> spotName = new ArrayList<>(); // 경유지 이름 ArrayList
    ArrayList<String> spotLat = new ArrayList<>();  // 경유지 위도 ArrayList
    ArrayList<String> spotLon = new ArrayList<>(); // 경유지 경도 ArrayList

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test);

        // Using SharedPreferences / memberId 받아오기
        preferences = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        userId = preferences.getString("memberId", "");

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

    @Override
    public void onLocationChange(Location location) {
        // 내 위치 좌표 home에 저장
        TMapPoint home = tMapGPS.getLocation();
        home_lat = home.getLatitude();
        home_lon = home.getLongitude();
        home_lat_s = Double.toString(home_lat);
        home_lon_s = Double.toString(home_lon);
        // 배열에 현위치 담기
        recentPosition.add(home_lat_s);
        recentPosition.add(home_lon_s);

        System.out.println("로그: 현위치 좌표 " + home_lat_s + ", " + home_lon_s);
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
                            c_sightseeing + "&exercise=" + c_exercise + "&latitude=" + home_lat_s + "&longitude=" +
                            home_lon_s + "&type=" + type + "&userId=" + userId);
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

            // 인텐트 전달
            Intent intentResult = new Intent(PreferenceTestActivity.this, PreferenceTestResultActivity.class);
            intentResult.putExtra("recentPosition", recentPosition);
            intentResult.putExtra("spotName", spotName);
            intentResult.putExtra("spotLat", spotLat);
            intentResult.putExtra("spotLon", spotLon);
            intentResult.putExtra("spotId", spotId);

            startActivity(intentResult);
            for (int i=0; i<spotName.size();i++){
                System.out.println("로그: 경유지 명: "+spotName.get(i));
            }
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

            for (int i = 0; i < dataArray.length(); i++) {
                String datas = dataArray.get(i).toString();
                JSONObject jsonObject1 = new JSONObject(datas);
                int spotId_i = jsonObject1.getInt("spotId");
                String spotId_s = Integer.toString(spotId_i);
                spotId.add(spotId_s);
                String name = jsonObject1.getString("name");
                spotName.add(name);
                String wlat = jsonObject1.getString("latitude");
                spotLat.add(wlat);
                String wlon = jsonObject1.getString("longitude");
                spotLon.add(wlon);
            }





        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("로그: 파싱 예외 발생");
        }
    }
}