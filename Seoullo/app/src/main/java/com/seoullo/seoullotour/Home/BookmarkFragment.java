package com.seoullo.seoullotour.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.seoullo.seoullotour.R;

public class BookmarkFragment extends Fragment {

    private FirebaseUser user;
    public BookmarkFragment() {
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
//        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bookmarkfragment_recyclerview);
//        recyclerView.setAdapter(new BookMarkRecyclerViewAdapter());
//        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
//        contentDTO = (ContentDTO) getArguments().getSerializable("object");
//        binding = DataBindingUtil.inflate(inflater, item_detailview, container, false);
//        binding.detailviewitemLocation.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.M)
//            @Override
//            public void onClick(View v) {
//
//                ((MainActivity)getActivity()).replaceFragment(ReviewMapFragment.newInstance(contentDTO));
//
//            }
//        });

        return view;
    }
}