package com.cookandroid.onjung;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment implements SensorEventListener {
    ViewGroup viewGroup;
    TextView userNameText;
    String userName;

    SharedPreferences preferences;

    //만보기
    SensorManager sensorManager;
    Sensor stepCountSensor;
    TextView stepCountView, totalDistance ,Calories;

    // 현재 걸음 수
    int currentSteps = 0;
    int calorie = 0;
    double distance = 0;

    //@Nullable
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        stepCountView = viewGroup.findViewById(R.id.stepCountView);
        totalDistance = viewGroup.findViewById(R.id.totalDistance);
        Calories = viewGroup.findViewById(R.id.Calories);

        if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // 걸음 센서 연결
        // * 옵션
        // - TYPE_STEP_DETECTOR:  리턴 값이 무조건 1, 앱이 종료되면 다시 0부터 시작
        // - TYPE_STEP_COUNTER : 앱 종료와 관계없이 계속 기존의 값을 가지고 있다가 1씩 증가한 값을 리턴
        //
        sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // 디바이스에 걸음 센서의 존재 여부 체크
        //if(stepCountSensor == null){
            //Toast.makeText(this,"No Step Detect Sensor",Toast.LENGTH_SHORT).show();
        //}

        preferences = getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        userName = preferences.getString("name", "");
        System.out.println("로그: 멤버이름 불러오기(Home): " + userName);

        userNameText = viewGroup.findViewById(R.id.userNameText);
        userNameText.setText(userName+" 님");
        return viewGroup;

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
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            currentSteps++;

            distance = 0.0007 * currentSteps;
            calorie = 45 * currentSteps;

            stepCountView.setText(String.valueOf(currentSteps)+"보");
            totalDistance.setText(String.format("%1$,.2f", 0.0006 * currentSteps)+"km");
            Calories.setText(String.format("%1$,.2f", 0.0333 * currentSteps)+"kcal");

            /*
            if(event.values[0]==1.0f){
                // 센서 이벤트가 발생할때 마다 걸음수 증가
                currentSteps++;
                stepCountView.setText(String.valueOf(userName)+"보");
            }*/

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

