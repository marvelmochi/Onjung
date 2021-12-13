package com.cookandroid.onjung;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    // walkId 저장할 리스트
    //ArrayList<String> walkIdList;

    // T Map 앱 키 등록
    String API_Key = "l7xxa57022c9d2f9453db8f198c5ca511fdb";
    TMapView tMapView = null;

    // 산책 완료 버튼
    Button completeBtn;
    String walkId;

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;



    // UserInfo(title) 전달할 SharedPrefereces
    SharedPreferences preferences;

    ArrayList<String> wnameList;
    ArrayList<String> wlatList;
    ArrayList<String> wlonList;

    String jname_s;
    String jlat_s;
    String jlon_s;
    
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
        preferences = this.getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");
        System.out.println("로그: 멤버아이디 불러오기(Schedule): " + memberId);


        // 리니어레이아웃 접근
        linearLayout = viewGroup.findViewById(R.id.scheduleView);

        // 경로 다시 보여줄 다이얼로그
        courseDialog = new Dialog(getContext());
        courseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        courseDialog.setContentView(R.layout.dialog_show_course);

        // walkId 저장할 리스트
        ArrayList<String> walkIdList = new ArrayList<>();

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

        /*
        // 산책 완료 버튼
        completeBtn = courseDialog.findViewById(R.id.completeBtn);
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 산책 완료 통신
                HttpConnectorComplete completeThread = new HttpConnectorComplete();
                completeThread.start();

                // 만족도 조사 다이얼로그 띄우기
                satisfactionDialog = new Dialog(getContext());
                satisfactionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                satisfactionDialog.setContentView(R.layout.dialog_satisfaction);
                satisfactionDialog.show();


            }
        });*/

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());

        // CourseInfo 전달을 위한 ArrayList 생성;
        wnameList = new ArrayList<>();
        wlatList = new ArrayList<>();
        wlonList = new ArrayList<>();

        return viewGroup;

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

    class HttpConectorCourse extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {
                url = new URL("http://smwu.onjung.tk/walk/" + walkId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 응답 메시지: " + returnMsg);
                System.out.println("로그: 코스 아이디: " + walkId);
                jsonParserCourse(returnMsg);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("로그: 개별 산책일정 불러오기 예외 발생");
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
                //linearLayout.removeAllViews();

                System.out.println("로그: remove on 400");
            } else {

                // JSONObject에서 "data" 부분을 추출
                String data = dataObject.getString("data");
                System.out.println("로그: data: " + data);
                // "data"의 값을 jsonArray에 넣음(요소 값은 각각의 일정을 의미)
                JSONArray jsonArray = new JSONArray(data);

                // 산책 제목 저장할 ArrayList
                ArrayList<String> title_List = new ArrayList<>();

                // walkId 저장할 리스트
                ArrayList<String> walkIdList = new ArrayList<>();

                // completeFlag 저장할 리스트
                ArrayList<Integer> flagList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {

                    // 각 일정을 String으로 변환
                    String schedule = jsonArray.get(i).toString();
                    // 일정을 jsonObject에 넣음
                    JSONObject jsonObject = new JSONObject(schedule);
                    // 산책 제목을 꺼내 저장
                    String schedule_title = jsonObject.getString("title");
                    //System.out.println("로그: 산책 제목: " + schedule_title);
                    title_List.add(schedule_title);

                    walkId = jsonObject.getString("walkId");
                    walkIdList.add(walkId);
                    System.out.println("로그: walkId리스트 " + i + "번째: " + walkIdList.get(i));

                    int complete_flag = jsonObject.getInt("completeFlag");
                    flagList.add(complete_flag);
                }

                // 산책 제목을 핸들러로 전달하기 위해
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("title", title_List);
                bundle.putStringArrayList("walkIdList", walkIdList);
                bundle.putIntegerArrayList("completeFlag", flagList);
                message.setData(bundle);
                handler.sendMessage(message);
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("로그: 파싱 예외 발생");
        }
    }

    public void jsonParserCourse(String resultJson) {
        try {
            // 응답으로 받은 데이터를 JSONObject에 넣음
            JSONObject jsonObject = new JSONObject(resultJson);
            // JSONObject에서 "data" 부분을 추출
            String data = jsonObject.getString("data");
            JSONObject dataObject = new JSONObject(data);
            String home_lat = dataObject.getString("latitude");
            String home_lon = dataObject.getString("longitude");

            ArrayList<Integer> flagList = new ArrayList<>();
            int completeFlag = dataObject.getInt("completeFlag");
            flagList.add(completeFlag);

            JSONArray wayArray = dataObject.getJSONArray("wayPoint");
            System.out.println("로그: 홈 위경도: " + home_lat + ", " + home_lon);

            for (int i = 0; i < wayArray.length(); i++) {
                System.out.println("로그: wayArray.get(" + i + ")" + wayArray.get(i));
                String way = wayArray.get(i).toString();
                JSONObject jsonObject1 = new JSONObject(way);
                String wname = jsonObject1.getString("name");
                wnameList.add(wname);
                String wlat = jsonObject1.getString("latitude");
                wlatList.add(wlat);
                String wlon = jsonObject1.getString("longitude");
                wlonList.add(wlon);
                System.out.println("로그: 이름/위/경도: " + wname + ", " + wlat + ", " + wlon);

            }

            JSONArray jname = new JSONArray();
            JSONArray jlat = new JSONArray();
            JSONArray jlon = new JSONArray();

            for (int i = 0; i < wnameList.size(); i++) {
                jname.put(wnameList.get(i));
                jlat.put(wlatList.get(i));
                jlon.put(wlonList.get(i));
            }
            jname_s = jname.toString();
            jlat_s = jlat.toString();
            jlon_s = jlon.toString();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("home_lat", home_lat);
            editor.putString("home_lon", home_lon);
            //editor.putInt("completeFlag", completeFlag);
            editor.putString("jname", jname_s);
            editor.putString("jlat", jlat_s);
            editor.putString("jlon", jlon_s);
            editor.putString("walkId", walkId);
            editor.apply();

            Intent intent = new Intent(getActivity(), ShowCourseActivity.class);
            startActivity(intent);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("로그: 파싱 예외 발생2");
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
            ArrayList<String> walkIdArrayList = new ArrayList<>();
            walkIdArrayList = bundle.getStringArrayList("walkIdList");
            ArrayList<Integer> flagArrayList = new ArrayList<>();
            flagArrayList = bundle.getIntegerArrayList("completeFlag");

            //System.out.println("로그: 핸들러에서 전달된 제목 리스트: "+titleArrayList.get(0));

            linearLayout.removeAllViewsInLayout();
            //System.out.println("로그: remove on Handler");
            for (int i = 0; i < titleArrayList.size(); i++) {
                TextView textView = new TextView(getContext());
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(22);
                textView.setTextColor(Color.rgb(0, 0, 0));
                textView.setTypeface(null, Typeface.BOLD);

                // 산책 완료 상태에 따라 텍스트뷰 색상 변경
                if (flagArrayList.get(i) == 0) {
                    textView.setBackgroundColor(Color.rgb(169, 214, 151));
                } else {
                    textView.setBackgroundColor(Color.rgb(169, 169, 169));
                }

                // 임시로 버튼 처리!! UI는 다시 손봐야 할 듯
                Button button = new Button(getContext());
                button.setText("walkId: " + walkIdArrayList.get(i));
                button.setTextColor(0x707070); // 투명 텍스트

                //DisplayMetrics dm = getResources().getDisplayMetrics();
                //int size = Math.round(20 * dm.density);
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
                linearLayout.addView(button);
                //System.out.println("로그: add TextView");

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //courseDialog.show();
                        //System.out.println("로그: 클릭 이벤트 발생");
                        //cdTitle.setText(textView.getText().toString());

                        // Using SharedPreferences on Fragment
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("title", textView.getText().toString());
                        editor.apply();

                        HttpConectorCourse courseThread = new HttpConectorCourse();
                        courseThread.start();

                    }
                });
            }


        }
    }


    // 스레드 위에서 토스트 메시지를 띄우기 위한 메소드
    public void ToastMessage(String message) {

        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

