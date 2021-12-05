package com.cookandroid.onjung;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScheduleFragment extends Fragment {
    ViewGroup viewGroup;

    // 캘린더뷰 선언
    CalendarView calendarView;

    // 날짜 표시할 임시 텍스트뷰 선언
    TextView scheduleText;

    // 선택된 날짜 담을 변수 선언
    String selectedDate;

    // 멤버아이디 불러올 변수 선언
    String memberId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_schedule, container, false);

        // 캘린더 뷰 접근 (viewGroup주의)
        calendarView = viewGroup.findViewById(R.id.calendar);
        scheduleText = viewGroup.findViewById(R.id.scheduleText);

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
                scheduleText.setText(selectedDate);
                System.out.println("로그: 선택된 날짜: " + selectedDate);

                HttpConnectorSchedule scheduleThread = new HttpConnectorSchedule();
                scheduleThread.start();
            }
        });


        // Using SharedPreferences on Fragment
        SharedPreferences preferences = this.getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");
        System.out.println("로그: 멤버아이디 불러오기(Schedule): " + memberId);

        return viewGroup;

    }

    class HttpConnectorSchedule extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run(){
            try{
                url = new URL("http://smwu.onjung.tk/walk/" + memberId + "/" + selectedDate);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 응답 메시지: "+returnMsg);

            }catch (Exception e){
                e.printStackTrace();
                System.out.println("로그: 산책일정 불러오기 예외 발생");
            }
        }
    }

    public  void jsonParser(String resultJson) {
        try{

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("로그: 파싱 예외 발생");
        }
    }
}

