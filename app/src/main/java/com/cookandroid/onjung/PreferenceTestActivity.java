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

    // 인텐트로 액티비티 간 데이터 전달
    String data = "success";

    // 체크박스 선언
    CheckBox mountain, park, lake, forest, river;
    CheckBox crowded, quiet, pet, scenery, statue, exercise, walk;

    // 체크된 값들 담을 ArrayList
    ArrayList<CheckBox> spot_c = new ArrayList<>();
    ArrayList<String> spot = new ArrayList<>();
    //ArrayList<CheckBox> mood_c = new ArrayList<>();
    //ArrayList<String> mood = new ArrayList<>();

    //분위기 체크 값
    String active, quiet_c, walkable, sight, pet_c, sightseeing, exercise_c;

    // T Map 앱 키 등록
    String API_Key = "l7xxa08e5b27d8fb417f9d09d0bc162c7df9";

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
    ArrayList recentPosition = new ArrayList(); // 현위치 좌표 {"위도", "경도"}
    ArrayList<String> spotName = new ArrayList<>(); // 경유지 이름 ArrayList
    ArrayList<String> spotLat = new ArrayList<>();  // 경유지 위도 ArrayList
    ArrayList<String> spotLon = new ArrayList<>(); // 경유지 경도 ArrayList
    ArrayList spotId = new ArrayList(); //  경유지 아이디 ArrayList

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test);

        // 툴바: 뒤로가기 버튼
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 체크박스 접근
        mountain = (CheckBox) findViewById(R.id.mountain);
        park = (CheckBox) findViewById(R.id.park);
        lake = (CheckBox) findViewById(R.id.lake);
        forest = (CheckBox) findViewById(R.id.forest);
        river= (CheckBox) findViewById(R.id.river);

        crowded = (CheckBox) findViewById(R.id.crowded);
        quiet = (CheckBox) findViewById(R.id.quiet);
        pet = (CheckBox) findViewById(R.id.pet);
        scenery = (CheckBox) findViewById(R.id.scenery);
        statue= (CheckBox) findViewById(R.id.statue);
        exercise = (CheckBox) findViewById(R.id.exercise);
        walk = (CheckBox) findViewById(R.id.walk);

        spot_c.add(mountain);
        spot_c.add(park);
        spot_c.add(lake);
        spot_c.add(forest);
        spot_c.add(river);

        /*
        mood_c.add(crowded);
        mood_c.add(quiet);
        mood_c.add(pet);
        mood_c.add(scenery);
        mood_c.add(statue);
        mood_c.add(exercise);
        mood_c.add(walk);
         */

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

    }

    public void HomebtnClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLocationChange(Location location) {
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

    public void preferenceResultbtnClicked(View view) {
        Intent intent = new Intent(this, PreferenceTestResultActivity.class);
        startActivity(intent);

        for (int i = 0; i < spot_c.size(); i++) {
            if (spot_c.get(i).isChecked()) {
                String spots = spot_c.get(i).getText().toString();
                spot.add(spots);
                //System.out.println("로그: 산책지 " + spot.get(i));}
            }
        }

        if(crowded.isChecked()) {    //체크 박스가 체크 된 경우
            active = "1";
        }
        else {   //체크 박스가 해제 된 경우
            active = "0";
        }

        if(quiet.isChecked()) {    //체크 박스가 체크 된 경우
            quiet_c = "1";
        }
        else {   //체크 박스가 해제 된 경우
            quiet_c = "0";
        }

        if(pet.isChecked()) {    //체크 박스가 체크 된 경우
            pet_c = "1";
        }
        else {   //체크 박스가 해제 된 경우
            pet_c = "0";
        }

        if(scenery.isChecked()) {    //체크 박스가 체크 된 경우
            sight = "1";
        }
        else {   //체크 박스가 해제 된 경우
            sight = "0";
        }

        if(statue.isChecked()) {    //체크 박스가 체크 된 경우
            sightseeing = "1";
        }
        else {   //체크 박스가 해제 된 경우
            sightseeing = "0";
        }

        if(exercise.isChecked()) {    //체크 박스가 체크 된 경우
            exercise_c = "1";
        }
        else {   //체크 박스가 해제 된 경우
            exercise_c = "0";
        }

        if(walk.isChecked()) {    //체크 박스가 체크 된 경우
            walkable = "1";
        }
        else {   //체크 박스가 해제 된 경우
            walkable = "0";
        }


        PreferenceTestActivity.HttpConnectorPrefer preferThread = new PreferenceTestActivity.HttpConnectorPrefer();
        preferThread.start();

    }

    //http://smwu.onjung.tk/mood?active=1&quiet=1&walkable=1&sight=1&pet=0&sightseeing=1
    // &exercise=0&latitude=37.534784&longitude=126.851609&type=park&userId=2
    class HttpConnectorPrefer extends Thread {
        Message message;
        Bundle bundle = new Bundle();

        @Override
        public void run() {
            for (int z = 0; z < spot.size(); z++) {
                try {
                    String type;
                    type = spot.get(z);
                    URL url = new URL("http://smwu.onjung.tk/mood?active="+ active + "&quiet=" + quiet_c + "&walkable=" + walkable
                            + "&sight=" + sight + "&pet=" + pet_c + "&sightseeing=" + sightseeing + "&exercise=" + exercise_c
                            + "&latitude=" + lat + "&longitude=" + lon + "&type=" + type + "&userId=1");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String returnMsg = in.readLine();
                    System.out.println("로그: 응답 메시지: " + returnMsg);

                    // 파싱 메소드 호출
                    jsonParser(returnMsg);

                    Intent intentResult = new Intent(PreferenceTestActivity.this, PreferenceTestResultActivity.class);
                    intentResult.putExtra("mydata", data);
                    intentResult.putExtra("recentPosition", recentPosition);
                    intentResult.putExtra("spotName", spotName);
                    intentResult.putExtra("spotLat", spotLat);
                    intentResult.putExtra("spotLon", spotLon);
                    intentResult.putExtra("spotId", spotId);

                    startActivity(intentResult);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("로그: 산책지 불러오기 예외발생");
                }
            }
        }

        public void jsonParser(String resultJson) {
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
                spotName.add(name);
                spotLat.add(lat);
                spotLon.add(lon);
                spotId.add(spotid);

            /*
            for (int i = 0; i<spotName.size() ; i++){
                System.out.println("로그: spotName: " + spotName.get(i));
                System.out.println("로그: spotLat: " + spotLat.get(i));
                System.out.println("로그: spotLon: " + spotLon.get(i));
            }*/
                //spotName.add(name);
                //spotLat.add(lat);
                //spotLon.add(lon);
                //System.out.println("로그: spotName: "+spotName.get(0));


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
    }
}