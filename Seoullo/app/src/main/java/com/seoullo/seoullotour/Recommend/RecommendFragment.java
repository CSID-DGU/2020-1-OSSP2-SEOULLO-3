package com.seoullo.seoullotour.Recommend;

import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.annotations.Nullable;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.Models.Place;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.R;

import java.util.ArrayList;

public class RecommendFragment extends Fragment {

    //variables
    //현위치
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    //naver map
    private Marker marker = new Marker();
    private MapView mapView;
    public NaverMap nMap;
    //정보가져올 DTO
    public Point point;
    private Geocoder geocoder;
    ArrayList<Place> placeList;
    //뷰페이저
    private ViewPager viewPager;
    //기본 생성자
    public RecommendFragment() { }
    //프래그먼트 전환시 사용
    // 각각의 Fragment마다 Instance를 반환해 줄 메소드를 생성합니다.
//    public static RecommendFragment newInstance(Photo photo) {
//        RecommendFragment reviewMapFragment = new RecommendFragment();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("object", photo);
//        reviewMapFragment.setArguments(bundle);
//        return reviewMapFragment;
//    }

    //TODO::Firebase연동 + 지도 연동
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //네이버지도
        NaverMapSdk.getInstance(this.getContext()).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("@string/NAVER_CLIENT_ID"));

        //xml layout
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        //current location
        locationSource = new FusedLocationSource(getActivity(), LOCATION_PERMISSION_REQUEST_CODE);

        //viewPager : at last
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        TabLayout tabLayout = (TabLayout)view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager, true);

        //adapt to viewpager
        com.seoullo.seoullotour.Recommend.ViewpagerAdapter.ViewpagerAdapter viewPagerAdapter =
                new com.seoullo.seoullotour.Recommend.ViewpagerAdapter.ViewpagerAdapter(getFragmentManager());
        viewPagerAdapter.addItem(new RecommendFirstFragment());
        viewPagerAdapter.addItem(new RecommendSecondFragment());
        viewPagerAdapter.addItem(new RecommendThirdFragment());

        viewPager.setAdapter(viewPagerAdapter);

        return view;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull final View view,
                              @Nullable Bundle savedInstanceState) {
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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        locationSource = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //location permission method
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }
}
