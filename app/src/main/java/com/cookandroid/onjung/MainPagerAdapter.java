package com.cookandroid.onjung;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class MainPagerAdapter extends FragmentStatePagerAdapter {

    ArrayList<Fragment> items = new ArrayList<Fragment>();

    public MainPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public void addItem(Fragment item){
        items.add(item);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

}
