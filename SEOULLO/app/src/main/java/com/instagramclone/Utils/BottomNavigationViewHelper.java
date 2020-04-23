package com.instagramclone.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.instagramclone.MainActivity;
import com.instagramclone.Tabs.BookmarkFragment;
import com.instagramclone.Tabs.GridFragment;
import com.instagramclone.Tabs.MapFragment;
import com.instagramclone.Tabs.UserFragment;
import com.instagramclone.databinding.ActivityMainBinding;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.instagramclone.R;

import static com.instagramclone.Utils.StatusCode.FRAGMENT_ARG;

public class BottomNavigationViewHelper {


    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx){
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }


    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx view){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                MainActivity mainActivity = new MainActivity();
                switch (item.getItemId()){

                    case R.id.action_home:

//               Fragment gridFragment = new GridFragment();
//
//                Bundle bundle_1 = new Bundle();
//                bundle_1.putInt(FRAGMENT_ARG, 1);
//
//                gridFragment.setArguments(bundle_1);
//
//                getFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.main_content, gridFragment)
//                        .commit();
                        androidx.fragment.app.Fragment gridFragment = new GridFragment();
                        Bundle bundle_1 = new Bundle();
                        bundle_1.putInt(FRAGMENT_ARG, 1);

                        gridFragment.setArguments(bundle_1);

                        //백스택 지우기
                        mainActivity.getSupportFragmentManager()
                                .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                        mainActivity.getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.main_content, gridFragment)
                                .commit();

                        return true;
                    case R.id.action_bookmark:



                        androidx.fragment.app.Fragment bookmarkFragment = new BookmarkFragment();

                        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        Bundle bundle_2 = new Bundle();
                        bundle_2.putString("loginUid", userid);
                        bundle_2.putInt(FRAGMENT_ARG, 2);

                        bookmarkFragment.setArguments(bundle_2);
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.main_content, bookmarkFragment)
//                        .commit();
                        mainActivity.getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.main_content, bookmarkFragment)
                                .commit();

                        return true;
                    case R.id.action_map:



                        androidx.fragment.app.Fragment mapFragment = new MapFragment();

                        Bundle bundle_3 = new Bundle();
                        bundle_3.putInt(FRAGMENT_ARG,3);

                        mapFragment.setArguments(bundle_3);
//                getFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.main_content, mapFragment)
//                        .commit();
                        mainActivity.getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.main_content, mapFragment)
                                .commit();

                        return true;
                    case R.id.action_account:



                        androidx.fragment.app.Fragment userFragment = new UserFragment();
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        Bundle bundle_4 = new Bundle();
                        bundle_4.putString("destinationUid", uid);
                        bundle_4.putInt(FRAGMENT_ARG, 4);

                        userFragment.setArguments(bundle_4);

//                getFragmentManager().beginTransaction()
//                        .replace(R.id.main_content, userFragment)
//                        .commit();
                        mainActivity.getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.main_content, userFragment)
                                .addToBackStack(null)
                                .commit();

                        return true;
                }


                return false;
            }
        });
    }
}
