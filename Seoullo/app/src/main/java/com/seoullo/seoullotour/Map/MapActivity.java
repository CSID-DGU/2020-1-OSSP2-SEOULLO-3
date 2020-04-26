package com.seoullo.seoullotour.Map;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.seoullo.seoullotour.R;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";
    public static final int ACTIVITY_NUM = 3;

    //TODO: link to fragment
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Log.d(TAG, "onCreate: starting.");
    }
}
