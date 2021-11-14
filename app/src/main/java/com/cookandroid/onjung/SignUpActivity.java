package com.cookandroid.onjung;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        name = (EditText)findViewById(R.id.name);
        birth = (EditText)findViewById(R.id.birth);
        id = (EditText)findViewById(R.id.id);
        pw = (EditText)findViewById(R.id.pw);
        genderRG = (RadioGroup)findViewById(R.id.genderRG);

    }


    public void signupClicked(View view) {
        genderid = genderRG.getCheckedRadioButtonId();
        genderbtn =(RadioButton)findViewById(genderid);

        HttpConnector thread = new HttpConnector();
        thread.start();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    class HttpConnector extends Thread {
        public JSONObject data;
        URL url;
        HttpURLConnection conn;
        public HttpConnector(){
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


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}