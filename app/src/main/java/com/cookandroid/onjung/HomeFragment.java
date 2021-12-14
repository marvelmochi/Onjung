package com.cookandroid.onjung;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    ViewGroup viewGroup;
    TextView userNameText;
    String userName;

    SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);


        preferences = getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        userName = preferences.getString("name", "");
        System.out.println("로그: 멤버이름 불러오기(Home): " + userName);

        userNameText = viewGroup.findViewById(R.id.userNameText);
        userNameText.setText(userName+" 님");
        return viewGroup;
    }
}

