package com.seoullo.seoullotour.Home;

import android.app.DownloadManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seoullo.seoullotour.Models.Bookmark;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.Models.Comment;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.Utils.MainfeedListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private RequestManager mRequestManager;
    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mAllUserPosts;
    private ArrayList<Photo> photos;

    private ListView mListView;
    private com.seoullo.seoullotour.Utils.MainfeedListAdapter mAdapter;
    private int mResults;

    private static final String ARG_PARAM1 = "param1";
    private String mParam;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public static Fragment newInstance(Photo clickedPhoto, String photoID) {
        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, photoID);
        bundle.putSerializable("object", clickedPhoto);
        fragment.setArguments(bundle);
        return fragment;
    }
    public static Fragment newInstance(Bookmark clickedPhoto, String photoID) {
        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, photoID);
        bundle.putSerializable("object", clickedPhoto);
        fragment.setArguments(bundle);
        return fragment;
    }
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        mParam = getArguments().getString(ARG_PARAM1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mAllUserPosts = new ArrayList<>();
        Photo photo = new Photo();
        mPhotos = new ArrayList<>();
        mRequestManager = Glide.with(this);
//        getAllPosts();
        getPhotos();
        return view;
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: getting photos");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.dbname_photos))
                .orderByChild(getString(R.string.field_photo_id))
//                .equalTo(mParam)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.getValue() != null) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                Photo photo = new Photo();
                                Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                if (!objectMap.get(getString(R.string.field_photo_id)).toString().equals(mParam))
                                    continue;
                                photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                                photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                                photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                                photo.setImage_name(objectMap.get("image_name").toString());
                                photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                                photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                                photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                                photo.setLikeCount(Integer.parseInt(objectMap.get("likeCount").toString()));
                                ArrayList<Comment> comments = new ArrayList<Comment>();
                                for (DataSnapshot dSnapshot : singleSnapshot
                                        .child(getString(R.string.field_comments)).getChildren()) {
                                    Comment comment = new Comment();
                                    comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                    comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                    comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                    comments.add(comment);
                                }

                                photo.setComments(comments);
                                mPhotos.add(photo);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        photos = new ArrayList<>();

        reference.child(getString(R.string.dbname_photos))
//                .child(getString(R.string.field_photo_id))
                .orderByChild(getString(R.string.field_photo_id))
//                .orderByValue()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                            Photo photo = new Photo();

                            Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                            if (objectMap.get(getString(R.string.field_photo_id)).toString().equals(mParam))
                                continue;
                            photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                            photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                            photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                            Log.d(TAG, "getPhoto_id" + photo.getPhoto_id());
                            photo.setImage_name(objectMap.get("image_name").toString());
                            photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                            photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                            photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                            photo.setLikeCount(Integer.parseInt(objectMap.get("likeCount").toString()));

                            ArrayList<Comment> comments = new ArrayList<Comment>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child(getString(R.string.field_comments)).getChildren()) {
                                Comment comment = new Comment();
                                comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                comments.add(comment);
                            }
                            photo.setComments(comments);
                            photos.add(photo);

                        }

                        displayPhotos();

                        for (int i = 0; i < photos.size(); i++) {

                            mPhotos.add(photos.get(i));
                        }

                        try {
                            mResults = 10;
                            mAdapter = new com.seoullo.seoullotour.Utils.MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPhotos,mRequestManager);
                            mListView.setAdapter(mAdapter);

//                            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                                    int pos = parent.getPositionForView(v);
//                                    System.out.println("11111"+pos+"**");
//                                    System.out.println("22222"+v.getId()+"**");
//                                    if(id == 222220) {
//                                        PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), v);
//                                        popup.getMenuInflater().inflate(R.menu.hello, popup.getMenu());
//                                        //
//                                        popup.show();
//                                    }
//                                }
//
//                            });


                        } catch (NullPointerException e) {
                            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
                        } catch (IndexOutOfBoundsException e) {
                            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void displayPhotos() {
        try {
            Collections.sort(photos, new Comparator<Photo>() {
                @Override
                public int compare(Photo photo1, Photo photo2) {
                    return Integer.valueOf(photo2.getLikeCount()).compareTo(photo1.getLikeCount());
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
        }


    }


    public void displayMorePhotos() {
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try {

            if (mPhotos.size() > mResults && mPhotos.size() > 0) {
                int iterations;
                if (mPhotos.size() > (mResults + 10)) {
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                } else {
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPhotos.size() - mResults;
                }

                //add the new photos to the paginated results
                for (int i = mResults; i < mResults + iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
        }
    }
    //팔로우 하는 사람 게시물만 보여지도록 함 --> 우리는 다 볼 수 있도록 하는게 목적
    //--> 필요가 없는 것 같음.
/*    private void getAllPosts() {
        Log.d(TAG, "getAllPosts: show all posts");

        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_photos))
                .orderByChild(getString(R.string.field_likes_count))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: found posts: " +
                                    singleSnapshot.child(getString(R.string.field_photo_id)).getValue());

                            mAllUserPosts.add(singleSnapshot.child(getString(R.string.field_photo_id)).getValue().toString());
                        }
                        //get the photos
                        getPhotos();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }*/

}