package com.seoullo.seoullotour.Directions;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.TimeZoneFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.database.annotations.Nullable;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.Models.Route;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.Utils.BottomNavigationViewHelper;
import com.seoullo.seoullotour.Utils.SectionsPagerAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.client.utils.CloneUtils;

public class DirectionFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "DirectionActivity";
    public static final int ACTIVITY_NUM = 2;

    //param
    private String NAVER_CLIENT_ID="";
    //naver map
    private MapView mapView;
    private NaverMap nMap;
    //현위치
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    private List<LatLng> mPathList = new ArrayList<>();
    private Route mRoute;

    DirectionFragment(Route r) throws CloneNotSupportedException {
        this.mRoute = (Route) CloneUtils.clone(r);
        setPath();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @com.google.firebase.database.annotations.Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //API KEY init
        NAVER_CLIENT_ID = getApiKeyFromManifest(this.getContext());

        //네이버지도
        NaverMapSdk.getInstance(this.getContext()).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(NAVER_CLIENT_ID));

        //xml layout
        View view = inflater.inflate(R.layout.activity_map, container, false);

        //current location
        locationSource = new FusedLocationSource(getActivity(), LOCATION_PERMISSION_REQUEST_CODE);

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.naver_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        nMap = naverMap;
        //location
        nMap.setLocationSource(locationSource);

        //Ui setting
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);      //현위치버튼
        uiSettings.setZoomControlEnabled(true);         //줌버튼
        uiSettings.setIndoorLevelPickerEnabled(true);   //층별로 볼수있
        uiSettings.setLogoGravity(1);
        uiSettings.setLogoMargin(5, 5, 450, 1000);
        uiSettings.setZoomGesturesEnabled(true);    //줌 제스처

        System.out.println("setting path overlay");
        PathOverlay path = new PathOverlay();
        path.setCoords(mPathList);
        path.setColor(Color.YELLOW);
        path.setMap(nMap);

        System.out.println(path.getCoords().get(0));
        System.out.println(mPathList.get(0).latitude);

        LatLng latLng0 = new LatLng(mPathList.get(0).latitude, mPathList.get(0).longitude);
        nMap.moveCamera(CameraUpdate.scrollAndZoomTo(latLng0,15f));
    }
    //view life cycle
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
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }
    private void setPath() {
        System.out.println("Direction Frag");
        ArrayList<String> mPath = (ArrayList<String>) mRoute.getPathArray().clone();

        for(int i=0; i<mPath.size(); ++i) {
            String lng = mPath.get(i).substring(mPath.get(i).indexOf("[") + 1,mPath.get(i).indexOf(",") - 1);
            String lat = mPath.get(i).substring(mPath.get(i).indexOf(",") + 1,mPath.get(i).indexOf("]") - 1);

            System.out.println("added : " + lat + "," + lng);

            LatLng latLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
            mPathList.add(latLng);
        }
        System.out.println("Direction Frag >>>>> finished");
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
            if (bundle != null) {
                apiKey = bundle.getString("com.naver.maps.map.CLIENT_ID");
            }
        } catch (Exception var6) {
            Log.d(TAG, "Caught non-fatal exception while retrieving apiKey: " + var6);
        }

        return apiKey;
    }
}
