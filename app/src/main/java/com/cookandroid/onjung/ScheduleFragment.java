package com.cookandroid.onjung;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScheduleFragment extends Fragment {
    ViewGroup viewGroup;

    // 캘린더뷰 선언
    CalendarView calendarView;

    // 선택된 날짜 담을 변수 선언
    String selectedDate;
    // 멤버아이디 불러올 변수 선언
    String memberId;

    ValueHandler handler = new ValueHandler();
    String returnMsg;

    LinearLayout linearLayout;

    // jsonParsing 후 위 경도를 저장할 ArrayList
    // 각 일정 별로 home의 위/경도 저장 : home_point = [{위,경}, {위,경}, ...]
    ArrayList<ArrayList<String>> home_point = new ArrayList<ArrayList<String>>();
    // 각 일정 별로 경유지들의 위도를 저장할 ArrayList
    ArrayList<ArrayList<String>> wayPoint_lat = new ArrayList<ArrayList<String>>();
    // 각 일정 별로 경유지들의 경도를 저장할 ArrayList
    ArrayList<ArrayList<String>> wayPoint_lon = new ArrayList<ArrayList<String>>();

    // 경로 보여줄 다이얼로그
    // 경로 다시 보여줄 다이얼로그
    Dialog courseDialog;
    Button okBtn;
    TextView cdTitle;

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;

    // 받아온 위, 경도 정보를 저장할 HashMap
    Map<String, ArrayList<ArrayList<String>>> locationMap = new HashMap<String, ArrayList<ArrayList<String>>>();

    // 산책 완료 버튼
    Button completeBtn;

    String walkId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_schedule, container, false);

        // 캘린더 뷰 접근 (viewGroup주의)
        calendarView = viewGroup.findViewById(R.id.calendar);

        // 캘린더뷰 클릭 이벤트
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String year_s, month_s, day_s;
                year_s = Integer.toString(year);
                if (Integer.toString(month).length() == 1) {
                    month_s = '0' + Integer.toString(month + 1);
                } else {
                    month_s = Integer.toString(month + 1);
                }

                if (Integer.toString(dayOfMonth).length() == 1) {
                    day_s = '0' + Integer.toString(dayOfMonth);
                } else {
                    day_s = Integer.toString(dayOfMonth);
                }
                selectedDate = year_s + month_s + day_s;
                //scheduleText.setText(selectedDate);
                System.out.println("로그: 선택된 날짜: " + selectedDate);

                HttpConnectorSchedule scheduleThread = new HttpConnectorSchedule();
                scheduleThread.start();


            }
        });


        // Using SharedPreferences on Fragment
        SharedPreferences preferences = this.getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");
        System.out.println("로그: 멤버아이디 불러오기(Schedule): " + memberId);

        // 리니어레이아웃 접근
        linearLayout = viewGroup.findViewById(R.id.scheduleView);

        // 경로 다시 보여줄 다이얼로그
        courseDialog = new Dialog(getContext());
        courseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        courseDialog.setContentView(R.layout.dialog_show_course);

        // 경로 다이얼로그에 있는 요소 접근
        okBtn = courseDialog.findViewById(R.id.cd_okBtn);
        okBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                courseDialog.dismiss();
            }
        });
        cdTitle = courseDialog.findViewById(R.id.cd_title);

        // 티맵 관련
        tMapView = new TMapView(getContext());// 티맵 뷰 생성
        tMapView.setSKTMapApiKey(API_Key); // 앱 키 등록
        // 맵뷰 기본 설정
        tMapView.setZoomLevel(14);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        // 리니어레이아웃에 맵뷰 추가
        LinearLayout Tmap = (LinearLayout) courseDialog.findViewById(R.id.map);
        Tmap.addView(tMapView);

        // 산책 완료 버튼
        completeBtn = courseDialog.findViewById(R.id.completeBtn);
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpConnectorComplete completeThread = new HttpConnectorComplete();
                completeThread.start();
            }
        });

        return viewGroup;

    }

    class HttpConnectorComplete extends Thread{
        URL url;
        HttpURLConnection conn;
        @Override
        public void run(){
            try{
                System.out.println("로그: walkId: "+ walkId);
                url = new URL("http://smwu.onjung.tk/walk/toggle/"+walkId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 응답 메시지: " + returnMsg);


            }catch (Exception e){e.printStackTrace(); System.out.println("로그: 산책 저장 예외 발생");}
        }
    }

    class HttpConnectorSchedule extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {
                url = new URL("http://smwu.onjung.tk/walk/" + memberId + "/" + selectedDate);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 응답 메시지: " + returnMsg);
                /*
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("returnMsg", returnMsg);
                message.setData(bundle);
                handler.sendMessage(message);

                 */
                jsonParser(returnMsg);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 산책일정 불러오기 예외 발생");
            }
        }
    }

    public void jsonParser(String resultJson) {
        try {

            // 응답으로 받은 데이터를 JSONObject에 넣음
            JSONObject dataObject = new JSONObject(resultJson);

            // 400 예외처리 위해
            int code = dataObject.getInt("code");
            if (code == 400) {
                System.out.println("로그: 코드 400");
                linearLayout.removeAllViewsInLayout();
                System.out.println("로그: remove on 400");
            } else {

                // JSONObject에서 "data" 부분을 추출
                String data = dataObject.getString("data");
                System.out.println("로그: data: " + data);
                // "data"의 값을 jsonArray에 넣음(요소 값은 각각의 일정을 의미)
                JSONArray jsonArray = new JSONArray(data);

                // 산책 제목 저장할 ArrayList
                ArrayList<String> title_List = new ArrayList<>();

                // home 위도, 경도 저장할 ArrayList
                ArrayList<String> home_List = new ArrayList<>();

                // 전체 정보를 담아서 해쉬맵에 넣기 위한 ArrayList
                ArrayList<ArrayList<String>> wholeList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {

                    // 각 일정을 String으로 변환
                    String schedule = jsonArray.get(i).toString();
                    // 일정을 jsonObject에 넣음
                    JSONObject jsonObject = new JSONObject(schedule);
                    // 산책 제목을 꺼내 저장
                    String schedule_title = jsonObject.getString("title");
                    //System.out.println("로그: 산책 제목: " + schedule_title);
                    title_List.add(schedule_title);

                    String home_lat = jsonObject.getString("latitude");
                    String home_lon = jsonObject.getString("longitude");

                    walkId = jsonObject.getString("walkId");

                    home_List.add(home_lat);
                    home_List.add(home_lon);
                    System.out.println("로그: 받아온 홈 위도, 경도: " + home_lat + ", " + home_lon);
                    wholeList.add(home_List); // 전체 어레이리스트에 현위치 위,경도 담음

                    JSONArray wayPointArray = jsonObject.getJSONArray("wayPoint");

                    // 경유지 위도, 경도, 이름 리스트
                    ArrayList<String> latList = new ArrayList<>();
                    ArrayList<String> lonList = new ArrayList<>();
                    ArrayList<String> nameList = new ArrayList<>();
                    for (int j = 0; j < wayPointArray.length(); j++) {
                        System.out.println("로그: 경유지 저장 확인: " + j + "번째: " + wayPointArray.get(j));
                        String wayPointString = wayPointArray.get(j).toString();
                        JSONObject jsonObject1 = new JSONObject(wayPointString);
                        String wlat = jsonObject1.getString("latitude");
                        String wlon = jsonObject1.getString("longitude");
                        String wname = jsonObject1.getString("name");
                        System.out.println("로그: wname: "+wname);

                        latList.add(wlat);
                        lonList.add(wlon);
                        nameList.add(wname);
                    }
                    wholeList.add(latList); // 전체 어레이리스트에 경유지 위도 리스트 담음
                    wholeList.add(lonList); // 전체 어레이리스트에 경유지 경도 리스트 담음
                    wholeList.add(nameList); // 전체 어레이리스트에 경유지 산책지이름 리스트 담음

                    locationMap.put(schedule_title, wholeList); // 해쉬맵에 전체 리스트 담음
                }

                //해쉬맵 테스트

                // 해쉬맵 보류 -> API 추가할 수 있을까..?
                System.out.println("로그 해쉬맵: "+ locationMap.get("서서울"));


                // 산책 제목을 핸들러로 전달하기 위해
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("title", title_List);
                message.setData(bundle);
                handler.sendMessage(message);
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("로그: 파싱 예외 발생");
        }
    }


    class ValueHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            Bundle bundle = msg.getData();
            //String returnmsg = bundle.getString("returnMsg");
            ArrayList<String> titleArrayList = new ArrayList<>();
            titleArrayList = bundle.getStringArrayList("title");

            //System.out.println("로그: 핸들러에서 전달된 제목 리스트: "+titleArrayList.get(0));

            linearLayout.removeAllViewsInLayout();
            //System.out.println("로그: remove on Handler");
            for (int i = 0; i < titleArrayList.size(); i++) {
                TextView textView = new TextView(getContext());
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(22);
                textView.setTextColor(Color.rgb(0, 0, 0));
                textView.setTypeface(null, Typeface.BOLD);
                textView.setBackgroundColor(Color.rgb(169, 214, 151));

                DisplayMetrics dm = getResources().getDisplayMetrics();
                int size = Math.round(20 * dm.density);
                textView.setPadding(20, 20, 20, 20);
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                param.topMargin = 30;
                param.leftMargin = 20;
                param.rightMargin = 20;
                param.gravity = Gravity.CENTER;

                textView.setLayoutParams(param);


                textView.setText(titleArrayList.get(i));
                linearLayout.addView(textView);
                //System.out.println("로그: add TextView");

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        courseDialog.show();
                        //System.out.println("로그: 클릭 이벤트 발생");
                        cdTitle.setText(textView.getText().toString());


                    }
                });
            }


        }
    }
}

