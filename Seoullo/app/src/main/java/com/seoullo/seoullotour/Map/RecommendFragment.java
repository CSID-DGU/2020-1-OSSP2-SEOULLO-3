package com.seoullo.seoullotour.Map;

import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

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

        return view;
    }
}
