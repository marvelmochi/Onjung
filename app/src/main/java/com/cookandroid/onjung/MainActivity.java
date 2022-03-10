package com.cookandroid.onjung;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements TMapGpsManager.onLocationChangedCallback {

    // T Map 앱 키
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";

    // 하단 내비게이션 바 선언
    BottomNavigationView bottomNavigationView;
    ScheduleFragment fragment1;
    HomeFragment fragment2;
    MenuFragment fragment3;
    FragmentManager fragmentManager;

    // 뒤로가기 두 번 눌러 종료
    private long backKeyPressedTime = 0;
    private Toast toast;

    // 멤버 아이디
    String memberId;

    // 평가 이력 개수
    int ratingNumber;

    // 현위치 좌표 전달
    SharedPreferences preferences;
    double home_lat, home_lon;
    String home_lat_s, home_lon_s;

    // T Map GPS
    TMapGpsManager tMapGPS = null;

    // 인텐트에 담아 전달할 데이터 배열 선언
    ArrayList<String> recentPosition = new ArrayList<>(); // 현위치 좌표 {"위도", "경도"}

    // JSON parsing에 필요한 변수 선언
    JSONArray firstTypes, secondTypes, thirdTypes;
    // 경유지 티맵 포인트 ArrayList
    /*
    ArrayList<TMapPoint> firstPoint = new ArrayList<>();
    ArrayList<TMapPoint> secondPoint = new ArrayList<>();
    ArrayList<TMapPoint> thirdPoint = new ArrayList<>();
     */
    ArrayList<String> firstLat = new ArrayList<>();
    ArrayList<String> secondLat = new ArrayList<>();
    ArrayList<String> thirdLat = new ArrayList<>();
    ArrayList<String> firstLon = new ArrayList<>();
    ArrayList<String> secondLon = new ArrayList<>();
    ArrayList<String> thirdLon = new ArrayList<>();

    // spotId
    ArrayList<String> firstSpotId = new ArrayList<>();
    ArrayList<String> secondSpotId = new ArrayList<>();
    ArrayList<String> thirdSpotId = new ArrayList<>();
    // name
    ArrayList<String> firstName = new ArrayList<>();
    ArrayList<String> secondName = new ArrayList<>();
    ArrayList<String> thirdName = new ArrayList<>();

    // <소요시간 관련 데이터 선언>
    ArrayList<String> timeList = new ArrayList<>();
    ArrayList<String> passList = new ArrayList<>();
    String passList1 = "";
    String passList2 = "";
    String passList3 = "";
    String time1;
    String time2;
    String time3;

    int flag_exception = 0;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 현위치 저장을 위해 불러옴
        preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

        // Request For GPS permission
        // 위치 권한 요청
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

        //만보기
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {

            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

/*
        // SharedPreferences Test
        SharedPreferences preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");
*/
        preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");


        System.out.println("로그: 멤버아이디 불러오기(Main): " + memberId);


        /*
        // 평가 횟수 받아오기
        HttpConnectorRatingNumber ratingNumberThread = new HttpConnectorRatingNumber();
        ratingNumberThread.start();

         */

        // 하단 내비게이션 바 생성
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 프래그먼트 생성 (캘린더/홈/메뉴)
        fragment1 = new ScheduleFragment();
        fragment2 = new HomeFragment();
        fragment3 = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment2, "home").commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                fragmentManager = getSupportFragmentManager();
                switch (menuItem.getItemId()) {
                    case R.id.tab1: {
                        if (fragmentManager.findFragmentByTag("schedule") != null) {
                            //프래그먼트가 존재한다면 보여준다.
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("schedule")).commit();
                        } else {
                            //getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment1).commitAllowingStateLoss();
                            fragmentManager.beginTransaction().add(R.id.main_layout, new ScheduleFragment(), "schedule").commit();
                        }
                        if (fragmentManager.findFragmentByTag("menu") != null) {
                            //다른프래그먼트가 보이면 가려준다.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("menu")).commit();
                        }
                        if (fragmentManager.findFragmentByTag("home") != null) {
                            //다른프래그먼트가 보이면 가려준다.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("home")).commit();
                        }
                        return true;
                    }

                    case R.id.tab2: {
                        if (fragmentManager.findFragmentByTag("home") != null) {
                            //if the fragment exists, show it.
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("home")).commit();
                        } else {
                            //getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment2).commitAllowingStateLoss();
                            fragmentManager.beginTransaction().add(R.id.main_layout, new HomeFragment(), "home").commit();
                        }
                        if (fragmentManager.findFragmentByTag("schedule") != null) {
                            //if the other fragment is visible, hide it.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("schedule")).commit();
                        }
                        if (fragmentManager.findFragmentByTag("menu") != null) {
                            //if the other fragment is visible, hide it.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("menu")).commit();
                        }
                        return true;
                    }

                    case R.id.tab3: {
                        if (fragmentManager.findFragmentByTag("menu") != null) {
                            //if the fragment exists, show it.
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("menu")).commit();
                        } else {
                            //getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment3).commitAllowingStateLoss();
                            fragmentManager.beginTransaction().add(R.id.main_layout, new MenuFragment(), "menu").commit();
                        }
                        if (fragmentManager.findFragmentByTag("schedule") != null) {
                            //if the other fragment is visible, hide it.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("schedule")).commit();
                        }
                        if (fragmentManager.findFragmentByTag("home") != null) {
                            //if the other fragment is visible, hide it.
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("home")).commit();
                        }
                        return true;
                    }

                    default:
                        return false;
                }
            }
        });

        /*
        moodButton = (Button) findViewById(R.id.moodButton);
        moodButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratingNumber == 0) {
                    // 평가 이력이 없을 경우
                    Intent intentPrefer = new Intent(MainActivity.this, PreferenceTestActivity.class);
                    intentPrefer.putStringArrayListExtra("recentPosition", recentPosition);
                    startActivity(intentPrefer);
                } else {
                    // 평가 이력이 있을 경우
                    intentMood.putStringArrayListExtra("recentPosition", recentPosition);
                    startActivity(intentMood);
                }
            }
        });

         */



        /*
        preferences = getSharedPreferences("UserInfo", MODE_MULTI_PROCESS);
        TextView UserId = (TextView) findViewById(R.id.UserId);
        String id = preferences.getString("id", "");
        UserId.setText(id);
         */


    }

    @Override
    public void onLocationChange(Location location) {
        // 내 위치 좌표 home에 저장
        TMapPoint home = tMapGPS.getLocation();
        home_lat = home.getLatitude();
        home_lon = home.getLongitude();
        home_lat_s = Double.toString(home_lat);
        home_lon_s = Double.toString(home_lon);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("home_lat", home_lat_s);
        editor.putString("home_lon", home_lon_s);

        editor.commit();


        System.out.println("로그: 현위치 좌표 " + home_lat_s + ", " + home_lon_s);
        // 평가 횟수 받아오기
        HttpConnectorRatingNumber ratingNumberThread = new HttpConnectorRatingNumber();
        ratingNumberThread.start();

    }

    // 조건에 맞는 산책로 추천 클릭
    public void selectbtnClicked(View view) {
        tMapGPS.CloseGps();
        Intent intent = new Intent(this, SelectActivity.class);
        //intent.putStringArrayListExtra("recentPosition", recentPosition);
        startActivity(intent);
        finish();
    }

    public void moodClicked(View view) {
        tMapGPS.CloseGps();
        System.out.println("로그: moodClicked 진입");

        if (ratingNumber == 0) {
            // 평가 이력이 없을 경우
            System.out.println("로그: 평가 이력 없음");
            Intent intent = new Intent(this, PreferenceTestActivity.class);
            //intent.putStringArrayListExtra("recentPosition", recentPosition);
            startActivity(intent);
            //finish();
        } else {
            // 평가 이력이 있을 경우
            System.out.println("로그: 평가 이력 있음");

            Intent intent = new Intent(this, MoodRecommendActivity.class);
            intent.putStringArrayListExtra("recentPosition", recentPosition);
            intent.putStringArrayListExtra("firstLat", firstLat);
            intent.putStringArrayListExtra("secondLat", secondLat);
            intent.putStringArrayListExtra("thirdLat", thirdLat);
            intent.putStringArrayListExtra("firstLon", firstLon);
            intent.putStringArrayListExtra("secondLon", secondLon);
            intent.putStringArrayListExtra("thirdLon", thirdLon);
            intent.putStringArrayListExtra("firstName", firstName);
            intent.putStringArrayListExtra("secondName", secondName);
            intent.putStringArrayListExtra("thirdName", thirdName);
            intent.putStringArrayListExtra("firstSpotId", firstSpotId);
            intent.putStringArrayListExtra("secondSpotId", secondSpotId);
            intent.putStringArrayListExtra("thirdSpotId", thirdSpotId);

            // 소요시간은 통신 오류가 자주 발생하므로 오류가 발생해도 다음 액티비티로 넘어갈 수 있도록 처리
            if (flag_exception == 0) {
                timeList.add(time1);
                timeList.add(time2);
                timeList.add(time3);
            } else if (flag_exception > 0 || timeList.size()<3) {
                timeList.add("?");
                timeList.add("?");
                timeList.add("?");
            }

            System.out.println("로그: 저장된 timeList: " + timeList);
            intent.putStringArrayListExtra("timeList", timeList);
            startActivity(intent);
            //finish();
        }
    }

    // 취향을 분석한 산책로 추천 클릭
    /*
    public void preferencebtnClicked(View view) {

        if (ratingNumber == 0) {
            // 평가 이력이 없을 경우
            Intent intent = new Intent(this, PreferenceTestActivity.class);
            intent.putStringArrayListExtra("recentPosition", recentPosition);
            startActivity(intent);
        } else {
            // 평가 이력이 있을 경우
            intentMood.putStringArrayListExtra("recentPosition", recentPosition);
            startActivity(intentMood);
        }

    }*/

    class HttpConnectorRatingNumber extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {
                System.out.println("로그: memberId: " + memberId);
                url = new URL("http://smwu.onjung.tk/rating/" + memberId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                //System.out.println("로그: 응답 메시지: " + returnMsg);
                jsonParserRatingNumber(returnMsg);


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 평가 개수 확인 예외 발생");
            }

        }
    }

    public void jsonParserRatingNumber(String resultJson) {
        try {
            // 응답으로 받은 데이터를 JSONObject에 넣음
            JSONObject dataObject = new JSONObject(resultJson);
            ratingNumber = dataObject.getInt("data");
            System.out.println("로그: data: " + ratingNumber);

            // 평가 이력 있을 경우
            // MainActivity에서 미리 산책로 데이터 받아오기

            if (ratingNumber > 0) {
                HttpConnectorMood moodThread = new HttpConnectorMood();
                moodThread.start();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 하단 내비게이션 -> 캘린더 프래그먼트 클릭
    public void calendarClicked(View view) {
        tMapGPS.CloseGps();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("menu")).commit();
        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("schedule")).commit();
    }

    // [메뉴] - 산책 다이어리 클릭
    public void diaryClicked(View view) {
        Intent intent = new Intent(this, DiaryActivity.class);
        startActivity(intent);
    }

    // 다이어리 작성 클릭
    public void diaryCreateClicked(View view) {
        Intent intent = new Intent(this, DiaryCreateActivity.class);
        startActivity(intent);
    }

    // [메뉴] - 친구 목록 클릭
    public void friendbtnClicked(View view) {
        Intent intent = new Intent(this, FriendActivity.class);
        startActivity(intent);
    }

    // [메뉴] - 회원정보 수정 클릭
    public void editInfoClicked(View view) {
        Intent intent = new Intent(this, EditUserInfoActivity.class);
        startActivity(intent);
    }

    // [메뉴] - 로그아웃 클릭

    public void logoutClicked(View view) {

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    // 홈에서 뒤로가기 막기
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            //finish();
            toast.cancel();
            // 앱 완전 종료
            ActivityCompat.finishAffinity(this);
            System.exit(0);
        }
        /* 앱 종료 다이얼로그 -> 실패..

        findViewById(android.R.id.home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitDialog.show();

                Button noBtn = quitDialog.findViewById(R.id.quitNo);
                noBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        quitDialog.dismiss();
                    }
                });
                quitDialog.findViewById(R.id.quitYes).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        finish();
                    }
                });
            }
        });
        */

    }


    // 평가 이력 있는 경우 가짜 정보 URL 통신
    class HttpConnectorMood extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {
                url = new URL("http://smwu.onjung.tk/mood?active=1&quiet=1&walkable=1&sight=1&pet=1&sightseeing=1&exercise=1&latitude="
                        + home_lat_s + "&longitude=" + home_lon_s + "&type=a&userId=" + memberId);
                System.out.println("로그: 통신 URL: " + url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 응답 메시지: " + returnMsg);
                jsonParserMood(returnMsg);


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 통신 예외 발생");

            }
            HttpConnectorTime TimeThread = new HttpConnectorTime();
            TimeThread.start();
        }
    }

    public void jsonParserMood(String resultJson) {
        try {
            // 응답으로 받은 데이터를 JSONObject에 넣음
            JSONObject jsonObject = new JSONObject(resultJson);
            // JSONObject에서 "data" 부분을 추출
            String data = jsonObject.getString("data"); // 이건 JSONArray X
            System.out.println("로그: data: " + data);
            JSONObject totalObject = new JSONObject(data);

            // 첫 번째
            if (!totalObject.isNull("1")) {
                firstTypes = totalObject.getJSONArray("1");

                if (!firstTypes.isNull(0)) {
                    JSONObject firstOfFirst = firstTypes.getJSONObject(0);
                    String lat = firstOfFirst.getString("latitude");
                    String lon = firstOfFirst.getString("longitude");
                    passList1 += lon + "," + lat + "_";
                    firstLat.add(lat);
                    firstLon.add(lon);

                    // spotId
                    int spotId1_i = firstOfFirst.getInt("spotId");
                    String spotId1 = Integer.toString(spotId1_i);
                    firstSpotId.add(spotId1);

                    // name
                    String name = firstOfFirst.getString("name");
                    firstName.add(name);

                }
                if (!firstTypes.isNull(1)) {
                    JSONObject secondOfFirst = firstTypes.getJSONObject(1);
                    String lat = secondOfFirst.getString("latitude");
                    String lon = secondOfFirst.getString("longitude");
                    passList2 += lon + "," + lat + "_";
                    secondLat.add(lat);
                    secondLon.add(lon);

                    // spotId
                    int spotId2_i = secondOfFirst.getInt("spotId");
                    String spotId2 = Integer.toString(spotId2_i);
                    secondSpotId.add(spotId2);

                    // name
                    String name = secondOfFirst.getString("name");
                    secondName.add(name);
                }
                if (!firstTypes.isNull(2)) {
                    JSONObject thirdOfFirst = firstTypes.getJSONObject(2);
                    String lat = thirdOfFirst.getString("latitude");
                    String lon = thirdOfFirst.getString("longitude");
                    passList3 += lon + "," + lat + "_";
                    thirdLat.add(lat);
                    thirdLon.add(lon);

                    // spotId
                    int spotId3_i = thirdOfFirst.getInt("spotId");
                    String spotId3 = Integer.toString(spotId3_i);
                    thirdSpotId.add(spotId3);

                    // name
                    String name = thirdOfFirst.getString("name");
                    thirdName.add(name);
                }

            }

            // 두 번째
            if (!totalObject.isNull("2")) {
                secondTypes = totalObject.getJSONArray("2");

                if (!secondTypes.isNull(0)) {
                    JSONObject firstOfSecond = secondTypes.getJSONObject(0);
                    String lat = firstOfSecond.getString("latitude");
                    String lon = firstOfSecond.getString("longitude");
                    passList1 += lon + "," + lat + "_";
                    firstLat.add(lat);
                    firstLon.add(lon);

                    // spotId
                    int spotId1_i = firstOfSecond.getInt("spotId");
                    String spotId1 = Integer.toString(spotId1_i);
                    firstSpotId.add(spotId1);

                    // name
                    String name = firstOfSecond.getString("name");
                    firstName.add(name);

                }
                if (!secondTypes.isNull(1)) {
                    JSONObject secondOfSecond = secondTypes.getJSONObject(1);
                    String lat = secondOfSecond.getString("latitude");
                    String lon = secondOfSecond.getString("longitude");
                    passList2 += lon + "," + lat + "_";
                    secondLat.add(lat);
                    secondLon.add(lon);

                    // spotId
                    int spotId2_i = secondOfSecond.getInt("spotId");
                    String spotId2 = Integer.toString(spotId2_i);
                    secondSpotId.add(spotId2);

                    // name
                    String name = secondOfSecond.getString("name");
                    secondName.add(name);
                }
                if (!secondTypes.isNull(2)) {
                    JSONObject thirdOfSecond = secondTypes.getJSONObject(2);
                    String lat = thirdOfSecond.getString("latitude");
                    String lon = thirdOfSecond.getString("longitude");
                    passList3 += lon + "," + lat + "_";
                    thirdLat.add(lat);
                    thirdLon.add(lon);

                    // spotId
                    int spotId3_i = thirdOfSecond.getInt("spotId");
                    String spotId3 = Integer.toString(spotId3_i);
                    thirdSpotId.add(spotId3);

                    // name
                    String name = thirdOfSecond.getString("name");
                    thirdName.add(name);
                }
            }

            // 세 번째
            if (!totalObject.isNull("3")) {
                thirdTypes = totalObject.getJSONArray("3");

                if (!thirdTypes.isNull(0)) {
                    JSONObject firstOfThird = thirdTypes.getJSONObject(0);
                    String lat = firstOfThird.getString("latitude");
                    String lon = firstOfThird.getString("longitude");
                    passList1 += lon + "," + lat + "_";
                    firstLat.add(lat);
                    firstLon.add(lon);

                    // spotId
                    int spotId1_i = firstOfThird.getInt("spotId");
                    String spotId1 = Integer.toString(spotId1_i);
                    firstSpotId.add(spotId1);

                    // name
                    String name = firstOfThird.getString("name");
                    firstName.add(name);
                }
                if (!thirdTypes.isNull(1)) {
                    JSONObject secondOfThird = thirdTypes.getJSONObject(1);
                    String lat = secondOfThird.getString("latitude");
                    String lon = secondOfThird.getString("longitude");
                    passList2 += lon + "," + lat + "_";
                    secondLat.add(lat);
                    secondLon.add(lon);

                    // spotId
                    int spotId2_i = secondOfThird.getInt("spotId");
                    String spotId2 = Integer.toString(spotId2_i);
                    secondSpotId.add(spotId2);

                    // name
                    String name = secondOfThird.getString("name");
                    secondName.add(name);
                }
                if (!thirdTypes.isNull(2)) {
                    JSONObject thirdOfThird = thirdTypes.getJSONObject(2);
                    String lat = thirdOfThird.getString("latitude");
                    String lon = thirdOfThird.getString("longitude");
                    passList3 += lon + "," + lat + "_";
                    thirdLat.add(lat);
                    thirdLon.add(lon);

                    // spotId
                    int spotId3_i = thirdOfThird.getInt("spotId");
                    String spotId3 = Integer.toString(spotId3_i);
                    thirdSpotId.add(spotId3);

                    // name
                    String name = thirdOfThird.getString("name");
                    thirdName.add(name);
                }

            }


            /*
            Message message = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("spotName", spotName);
            bundle.putStringArrayList("spotId", spotId);
            bundle.putStringArrayList("spotLat", spotLat);
            bundle.putStringArrayList("spotLon", spotLon);
            message.setData(bundle);
            handler.sendMessage(message);
            */

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
                            + "&startX=" + home_lon_s + "&startY=" + home_lat_s +
                            "&endX=" + home_lon_s + "&endY=" + home_lat_s +
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


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 소요시간 통신 예외 발생");
                flag_exception += 1;
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



