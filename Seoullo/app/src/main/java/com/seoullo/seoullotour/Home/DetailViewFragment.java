package com.seoullo.seoullotour.Home;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.seoullo.seoullotour.Models.Bookmark;
import com.seoullo.seoullotour.Models.User;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.databinding.ItemDetailviewBinding;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DetailViewFragment extends androidx.fragment.app.Fragment {
    private ItemDetailviewBinding binding;
    private FirebaseUser user;
    private ArrayList<String> contentDTOs;
    private HomeActivity activity;
    private User contentDTO;
    //private BookmarkDTO bookmarkDTO;

    public int SEC = 60;
    public int MIN = 60;
    public int HOUR = 24;
    public int DAY = 30;
    public int MONTH = 12;

    private static final String ARG_PARAM1 = "param1";
    private int mParam;

    public static Fragment newInstance(User contentDTO, int position) {
        DetailViewFragment fragment = new DetailViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PARAM1, position);
        bundle.putSerializable("object", (Serializable) contentDTO);
        fragment.setArguments(bundle);
        return fragment;
    }

    public DetailViewFragment() {
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        mParam = getArguments().getInt(ARG_PARAM1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentDTO = (User) getArguments().getSerializable("object");
        binding = DataBindingUtil.inflate(inflater, R.layout.item_detailview, container, false);

        //TODO: bookmark
        //bookmarkDTO = new BookmarkDTO();

        View view = binding.getRoot();
        // View view = inflater.inflate(item_detailview, container, false);



        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://tourism-5864e.appspot.com");
        storageReference.child("userProfileImages").child(contentDTO.uid).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(activity)
                                .load(uri)
                                .apply(new RequestOptions().circleCrop()).into(binding.detailviewitemProfileImage);
                    }
                });


        storageReference.child("images").child(contentDTO.imageName).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(activity)
                                .load(uri)
                                .into(binding.detailviewitemImageviewContent);
                    }
                });

        contentDTOs = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("images").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                contentDTOs.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    contentDTOs.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        binding.detailviewitemProfileTextview.setText(contentDTO.user_id);
        //TODO:UserFragment
//        binding.detailviewitemProfileTextview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v){
//                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                FragmentManager fragmentManager =  getActivity().getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left,R.anim.exit_to_right);
//                fragmentTransaction.replace(R.id.main_content,UserFragment.newInstance(contentDTO.uid,uid));
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();
//            }
//        });

        binding.detailviewitemExplainTextview.setText(contentDTO.explain);

        //위치 추가됨! - 03/30 20:00

        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date_time = new Date();
        try {
            date_time = transFormat.parse(contentDTO.timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        binding.detailviewitemTimeTextview.setText(calculateTime(date_time));

        binding.detailviewitemLocation.setText(contentDTO.location);
        if (contentDTO
                .favorites.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite);
        } else {
            binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border);
        }
        binding.detailviewitemFavoritecounterTextview.setText("좋아요 " + contentDTO.favoriteCount + "개");

//        if(FirebaseDatabase.getInstance().getReference().child("bookmark").child(user.getUid()).get)


        if (contentDTO
                .bookmarks.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            binding.detailviewitemBookmarkImageview.setImageResource(R.drawable.ic_bookmark);
        } else {
            binding.detailviewitemBookmarkImageview.setImageResource(R.drawable.ic_bookmark_border);
        }
//TODO:CommentFragment

