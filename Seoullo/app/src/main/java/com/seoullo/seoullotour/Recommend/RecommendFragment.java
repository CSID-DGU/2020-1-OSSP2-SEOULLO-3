package com.seoullo.seoullotour.Recommend;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.annotations.Nullable;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.Models.Place;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecommendFragment extends Fragment implements OnMapReadyCallback {

    //variables
    private static String TAG ="Recommend Fragment";
    //현위치
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    //naver map
    private Marker marker = new Marker();
    private MapView mapView;
    public NaverMap nMap;
    private String NAVER_CLIENT_ID = "";
    //정보가져올 DTO
    private String findLocation;
    public Point point;
    private Geocoder geocoder;
    ArrayList<Place> placeList;
    //뷰페이저
    public ViewPager viewPager;
    public LinearLayout mLinearLayout;
    //기본 생성자
    public RecommendFragment() { }
    //값 받아오기
    public RecommendFragment(String ref1, ArrayList<Place> ref2) {
        this.findLocation = ref1;
        this.placeList = (ArrayList<Place>) ref2.clone();
    }

    //naver api key
    public static String getApiKeyFromManifest(Context context) {
        String apiKey = null;

        try {
            String e = context.getPackageName();
            ApplicationInfo ai = context
                    .getPackageManager()
                    .getApplicationInfo(e, PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if(bundle != null) {
                apiKey = bundle.getString("com.naver.maps.map.CLIENT_ID");
            }
        } catch (Exception var6) {
            Log.d(TAG, "Caught non-fatal exception while retrieving apiKey: " + var6);
        }

        return apiKey;
    }

    //TODO::Firebase연동 + 지도 연동
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //네이버지도
        NAVER_CLIENT_ID = getApiKeyFromManifest(this.getContext());
        NaverMapSdk.getInstance(this.getContext()).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(NAVER_CLIENT_ID));

        //xml layout
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);
        mLinearLayout = view.findViewById(R.id.group_viewPager);

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
//        if(placeList.size() != 0) {
//
//        } else {
//            Toast.makeText(this.getContext(),"placeList is null",Toast.LENGTH_LONG).show();
//        }
        viewPagerAdapter.addItem(new RecommendFirstFragment(placeList.get(0), mLinearLayout));
        viewPagerAdapter.addItem(new RecommendSecondFragment(placeList.get(1)));
        viewPagerAdapter.addItem(new RecommendThirdFragment(placeList.get(2)));

        viewPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getContext(),"this is view pager"+ event.getY(), Toast.LENGTH_LONG).show();
                return false;
            }
        });

        viewPager.setAdapter(viewPagerAdapter);

        return view;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull final View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //네이버지도 싱크
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        //줌 버튼 이동
//        ZoomControlView zoomControlView = view.findViewById(R.id.navermap_zoom_control);
//        zoomControlView.setMap(nMap);
        mapView.getMapAsync(this);

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
    //네이버 지도 콜
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        nMap = naverMap;
        nMap.getUiSettings()
                .setZoomControlEnabled(false);
        //click event
        marker.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {

                InfoWindow infoWindow = new InfoWindow();
                infoWindow.open(marker);

                return false;
            }
        });
        //location
        nMap.setLocationSource(locationSource);
        //geocoding
        geocoder = new Geocoder(this.getContext());
        point = getLatlngFromLocation(findLocation);

        //get to location
        LatLng latLng = new LatLng(point.x, point.y);
        //Marker marker = new Marker();
        marker.setPosition(latLng);
        marker.setMap(nMap);
        nMap.moveCamera(CameraUpdate.scrollAndZoomTo(latLng,17f));
    }
    //geocoding
    public Point getLatlngFromLocation(String findLocation) {

        Point resultPoint = new Point();

        String str = findLocation;
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocationName(
                    str,
                    10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultPoint.x = Double.parseDouble(String.valueOf(addressList.get(0).getLatitude()));
        resultPoint.y = Double.parseDouble(String.valueOf(addressList.get(0).getLongitude()));
        resultPoint.location = findLocation;

        return resultPoint;
    }
}
