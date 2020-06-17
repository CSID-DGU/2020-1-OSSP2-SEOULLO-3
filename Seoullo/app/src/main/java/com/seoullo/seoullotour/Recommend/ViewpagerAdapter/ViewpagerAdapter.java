package com.seoullo.seoullotour.Recommend.ViewpagerAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ViewpagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<Fragment> fragments = new ArrayList<>();

    public ViewpagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void addItem(Fragment frg) {
        fragments.add(frg);
    }

    public void addItem(ArrayList<Fragment> frg) {
        for(int i=0; i<frg.size(); ++i)
            fragments.add(frg.get(i));
    }
}
