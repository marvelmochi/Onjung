package com.cookandroid.onjung;

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
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

    public void jsonParser(String resultJson) {
        try {

            // 응답으로 받은 데이터를 JSONObject에 넣음
            JSONObject dataObject = new JSONObject(resultJson);

            // 400 예외처리 위해
            int code = dataObject.getInt("code");
            if (code==400){
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

                for (int i = 0; i < jsonArray.length(); i++) {
                    // 각 일정을 String으로 변환
                    String schedule = jsonArray.get(i).toString();
                    // 일정을 jsonObject에 넣음
                    JSONObject jsonObject = new JSONObject(schedule);
                    // 산책 제목을 꺼내 저장
                    String schedule_title = jsonObject.getString("title");
                    System.out.println("로그: 산책 제목: " + schedule_title);
                    title_List.add(schedule_title);

                }
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
            System.out.println("로그: remove on Handler");
            for (int i = 0; i < titleArrayList.size(); i++) {
                TextView textView = new TextView(getContext());
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(22);
                textView.setTextColor(Color.rgb(0,0,0));
                textView.setTypeface(null, Typeface.BOLD);
                textView.setBackgroundColor(Color.rgb(169,214,151));

                DisplayMetrics dm = getResources().getDisplayMetrics();
                int size = Math.round(20 * dm.density);
                textView.setPadding(20,20,20,20);
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
                System.out.println("로그: add TextView");
            }


        }
    }
}

