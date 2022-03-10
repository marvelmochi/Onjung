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
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class ScheduleFragment extends Fragment {
    ViewGroup viewGroup;

    // 캘린더뷰 선언
    //CalendarView calendarView;
    MaterialCalendarView calendarView;

    // 선택된 날짜 담을 변수 선언
    String selectedDate, mselectDate;
    CalendarDay cal;
    // 멤버아이디 불러올 변수 선언
    String memberId;

    ValueHandler handler = new ValueHandler();
    PlanHandler handler2 = new PlanHandler();

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
    ArrayList<Integer> wspotIdList;

    String jname_s;
    String jlat_s;
    String jlon_s;
    String jspot_s;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_schedule, container, false);

        // 캘린더 뷰 접근 (viewGroup주의)
        calendarView = viewGroup.findViewById(R.id.calendar);

        // 캘린더뷰 클릭 이벤트
        OneDayDecorator today = new OneDayDecorator();
        calendarView.addDecorators(today);
        calendarView.setDynamicHeightEnabled(true);

        // Using SharedPreferences on Fragment
        preferences = this.getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        memberId = preferences.getString("memberId", "");
        System.out.println("로그: 멤버아이디 불러오기(Schedule): " + memberId);

        // 달력켜자마자 일정
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.MONTH, Calendar.YEAR);

        String year_s, month_s;

        year_s = String.valueOf(calendar.get(Calendar.YEAR));
        System.out.println("로그: 년도 " + calendar.get(Calendar.YEAR));
        if (Integer.toString(Calendar.MONTH).length() == 1) {
            month_s = '0' + Integer.toString(Calendar.MONTH+1);
        } else {
            month_s = Integer.toString(Calendar.MONTH+1);
        }
        System.out.println("로그: 달 " + month_s);

        mselectDate = year_s + month_s;

        HttpConnectorPlans plansThread = new HttpConnectorPlans();
        plansThread.start();

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {

            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                String year_s, month_s, day_s;

                year_s = String.valueOf(date.getYear());
                if (Integer.toString(date.getMonth()).length() == 1) {
                    month_s = '0' + Integer.toString(date.getMonth());
                } else {
                    month_s = Integer.toString(date.getMonth());
                }

                if (Integer.toString(date.getDay()).length() == 1) {
                    day_s = '0' + Integer.toString(date.getDay());
                } else {
                    day_s = Integer.toString(date.getDay());
                }
                selectedDate = year_s + month_s + day_s;
                //scheduleText.setText(selectedDate);

                System.out.println("로그: 선택된 날짜: " + selectedDate);

                HttpConnectorSchedule scheduleThread = new HttpConnectorSchedule();
                scheduleThread.start();

            }

        });

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {

            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                //캘린더뷰 일정 표시
                Calendar calendar= Calendar.getInstance();
                calendar.add(Calendar.MONTH+1, date.getYear()-1);

                String year_s, month_s;

                year_s = String.valueOf(date.getYear());
                if (Integer.toString(date.getMonth()).length() == 1) {
                    month_s = '0' + Integer.toString(date.getMonth());
                } else {
                    month_s = Integer.toString(date.getMonth());
                }

                mselectDate = year_s + month_s;

                HttpConnectorPlans plansThread = new HttpConnectorPlans();
                plansThread.start();

            }
        });


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
        wspotIdList = new ArrayList<>();

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

    class HttpConnectorPlans extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {

                url = new URL("http://smwu.onjung.tk/walk/" + memberId + "/" + mselectDate);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 선택 날짜: " + mselectDate);
                System.out.println("로그: 응답 메시지: " + returnMsg);

                jsonParserPlans(returnMsg);

            } catch (Exception es) {
                es.printStackTrace();
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
                int wspot = jsonObject1.getInt("spotId");
                wspotIdList.add(wspot);

                //북마크
                System.out.println("로그: 이름/위/경도: " + wname + ", " + wlat + ", " + wlon);

            }

            JSONArray jname = new JSONArray();
            JSONArray jlat = new JSONArray();
            JSONArray jlon = new JSONArray();
            JSONArray jspotId = new JSONArray();

            for (int i = 0; i < wnameList.size(); i++) {
                jname.put(wnameList.get(i));
                jlat.put(wlatList.get(i));
                jlon.put(wlonList.get(i));
                jspotId.put(wspotIdList.get(i));
            }
            jname_s = jname.toString();
            jlat_s = jlat.toString();
            jlon_s = jlon.toString();
            jspot_s = jspotId.toString();


            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("home_lat", home_lat);
            editor.putString("home_lon", home_lon);
            //editor.putInt("completeFlag", completeFlag);
            editor.putString("jname", jname_s);
            editor.putString("jlat", jlat_s);
            editor.putString("jlon", jlon_s);
            editor.putString("walkId", walkId);
            editor.putString("jspot", jspot_s);
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
            linearLayout.removeAllViews();
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
                button.setOutlineProvider(null);

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

    public void jsonParserPlans(String resultJson) {
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
                ArrayList<String> date_List = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {

                    // 각 일정을 String으로 변환
                    String plan = jsonArray.get(i).toString();
                    // 일정을 jsonObject에 넣음
                    JSONObject jsonObject = new JSONObject(plan);
                    // 산책 제목을 꺼내 저장
                    String plan_date = jsonObject.getString("walkDate");
                    System.out.println("로그: 산책 날짜: " + plan_date);

                    //calendarView.addDecorator(new EventDecorator(Color.parseColor("#329F0B"), Collections.singleton(cal)));
                    if (!date_List.contains(plan_date)) {
                        date_List.add(plan_date);
                    }
                }

                Message msg = handler2.obtainMessage();
                Bundle pbundle = new Bundle();
                pbundle.putStringArrayList("date", date_List);
                msg.setData(pbundle);
                handler2.sendMessage(msg);

            }

        } catch (Exception el) {
            el.printStackTrace();
            System.out.println("로그: 파싱 예외 발생");
        }
    }

    class PlanHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            Bundle bundle = msg.getData();
            //String returnmsg = bundle.getString("returnMsg");
            ArrayList<String> dateArrayList = new ArrayList<>();
            dateArrayList = bundle.getStringArrayList("date");

            linearLayout.removeAllViewsInLayout();
            //System.out.println("로그: remove on Handler");
            for (int i = 0; i < dateArrayList.size(); i++) {

                String year_s, month_s, day_s;
                Integer year, month, day;

                year_s = dateArrayList.get(i).substring(0,4);
                month_s = dateArrayList.get(i).substring(5,7);
                day_s = dateArrayList.get(i).substring(8,10);
                System.out.println("로그: 날짜: " + year_s + month_s + day_s);

                year = Integer.parseInt(year_s);
                month = Integer.parseInt(month_s);
                day = Integer.parseInt(day_s);

                cal = CalendarDay.from(year, month, day);
                calendarView.addDecorator(new EventDecorator(Color.parseColor("#329F0B"), Collections.singleton(cal)));

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

    //오늘 날짜 표시하기기
    public class OneDayDecorator implements DayViewDecorator {

        private CalendarDay date;


        public OneDayDecorator() {
            date = CalendarDay.today();
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new StyleSpan(Typeface.BOLD));
            view.addSpan(new RelativeSizeSpan(1.4f));
            //view.addSpan(new ForegroundColorSpan(Color.parseColor("#329F0B")));
        }
    }

    //일정 표시
    public class EventDecorator implements DayViewDecorator {

        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(5, color));
        }
    }

}
