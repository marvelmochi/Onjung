package com.cookandroid.onjung;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {


    // 뒤로가기 2회로 종료 하기 위한 변수
    private long backKeyPressedTime = 0; // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private Toast toast; // 첫 번째 뒤로가기 버튼을 누를때 표시

    // 전역변수 선언
    EditText id, pw;

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;

    // 파싱을 위한 변수 선언
    String code;
    String detail;
    String data;

    // 파싱 후 회원정보를 저장할 변수 선언
    String memberId_s, name_s, gender_s, id_s, pw_s;
    int birth_s;


    // 로그인 성공 후 회원 정보를 저장하기 위한 프리퍼런스
    private SharedPreferences preferences;

    // 산책 경로 정보를 저장할 preferences
    private SharedPreferences preferencesCourse;


    // Intent
    //Intent intentlogin = new Intent(LoginActivity.this, MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        id = (EditText) findViewById(R.id.id);
        pw = (EditText) findViewById(R.id.password);

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());

        //
        preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

    }

    // 로그인 버튼 클릭 이벤트
    public void loginClicked(View view) {
        HttpConnectorLogin loginThread = new HttpConnectorLogin();
        loginThread.start();


    }

    // 회원 가입 클릭 이벤트
    public void signupClicked(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    // 로그인 기능
    class HttpConnectorLogin extends Thread {
        public JSONObject data;
        URL url;
        HttpURLConnection conn;

        public HttpConnectorLogin() {
            try {
                data = new JSONObject();

                data.put("id", id.getText().toString());
                data.put("pw", pw.getText().toString());
                System.out.println("로그: data: " + data);
                url = new URL("http://smwu.onjung.tk/member/login");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                conn.setDoOutput(true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                bw.write(data.toString());
                bw.flush();
                bw.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();

                System.out.println("로그: 응답 메시지: " + returnMsg);

                jsonParser(returnMsg);

                Intent intentlogin = new Intent(LoginActivity.this, MainActivity.class);
                //startActivity(intentlogin);


                if ("200".equals(code)) {
                    System.out.println("로그: code값: " + code);
                    startActivity(intentlogin);
                } else if ("401".equals(code)) {
                    System.out.println("로그: code값: " + code);
                    System.out.println("로그: " + detail);
                    ToastMessage(detail);
                } else {
                    System.out.println("로그: 액티비티 전환 실패");
                }



            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }

    // 받아온 Detail 값 파싱을 위해..
    public void jsonParser(String resultJson) {

        try {

            //String rmNull; // json 문자열에서 null 값 제거한 문자열
            //rmNull = resultJson.replace("null", "");
            JSONObject jsonObject = new JSONObject(resultJson);

            code = jsonObject.getString("code");
            detail = jsonObject.getString("detail");
            data = jsonObject.getString("data");


            // data를 파싱할 jsonObject
            JSONObject jsonData = new JSONObject(data);
            memberId_s = jsonData.getString("memberId");
            name_s = jsonData.getString("name");
            birth_s = jsonData.getInt("birth");
            gender_s = jsonData.getString("gender");
            id_s = jsonData.getString("id");
            pw_s = jsonData.getString("pw");

            //System.out.println("로그: 로그인 후 받아온 회원정보 파싱 결과: "
            // +memberId_s+name_s+birth_s+gender_s+id_s+pw_s);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("memberId", memberId_s);
            editor.putString("name", name_s);
            editor.putInt("birth", birth_s);
            editor.putString("gender", gender_s);
            editor.putString("id", id_s);
            editor.putString("pw", pw_s);

            editor.commit();


            //System.out.println("로그: preferences에 저장된 값: "
            //+ preferences.getInt("birth",0) );
            ToastMessage(detail);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("로그: 파싱 예외 발생");

        }


    }

    // 로그인 성공 후 받은 회원정보를 앱에 저장


    // 스레드 위에서 토스트 메시지를 띄우기 위한 메소드
    public void ToastMessage(String message) {

        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // 뒤로가기 두 번 눌러 종료
    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
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
    }
}

