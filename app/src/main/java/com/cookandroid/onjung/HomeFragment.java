package com.cookandroid.onjung;

import static android.content.Context.MODE_PRIVATE;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment implements SensorEventListener {
    ViewGroup viewGroup;
    TextView userNameText;
    String userName, memberId, nowDate;
    Long now;
    Date date;

    LinearLayout linearLayout;

    SharedPreferences preferences;

    //만보기
    SensorManager sensorManager;
    Sensor stepCountSensor;
    TextView stepCountView, totalDistance ,Calories, Progress;
    private SharedPreferences pedometer;

    //오늘의 일정
    private ListView listview = null;
    private ListViewAdapter adapter = null;

    // 현재 걸음 수
    int currentSteps;
    String calorie, distance;

    //프로그레스
    ProgressBar progressbar;

    //@Nullable
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        stepCountView = viewGroup.findViewById(R.id.stepCountView);
        totalDistance = viewGroup.findViewById(R.id.totalDistance);
        Calories = viewGroup.findViewById(R.id.Calories);
        Progress = viewGroup.findViewById(R.id.progress_text);

        if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        //만보기 프로그레스
        progressbar = (ProgressBar) viewGroup.findViewById(R.id.progress_bar) ;


        // 걸음 센서 연결
        // * 옵션
        // - TYPE_STEP_DETECTOR:  리턴 값이 무조건 1, 앱이 종료되면 다시 0부터 시작
        // - TYPE_STEP_COUNTER : 앱 종료와 관계없이 계속 기존의 값을 가지고 있다가 1씩 증가한 값을 리턴
        //
        sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        //만보기 초기화 알람
        int DATA_FETCHER_RC = 123;
        AlarmManager mAlarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.add(Calendar.DATE, 1);

        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), DATA_FETCHER_RC,intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mAlarmManager.setInexactRepeating(AlarmManager.RTC,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);

        //오늘 일정 불러오기
        listview = (ListView) viewGroup.findViewById(R.id.listview);

        now = System.currentTimeMillis();
        date = new Date(now);
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyyMMdd");
        String nowDate = sdformat.format(date);
        //System.out.println("출력: "+ nowDate);

        HomeFragment.HttpConnectorSchedule scheduleThread = new HomeFragment.HttpConnectorSchedule();
        scheduleThread.start();

        preferences = getActivity().getSharedPreferences("UserInfo", MODE_PRIVATE);
        userName = preferences.getString("name", "");
        memberId = preferences.getString("memberId", "");
        System.out.println("로그: 멤버이름 불러오기(Home): " + userName);

        userNameText = viewGroup.findViewById(R.id.userNameText);
        userNameText.setText(userName+" 님");

        return viewGroup;

    }

    //만보기 초기화 알람
    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences preferences = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
            String memberId = preferences.getString("memberId", "");

            SharedPreferences pedometer = context.getSharedPreferences(memberId, MODE_PRIVATE);
            SharedPreferences.Editor peditor = pedometer.edit();

            peditor.putInt("currentSteps", 0);

            peditor.apply();
        }
    }

    class HttpConnectorSchedule extends Thread {
        URL url;
        HttpURLConnection conn;

        @Override
        public void run() {
            try {
                url = new URL("http://smwu.onjung.tk/walk/" + memberId + "/" + nowDate);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                //System.out.println("로그: 응답 메시지: " + returnMsg);

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

                adapter = new ListViewAdapter();

                // JSONObject에서 "data" 부분을 추출
                String data = dataObject.getString("data");
                //System.out.println("로그: data: " + data);
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
                    adapter.addItem(new TitleItem(schedule_title));
                    title_List.add(schedule_title);
                }
                listview.setAdapter(adapter);

            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("로그: 파싱 예외 발생");
        }
    }

    public class TitleItem {
        /* 아이템의 정보를 담기 위한 클래스 */

        String title;

        public TitleItem(String title) {
            this.title = title;
        }

        public String getName() {
            return title;
        }
        public void setName(String name) {
            this.title = title;
        }
    }

    public class ListViewAdapter extends BaseAdapter {
        ArrayList<TitleItem> items = new ArrayList<TitleItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(TitleItem item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final Context context = viewGroup.getContext();
            final TitleItem title = items.get(position);

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_item, viewGroup, false);

            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            TextView listitem_title = (TextView) convertView.findViewById(R.id.listitem_title);

            listitem_title.setText(items.get(position).getName());

            return convertView;  //뷰 객체 반환
        }
    }

    public void onStart() {
        super.onStart();
        if(stepCountSensor !=null) {
            // 센서 속도 설정
            // * 옵션
            // - SENSOR_DELAY_NORMAL: 20,000 초 딜레이
            // - SENSOR_DELAY_UI: 6,000 초 딜레이
            // - SENSOR_DELAY_GAME: 20,000 초 딜레이
            // - SENSOR_DELAY_FASTEST: 딜레이 없음
            //
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 걸음 센서 이벤트 발생시

        pedometer = getActivity().getSharedPreferences(memberId, MODE_PRIVATE);
        currentSteps = pedometer.getInt("currentSteps", 0);

        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){

            currentSteps++;
            distance = String.format("%1$,.2f", 0.0006 * currentSteps);
            calorie = String.format("%1$,.2f", 0.0333 * currentSteps);

            progressbar.setProgress(currentSteps) ;

            stepCountView.setText(String.valueOf(currentSteps)+"보");
            totalDistance.setText(distance+"km");
            Calories.setText(calorie+"kcal");
            Progress.setText(currentSteps+"/6000");

            //System.out.println("발자국: " + currentSteps);

            pedometer = getActivity().getSharedPreferences(memberId, MODE_PRIVATE);
            SharedPreferences.Editor peditor = pedometer.edit();

            peditor.putInt("currentSteps", currentSteps);
            peditor.putString("distance", distance);
            peditor.putString("calorie", calorie);

            peditor.apply()
            ;


        }

        /*
        if(event.values[0]==1.0f){
            // 센서 이벤트가 발생할때 마다 걸음수 증가
            currentSteps++;
            stepCountView.setText(String.valueOf(userName)+"보");
        }*/

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}