//        binding.detailviewitemCommentImageview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v){
////                ((MainActivity)getActivity()).replaceFragment(CommentFragment.newInstance(contentDTOs.get(mParam)));
//                FragmentManager fragmentManager =  getActivity().getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left,R.anim.exit_to_right);
//                fragmentTransaction.replace(R.id.main_content,CommentFragment.newInstance(contentDTOs.get(mParam)));
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();
//            }
//        });

        //클릭했을 때, bookmark한 사용자 저장.
        binding.detailviewitemBookmarkImageview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("images").child(contentDTOs.get(mParam))
                        .runTransaction(new Transaction.Handler() {

                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                if (contentDTO == null) {
                                    return Transaction.success(mutableData);
                                }
                                if (contentDTO.bookmarks.containsKey(uid)) {
                                    contentDTO.bookmarkCount = contentDTO.bookmarkCount - 1;
                                    contentDTO.bookmarks.remove(uid);
                                } else {
                                    contentDTO.bookmarkCount = contentDTO.bookmarkCount + 1;
                                    contentDTO.bookmarks.put(uid, true);
                                    // favoriteAlarm(contentDTO.uid);
                                }
                                mutableData.setValue(contentDTO);

                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                            }
                        });
                getBookmark(contentDTOs.get(mParam));

            }
        });

        binding.detailviewitemFavoriteImageview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("images").child(contentDTOs.get(mParam))
                        .runTransaction(new Transaction.Handler() {

                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                System.out.println(contentDTOs.get(mParam));

                                if (contentDTO == null) {
                                    System.out.println("contentDTO없어용");
                                    return Transaction.success(mutableData);
                                }
                                if (contentDTO.favorites.containsKey(uid)) {
                                    System.out.println("favoriteCount-1");

                                    // Unstar the post and remove self from stars
                                    contentDTO.favoriteCount = contentDTO.favoriteCount - 1;
                                    contentDTO.favorites.remove(uid);
                                } else {
                                    System.out.println("favoriteCount+1");
                                    // Star the post and add self to stars
                                    contentDTO.favoriteCount = contentDTO.favoriteCount + 1;
                                    contentDTO.favorites.put(uid, true);
                                }

                                if (contentDTO
                                        .favorites.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite);

                                } else {
                                    binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border);

                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.detailviewitemFavoritecounterTextview.setText("좋아요 " + contentDTO.favoriteCount + "개");
                                    }
                                });
                                // Set value and report transaction success
                                mutableData.setValue(contentDTO);

                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                            }
                        });
            }
        });
        //텍스트 뷰 클릭을 해보자 !
        binding.detailviewitemLocation.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
//                ((MainActivity) getActivity()).replaceFragment(ReviewMapFragment.newInstance(contentDTO));
                Toast.makeText(getContext(),binding.detailviewitemLocation.getText().toString(),Toast.LENGTH_LONG).show();
                //TODO: ReviewMap Fragment
//                FragmentManager fragmentManager =  getActivity().getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left,R.anim.exit_to_right);
//                fragmentTransaction.replace(R.id.main_content,ReviewMapFragment.newInstance(contentDTO));
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();

            }
        });

        return view;//binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            activity = (HomeActivity) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((MainActivity) getActivity()).getBinding().progressBar.setVisibility(View.GONE);
    }


    void getBookmark(final String postingName) {

        FirebaseDatabase.getInstance().getReference().child("bookmark").child(user.getUid())
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {

                        Bookmark bookmarkDTO = mutableData.getValue(Bookmark.class);

                        if (bookmarkDTO == null) {

                            bookmarkDTO = new Bookmark();
                            bookmarkDTO.bookmarks.put(postingName, true);
                            mutableData.setValue(bookmarkDTO);

                            return Transaction.success(mutableData);
                        }
                        // Unstar the post and remove self from stars
                        if (bookmarkDTO.bookmarks.containsKey(postingName)) {
                            binding.detailviewitemBookmarkImageview.setImageResource(R.drawable.ic_bookmark_border);

                            bookmarkDTO.bookmarks.remove(postingName);
                        }
                        // Star the post and add self to stars
                        else {
                            binding.detailviewitemBookmarkImageview.setImageResource(R.drawable.ic_bookmark);
                            bookmarkDTO.bookmarks.put(postingName, true);
                        }

                        // Set value and report transaction success
                        mutableData.setValue(bookmarkDTO);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    }
                });
    }


    public String calculateTime(Date date)
    {

        long curTime = System.currentTimeMillis();
        long regTime = date.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;

        if (diffTime < SEC)
        {
            // sec
            msg = diffTime + "초전";
        }
        else if ((diffTime /= SEC) < MIN)
        {
            // min
            System.out.println(diffTime);

            msg = diffTime + "분전";
        }
        else if ((diffTime /= MIN) <HOUR)
        {
            // hour
            msg = (diffTime ) + "시간전";
        }
        else if ((diffTime /= HOUR) < DAY)
        {
            // day
            msg = (diffTime ) + "일전";
        }
        else if ((diffTime /= DAY) <MONTH)
        {
            // day
            msg = (diffTime ) + "달전";
        }
        else
        {
            msg = (diffTime) + "년전";
        }

        return msg;
    }
}

