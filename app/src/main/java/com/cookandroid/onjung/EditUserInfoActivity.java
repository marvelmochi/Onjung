package com.cookandroid.onjung;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditUserInfoActivity extends AppCompatActivity {

    EditText name, birth, pw, check_pw;
    String pws, check_pws;
    RadioGroup genderRG;
    int genderid;
    RadioButton genderbtn;
    String id, memberId;
    int responseCode;

    // 토스트 온 스레드를 위한 핸들러
    Handler toastHandler;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 토스트 온 스레드
        toastHandler = new Handler(Looper.getMainLooper());

        SharedPreferences preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        id = preferences.getString("id", "");
        memberId = preferences.getString("memberId", "");

        // 회원정보 수정
        name = (EditText)findViewById(R.id.name);
        birth = (EditText)findViewById(R.id.birth);
        pw = (EditText)findViewById(R.id.pw);
        genderRG = (RadioGroup)findViewById(R.id.genderRG);
        check_pw = (EditText)findViewById(R.id.check_pw);

    }

    public void editClicked(View view) {
        pws = pw.getText().toString();
        check_pws = check_pw.getText().toString();
        System.out.println("비밀번호 비교: "+ pws +", "+check_pws);

        if (!pws.equals(check_pws)) {

            ToastMessage("비밀번호를 다시 확인해주세요.");
            pw.setText(null);
            check_pw.setText(null);

        } else {
            genderid = genderRG.getCheckedRadioButtonId();
            genderbtn =(RadioButton)findViewById(genderid);

            ToastMessage("비밀번호가 일치합니다");
            EditUserInfoActivity.HttpConnectorSignup signupThread = new EditUserInfoActivity.HttpConnectorSignup();
            signupThread.start();
        }
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
                data.put("id", id);
                data.put("pw", pw.getText().toString());
                System.out.println("로그: data: " + data);
                url = new URL("http://smwu.onjung.tk/member");
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
                    ToastMessage("정보 수정이 완료되었습니다.");
                } else ToastMessage("정보 수정에 실패했습니다.");


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                // 액티비티 이동
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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