package com.seoullo.seoullotour.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.annotations.Nullable;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationSource;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.Models.Route;
import com.seoullo.seoullotour.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.seoullo.seoullotour.Utils.SharedRoute;

import cz.msebera.android.httpclient.client.utils.CloneUtils;

//TODO : 북마크 지도에 표시하기
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static String TAG = "MapFragment";
    //naver map
    private MapView mapView;
    private NaverMap nMap;
    private Marker nMarker = new Marker();
    private InfoWindow mInfoWindow = new InfoWindow();
    private String NAVER_CLIENT_ID = "";
    private String NAVER_CLIENT_SECRET_ID = "";
    //현위치
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    //got from recommend
    private Point mPoint;
    //save direction routes
    private Route mRoute;
    private List<LatLng> mPathList = new ArrayList<>();
    //widget
    private TextView mGuide;

    MapFragment() {
    }

    MapFragment(Point ref1) {
        this.mPoint = ref1;
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

    public static String getApiKeyFromManifestSecret(Context context) {
        String apiKey = null;

        try {
            String e = context.getPackageName();
            ApplicationInfo ai = context
                    .getPackageManager()
                    .getApplicationInfo(e, PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null) {
                apiKey = bundle.getString("com.naver.maps.map.CLIENT_SECRET_ID");
            }
        } catch (Exception var6) {
            Log.d(TAG, "Caught non-fatal exception while retrieving apiKey: " + var6);
        }

        return apiKey;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //API KEY init
        NAVER_CLIENT_ID = getApiKeyFromManifest(this.getContext());
        NAVER_CLIENT_SECRET_ID = getApiKeyFromManifestSecret(this.getContext());

        //네이버지도
        NaverMapSdk.getInstance(this.getContext()).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(NAVER_CLIENT_ID));

        //xml layout
        View view = inflater.inflate(R.layout.activity_map, container, false);
        mGuide = (TextView) view.findViewById(R.id.navermap_guide);

        //current location
        locationSource = new FusedLocationSource(getActivity(), LOCATION_PERMISSION_REQUEST_CODE);

        mInfoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        System.out.println("click event");
                        new Thread() {
                            public void run() {
                                System.out.println("THREAD RUN");
                                String isSettedNow = "";
                                try {
                                    HttpConnection();
                                    isSettedNow = "true";
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Bundle bun = new Bundle();
                                bun.putString("setted", isSettedNow);
                                Message msg = handler.obtainMessage();
                                msg.setData(bun);
                                handler.sendMessage(msg);
                            }
                        }.start();
                        System.out.println("thread finished");
                        return false;
                    }
                });
                return mPoint.location;
            }
        });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.naver_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }
    //핸들러
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint({"ResourceType", "SetTextI18n"})
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String set = bun.getString("setted");
            if(set.equals("true")) {
                System.out.println("draw path !");

                //draw path here
                PathOverlay path = new PathOverlay();
                path.setCoords(mPathList);
                path.setColor(R.color.theme_lightblue);
                path.setMap(nMap);

                //draw text view
                int durationMilliesecond = mRoute.getDuration();
                double durationMinute = durationMilliesecond * ( 0.16666666666667 ) * 0.0001;

                String departureTime = mRoute.getDepartureTime().substring(
                        mRoute.getDepartureTime().indexOf("T")+1,
                        mRoute.getDepartureTime().length()-3
                );
                System.out.println(departureTime);
                String[] time = departureTime.split(":");

                String HH = time[0];
                String MM = time[1];

                int Hrs = Integer.parseInt(HH);
                int Min = Integer.parseInt(MM);

                System.out.println("HH : " + Hrs + "MM : " + Min + "지연 : " + durationMinute);

                if(Min + (int)durationMinute > 60) {
                   int toHrs = (Min + (int)durationMinute) / 60;
                   Min = (Min + (int)durationMinute) % 60;
                   Hrs = Hrs + toHrs;
                } else {
                    Min += (int)durationMinute;
                }

                mGuide.setText("도착 시간 : " + Hrs +" 시 " + Min + " 분 도착 예정입니다.");
                nMap.moveCamera(CameraUpdate.zoomTo(13f));
            }
        }
    };

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

        //location change listener
        nMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                nMap.setLocationTrackingMode(LocationTrackingMode.Follow);
                Toast.makeText(getContext(),
                        location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_LONG).show();
            }
        });

        if (mPoint != null) {
            LatLng latLng = new LatLng(mPoint.x, mPoint.y);

            final boolean[] isInfoWindowOpen = {false};
            nMarker.setOnClickListener(new Overlay.OnClickListener() {
                @Override
                public boolean onClick(@NonNull Overlay overlay) {
                    if (!isInfoWindowOpen[0]) {
                        mInfoWindow.open(nMarker);
                        isInfoWindowOpen[0] = true;
                    } else {
                        mInfoWindow.close();
                        isInfoWindowOpen[0] = false;
                    }
                    return false;
                }
            });
            nMarker.setPosition(latLng);
            nMarker.setMap(nMap);
            nMap.moveCamera(CameraUpdate.scrollAndZoomTo(latLng, 16f));
        }
        else {
            LatLng latLng = new LatLng(37.5582, 127.0002);
            nMap.moveCamera(CameraUpdate.scrollAndZoomTo(latLng, 16f));
        }
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

    protected void HttpConnection() throws IOException, CloneNotSupportedException, JSONException {

        String result = null;

        String mURL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving" +
                "?start=127.0002,37.5582" +
                "&goal=" + mPoint.y + "," + mPoint.x;
        // Open the connection
        URL url = new URL(mURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", NAVER_CLIENT_ID);
        conn.setRequestProperty("X-NCP-APIGW-API-KEY", NAVER_CLIENT_SECRET_ID);
        InputStream is = conn.getInputStream();

        System.out.println("RESPONSE CODE : " + conn.getResponseMessage());

        // Get the stream
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        // Set the result
        conn.disconnect();
        result = builder.toString();
        //save in json
        jsonParsing(result);
        //save in list
        setPath();
    }

    private void jsonParsing(String jsonString) throws JSONException, CloneNotSupportedException {

        Route resultRoute = new Route();
        ArrayList<String> mPathArray = new ArrayList<>();
        ArrayList<String> mGuideArray = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject routeObject = jsonObject.getJSONObject("route");
        JSONArray traoptimalArray = routeObject.getJSONArray("traoptimal");

        JSONObject resultObject = traoptimalArray.getJSONObject(0);
        //summary : {     }
        JSONObject summaryObject = (JSONObject) resultObject.get("summary");
        int mDistance = (int) summaryObject.get("distance");
        int mDuration = (int) summaryObject.get("duration");
        String mDepartureTime = summaryObject.get("departureTime").toString();
        //path : [      ]
        JSONArray pathArray = resultObject.getJSONArray("path");
        for (int i = 0; i < pathArray.length(); ++i) {
            String tempPath = "";
            tempPath = pathArray.get(i).toString();
            mPathArray.add(tempPath);
        }
        JSONArray guideArray = resultObject.getJSONArray("guide");
        for (int i = 0; i < guideArray.length(); ++i) {
            String tempInstructions = "";
            JSONObject guideObject = (JSONObject) guideArray.get(i);
            tempInstructions = guideObject.get("instructions").toString();
            mGuideArray.add(tempInstructions);
        }

        //set Route model
        resultRoute.setDepartureTime(mDepartureTime);
        resultRoute.setDistance(mDistance);
        resultRoute.setDuration(mDuration);
        resultRoute.setPathArray(mPathArray);
        resultRoute.setGuideArray(mGuideArray);

        mRoute =  (Route) CloneUtils.clone(resultRoute);

        System.out.println(mDepartureTime);
        System.out.println(mDistance);
        System.out.println(mDuration);
        for (int i = 0; i < resultRoute.getPathArray().size(); ++i)
            System.out.println(resultRoute.getPathArray().get(i));
        for (int i = 0; i < resultRoute.getGuideArray().size(); ++i)
            System.out.println(resultRoute.getGuideArray().get(i));
    }
    private void setPath() {
        ArrayList<String> mPath = (ArrayList<String>) mRoute.getPathArray().clone();

        for(int i=0; i<mPath.size(); ++i) {
            String lng = mPath.get(i).substring(mPath.get(i).indexOf("[") + 1,mPath.get(i).indexOf(",") - 1);
            String lat = mPath.get(i).substring(mPath.get(i).indexOf(",") + 1,mPath.get(i).length()-1);

            System.out.println("added : " + lat + "," + lng);

            LatLng latLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
            mPathList.add(latLng);
        }
    }
}
