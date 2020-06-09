package com.seoullo.seoullotour.Bookmark;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.seoullo.seoullotour.R;

public class BookmarkFragment extends Fragment {
    private static final String TAG = "BookmarkFragment";
    ScrollView scrollView;

    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
//        scrollView = view.findViewById(R.id.horizontal_scrollView);
//        scrollView.setHorizontalScrollBarEnabled(true);
        Log.d(TAG, "bookmark ing");
        return view;
    }
}
