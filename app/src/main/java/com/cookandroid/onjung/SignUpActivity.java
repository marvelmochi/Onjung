package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    EditText name, birth, id, pw;
    RadioGroup genderRG;
    int genderid;
    RadioButton genderbtn;
    int responseCode;

    String idCheck;

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 회원가입
        name = (EditText)findViewById(R.id.name);
        birth = (EditText)findViewById(R.id.birth);
        id = (EditText)findViewById(R.id.id);
        pw = (EditText)findViewById(R.id.pw);
        genderRG = (RadioGroup)findViewById(R.id.genderRG);

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());


    }

    // 회원가입 버튼 눌렀을 때 이벤트
    public void signupClicked(View view) {
        genderid = genderRG.getCheckedRadioButtonId();
        genderbtn =(RadioButton)findViewById(genderid);

        HttpConnectorSignup signupThread = new HttpConnectorSignup();
        signupThread.start();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // 아이디 중복체크 버튼 눌렀을 때 이벤트
    public void idCheckClicked(View view) {
        HttpConnectorIdCheck idcheckThread = new HttpConnectorIdCheck();
        idcheckThread.start();
    }

    // 회원가입 기능
    class HttpConnectorSignup extends Thread {
        public JSONObject data;
        URL url;
        HttpURLConnection conn;
        public HttpConnectorSignup(){
            try {
                data = new JSONObject();
                data.put("name", name.getText().toString());
                data.put("birth", Integer.parseInt(birth.getText().toString()));
                data.put("gender", genderbtn.getText().toString());
                data.put("id", id.getText().toString());
                data.put("pw", pw.getText().toString());
                System.out.println("로그: data: " + data);
                url = new URL("http://3.18.232.232:8080/member");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

            }catch (Exception e){
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
                responseCode = conn.getResponseCode();
                System.out.println("로그: 응답 메시지: " +returnMsg);

                int resCode = conn.getResponseCode();
                System.out.println("로그: ResponseCode: " +resCode);
                if (resCode == 200) {
                    ToastMessage("회원가입이 완료되었습니다.");
                } else ToastMessage("회원가입에 실패했습니다.");


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // 아이디 중복체크 기능
    class HttpConnectorIdCheck extends Thread {

        URL url;
        HttpURLConnection conn;
        public HttpConnectorIdCheck(){
            try {
                idCheck = id.getText().toString();
                url = new URL("http://3.18.232.232:8080/member/check?id="+idCheck);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                //System.out.println("로그: 입력된 아이디 값: "+ idCheck);
                //System.out.println("로그: "+idCheck+"로 중복 체크");

            }catch (Exception e){
                //System.out.println("로그: 아이디 중복체크 예외 발생");
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String returnMsg = in.readLine();
                System.out.println("로그: 중복체크 응답 메시지: " +returnMsg);

                int resCode = conn.getResponseCode();
                System.out.println("로그: ResponseCode: " +resCode);
                ToastMessage(returnMsg);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    // 스레드 위에서 토스트 메시지를 띄우기 위한 메소드
    public void ToastMessage(final String message) {

        toastHandler.post(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}