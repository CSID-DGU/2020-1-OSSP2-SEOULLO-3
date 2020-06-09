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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seoullo.seoullotour.R;

import java.util.ArrayList;

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

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bookmarkfragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new BookmarkRecyclerViewAdapter());
        Log.d(TAG, "bookmark ing");
        return view;
    }

    private class BookmarkRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<String> bookmarkList;

        BookmarkRecyclerViewAdapter() {

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bookmark, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final int finalPosition = position;


        }

        @Override
        public int getItemCount() {
            return 0;
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {


            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}