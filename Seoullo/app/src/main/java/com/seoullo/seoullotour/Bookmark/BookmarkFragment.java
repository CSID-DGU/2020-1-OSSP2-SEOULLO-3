package com.seoullo.seoullotour.Bookmark;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Overlay;
import com.seoullo.seoullotour.Home.GridFragment;
import com.seoullo.seoullotour.Home.HomeFragment;
import com.seoullo.seoullotour.Map.MapActivity;
import com.seoullo.seoullotour.Models.Bookmark;
import com.seoullo.seoullotour.Models.Comment;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.Recommend.RecommendFirstFragment;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookmarkFragment extends Fragment {
    private static final String TAG = "BookmarkFragment";
    public RequestManager mRequestManager;
    public ViewPager viewPager;
    public LinearLayout mLinearLayout;

    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);

//        scrollView = view.findViewById(R.id.horizontal_scrollView);
//        scrollView.setHorizontalScrollBarEnabled(true);
        mRequestManager = Glide.with(this);
        ArrayList<Photo> bookmark = new ArrayList<>();
        BookmarkRecyclerViewAdapter adapter = new BookmarkRecyclerViewAdapter(bookmark);
        mLinearLayout = view.findViewById(R.id.group_viewPager);

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        TabLayout tabLayout = (TabLayout)view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager, true);

        //adapt to viewpager
        com.seoullo.seoullotour.Recommend.ViewpagerAdapter.ViewpagerAdapter viewPagerAdapter =
                new com.seoullo.seoullotour.Recommend.ViewpagerAdapter.ViewpagerAdapter(getFragmentManager());

        //TODO: fragment
        //viewPagerAdapter.addItem(new RecommendFirstFragment(UserId, ImageName, PhotoId));


        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bookmarkfragment_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, true));

        Log.d(TAG, "bookmark ing");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //양옆 미리보기 : 수치는 숫자 조절로
        viewPager.setClipToPadding(false);
        viewPager.setPadding((int) (48 * getResources().getDisplayMetrics().density),
                0,
                (int) (48 * getResources().getDisplayMetrics().density),
                0);
        viewPager.setPageMargin((int) ((48 * getResources().getDisplayMetrics().density) / 2));
        //set current position
        viewPager.setCurrentItem(0, false);
        //marker bubble info window
        final boolean[] infoEvent = {false};

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {

                infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                    @NonNull
                    @Override
                    public CharSequence getText(@NonNull InfoWindow infoWindow) {
                        infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                            @Override
                            public boolean onClick(@NonNull Overlay overlay) {

                                Intent intent = new Intent(getActivity(), MapActivity.class);
                                if(position == 0) {
                                    Point point = new Point();
                                    point.x = mPlace.getLatitude();
                                    point.y = mPlace.getLongitude();
                                    point.location = mPlace.getVicinity();
                                    intent.putExtra("point", point);
                                }
                                else {
                                    Point point1 = new Point();
                                    point1.location = placeList.get(position - 1).getVicinity();
                                    point1.x = placeList.get(position - 1).getLatitude();
                                    point1.y = placeList.get(position - 1).getLongitude();
                                    intent.putExtra("point", point1);
                                }
                                startActivity(intent);
                                getActivity().finish();
                                return false;
                            }
                        });
                        switch (position) {
                            case 0:
                                return "선택하신 곳 \n" +mPlace.getVicinity();
                            case 1:
                                return placeList.get(0).getName();
                            case 2:
                                return placeList.get(1).getName();
                            default:
                                return "장소 추천";
                        }
                    }
                });
                //click event
                marker.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        if(infoEvent[0] == false) {
                            infoWindow.open(marker);
                            infoEvent[0] = true;
                        }
                        else {
                            infoWindow.close();
                            infoEvent[0] = false;
                        }
                        return true;
                    }
                });
                nMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                        infoWindow.close();
                    }
                });

                if(placeList.size() >= 3) {
                    switch (position) {
                        case 0:
                            LatLng latlng0 = new LatLng(mPlace.getLatitude(), mPlace.getLongitude());

                            marker.setPosition(latlng0);
                            marker.setIconTintColor(Color.GREEN);
                            marker.setMap(nMap);
                            CameraUpdate cameraUpdate0 = CameraUpdate.scrollAndZoomTo(latlng0, 16f);
                            nMap.moveCamera(cameraUpdate0);
                            break;
                        case 1:

                            LatLng latlng1 = new LatLng(placeList.get(0).getLatitude(), placeList.get(0).getLongitude());
                            marker.setPosition(latlng1);
                            marker.setIconTintColor(Color.YELLOW);
                            marker.setMap(nMap);
                            CameraUpdate cameraUpdate1 = CameraUpdate.scrollAndZoomTo(latlng1, 16f);
                            nMap.moveCamera(cameraUpdate1);

                            break;
                        case 2:

                            LatLng latlng2 = new LatLng(placeList.get(1).getLatitude(), placeList.get(1).getLongitude());
                            marker.setPosition(latlng2);
                            marker.setIconTintColor(Color.BLUE);
                            marker.setMap(nMap);
                            CameraUpdate cameraUpdate2 = CameraUpdate.scrollAndZoomTo(latlng2, 16f);
                            nMap.moveCamera(cameraUpdate2);

                            break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                infoWindow.setMap(null);
            }
        });
    }

    private class BookmarkRecyclerViewAdapter extends RecyclerView.Adapter<BookmarkRecyclerViewAdapter.ViewHolder> {
        private ArrayList<Bookmark> mBookmarkList;
        private ArrayList<Photo> mBookmarkPhotos;

        public class ViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public ImageView imageView;
            public TextView textView;
            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.bookmark_image);
                textView = (TextView) itemView.findViewById(R.id.bookmark_location);
            }
        }

        //        public BookmarkRecyclerViewAdapter(ArrayList<Photo> bookmark) {
        public BookmarkRecyclerViewAdapter(ArrayList<Photo> bookmark) {

            mBookmarkList = new ArrayList<>();

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child(getString(R.string.dbname_bookmarks))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                    .orderByChild(getString(R.string.field_photo_id))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mBookmarkList.clear();
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                Bookmark bookmark = new Bookmark();
                                Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                bookmark.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                                bookmark.setImage_name(objectMap.get("image_name").toString());
                                bookmark.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                                bookmark.setLocation(objectMap.get("location").toString());
                                bookmark.setLatlng((ArrayList<Double>) objectMap.get("latlng"));
                                mBookmarkList.add(bookmark);
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });
            for(int i=0;i<mBookmarkList.size();i++) {
                reference.child(getString(R.string.dbname_photos))
                        .orderByChild(getString(R.string.field_photo_id))
                        .equalTo(mBookmarkList.get(i).getPhoto_id())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                mBookmarkPhotos.clear();
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

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

                                    mBookmarkPhotos.add(photo);
                                }
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }

                        });
            }
        }

        @Override
        public BookmarkRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View bookmarkView = inflater.inflate(R.layout.recycler_bookmark_item, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(bookmarkView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(final BookmarkRecyclerViewAdapter.ViewHolder viewHolder, final int position) {
            // Get the data model based on position
            TextView textView = viewHolder.textView;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference()
//                    .child(getString(R.string.dbname_bookmarks))
//                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("photos").child("users")
                    .child(mBookmarkList.get(position).getUser_id())
                    .child(mBookmarkList.get(position).getImage_name());
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mRequestManager
                                .load(uri)
                                .into(viewHolder.imageView);
                        }
                    });
//            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        // Glide 이용하여 이미지뷰에 로딩
//                        mRequestManager
//                                .load(task.getResult())
//                                .override(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels / 3)
//                                .into(viewHolder.imageView);
//
//                    } else {
//                    }
//                }
//            });

            // Set item views based on your views and data model
            ImageView imageView = viewHolder.imageView;
            imageView.setEnabled(true);
//            textView.setText(mBookmarkPhotos.get(position).getLocation());

            textView.setEnabled(true);
            //TODO: 클릭하면 게시물로 이동하도록 해야 함
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.rel_layout_3, HomeFragment.newInstance(mBookmarkList.get(position),
                            mBookmarkList.get(position).getPhoto_id()));
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return mBookmarkList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public ImageView imageView;

            public CustomViewHolder(ImageView imageView) {
                super(imageView);
                this.imageView = imageView;

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("Recyclerview", "position = " + getAdapterPosition());
                    }
                });
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Log.d("Recyclerview", "position = " + getAdapterPosition());
                        return false;
                    }
                });

            }

            @Override
            public void onClick(View v) {

            }
        }
    }

}