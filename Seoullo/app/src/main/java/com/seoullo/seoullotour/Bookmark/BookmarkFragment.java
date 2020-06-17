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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
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
import com.seoullo.seoullotour.Bookmark.cardviewpager.CardAdapter;
import com.seoullo.seoullotour.Bookmark.cardviewpager.CardFragmentPagerAdapter;
import com.seoullo.seoullotour.Bookmark.cardviewpager.CardPagerAdapter;
import com.seoullo.seoullotour.Bookmark.cardviewpager.ShadowTransformer;
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
    public ViewPager mViewPager;
    public LinearLayout mLinearLayout;
    private CardView mCardView;
    private ArrayList<Bookmark> mBookmarkList;
    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private CardFragmentPagerAdapter mFragmentCardAdapter;
    private ShadowTransformer mFragmentCardShadowTransformer;

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
        mLinearLayout = view.findViewById(R.id.group_viewPager);

        mCardView = (CardView) view.findViewById(R.id.cardView);
        //mCardView.setMaxCardElevation(mCardView.getCardElevation() * CardAdapter.MAX_ELEVATION_FACTOR);

        mFragmentCardAdapter = new CardFragmentPagerAdapter(getFragmentManager(),
                dpToPixels(2, this));

        mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
        mFragmentCardShadowTransformer = new ShadowTransformer(mViewPager, mFragmentCardAdapter);

        mViewPager.setAdapter(mCardAdapter);
        mViewPager.setPageTransformer(false, mCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(3);
        Log.d(TAG, "bookmark ing");
        return view;
    }

    public static float dpToPixels(int dp, BookmarkFragment context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    public CardView getCardView() {
        return mCardView;
    }
    public void createBookmark(){
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
                            mCardAdapter.addCardItem(bookmark);
                        }
                        mCardAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });

    }


}