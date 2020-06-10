package com.seoullo.seoullotour.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.Utils.BottomNavigationViewHelper;
import com.seoullo.seoullotour.Utils.SectionsPagerAdapter;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";
    public static final int ACTIVITY_NUM = 2;
    private Context mContext = MapActivity.this;
    //widget
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;
    private LinearLayout mLinearLayout;
    //var
    private Point mPoint;

    //TODO: link to fragment
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");

        Intent intent = getIntent();
        this.mPoint = (Point) intent.getSerializableExtra("point");

        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayout1);
        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mRelativeLayout.setVisibility(View.GONE);
        mLinearLayout = (LinearLayout) findViewById(R.id.bottom_division);
        mLinearLayout.setVisibility(View.GONE);

        setupBottomNavigationView();
        setupViewPager();
    }
    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        if(mPoint != null)
            adapter.addFragment(new MapFragment(mPoint));
        else
            adapter.addFragment(new MapFragment());
        mViewPager.setAdapter(adapter);
    }
    public void hideLayout(){
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }


    public void showLayout(){
        Log.d(TAG, "hideLayout: showing layout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mFrameLayout.getVisibility() == View.VISIBLE){
            showLayout();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onRestart() {
        super.onRestart();
    }
    public void onDestroy() {
        super.onDestroy();
    }
}
