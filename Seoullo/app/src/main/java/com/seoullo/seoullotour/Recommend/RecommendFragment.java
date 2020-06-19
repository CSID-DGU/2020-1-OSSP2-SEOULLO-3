package com.seoullo.seoullotour.Recommend;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.seoullo.seoullotour.Map.MapActivity;
import com.seoullo.seoullotour.Models.Place;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RecommendFragment extends Fragment implements OnMapReadyCallback {

    //variables
    private static String TAG ="Recommend Fragment";
    //현위치
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    //naver map
    private Marker marker = new Marker();
    private MapView mapView;
    private NaverMap nMap;
    private String NAVER_CLIENT_ID = "";
    private InfoWindow infoWindow = new InfoWindow();
    //정보가져올 DTO
    private Geocoder geocoder;
    ArrayList<Place> placeList;
    ArrayList<Double> mLatLng;
    private String location;
    private String UserId;
    private String ImageName;
    private String PhotoId;
    private Place mPlace;
    private int cnt = 0;
    //뷰페이저
    public ViewPager viewPager;
    public LinearLayout mLinearLayout;
    //기본 생성자
    public RecommendFragment() { }
    //값 받아오기
    public RecommendFragment(Place ref1, ArrayList<Place> ref2, String ref3, String ref4, String ref5) {
        this.mPlace = ref1;
        this.placeList = (ArrayList<Place>) ref2.clone();
        this.UserId = ref3;
        this.ImageName = ref4;
        this.PhotoId = ref5;
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
        System.out.println("==================================Recommend Fragment");

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
        Random rand = new Random();
        viewPagerAdapter.addItem(new RecommendFirstFragment(UserId, ImageName, PhotoId));
        viewPagerAdapter.addItem(new RecommendSecondFragment(placeList.get(1)));
        viewPagerAdapter.addItem(new RecommendThirdFragment(placeList.get(2)));

        //TODO : viewPager 스크롤 이벤트 처리
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
                                return placeList.get(1).getName();
                            case 2:
                                return placeList.get(2).getName();
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

                            LatLng latlng1 = new LatLng(placeList.get(1).getLatitude(), placeList.get(1).getLongitude());
                            marker.setPosition(latlng1);
                            marker.setIconTintColor(Color.YELLOW);
                            marker.setMap(nMap);
                            CameraUpdate cameraUpdate1 = CameraUpdate.scrollAndZoomTo(latlng1, 16f);
                            nMap.moveCamera(cameraUpdate1);

                            break;
                        case 2:

                            LatLng latlng2 = new LatLng(placeList.get(2).getLatitude(), placeList.get(2).getLongitude());
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

        //ui setting
        UiSettings ui = nMap.getUiSettings();
        ui.setLocationButtonEnabled(false);
        ui.setZoomControlEnabled(false);
        ui.setLogoGravity(1);
        ui.setLogoMargin(5,5, 450, 1000);
        ui.setZoomGesturesEnabled(true);

        final boolean[] infoEvent = {false};
        //only once
        if(cnt == 0) {
            //default
            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                @NonNull
                @Override
                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                    infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                        @Override
                        public boolean onClick(@NonNull Overlay overlay) {
                            Point point = new Point();
                            point.x = mPlace.getLatitude();
                            point.y = mPlace.getLongitude();
                            point.location = mPlace.getVicinity();

                            Intent intent = new Intent(getActivity(), MapActivity.class);
                            intent.putExtra("point", point);
                            startActivity(intent);

                            return false;
                        }
                    });
                    return "선택하신 곳 : \n" + mPlace.getVicinity();

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
            cnt = -1;
        }
        if(marker.getInfoWindow() != null) {
            infoWindow.close();
        }
        nMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                infoWindow.close();
            }
        });

        //geocoding
        geocoder = new Geocoder(this.getContext());
        //location
        nMap.setLocationSource(locationSource);

        //get to location
        LatLng latLng = new LatLng(mPlace.getLatitude(), mPlace.getLongitude());
        //Marker marker = new Marker();
        marker.setPosition(latLng);
        marker.setMap(nMap);
        nMap.moveCamera(CameraUpdate.scrollAndZoomTo(latLng,16f));
    }
}
