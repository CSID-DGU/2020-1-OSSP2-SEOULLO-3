package com.seoullo.seoullotour.Bookmark;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seoullo.seoullotour.Home.HomeFragment;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.R;

import java.util.HashMap;
import java.util.Map;

public class EmptyView extends Fragment {

    private Button addBookmarkButton;

    public static EmptyView newInstance() {
        return new EmptyView();
    }
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emptyview, container, false);
        addBookmarkButton = (Button) view.findViewById(R.id.add_bookmark_button);
        buttonEvent();
        return view;
    }
    public void buttonEvent(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final String[] photo_id = new String[1];
        reference.child(getString(R.string.dbname_photos))
                .child(getString(R.string.field_photo_id))
                .orderByChild(getString(R.string.field_likes_count))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                Photo photo = new Photo();
                                Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                photo_id[0] = objectMap.get(getString(R.string.field_photo_id)).toString();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        addBookmarkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.rel_layout_3, HomeFragment.newInstance(photo_id[0]));
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

}
