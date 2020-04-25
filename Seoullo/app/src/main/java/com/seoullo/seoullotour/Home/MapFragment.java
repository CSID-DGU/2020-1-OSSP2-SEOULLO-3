package com.seoullo.seoullotour.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.annotations.Nullable;
import com.seoullo.seoullotour.R;

public class MapFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //xml layout
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        return view;
    }
}
