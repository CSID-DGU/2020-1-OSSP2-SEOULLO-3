package com.seoullo.seoullotour.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
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
import com.seoullo.seoullotour.Models.Bookmark;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
    private boolean isDrawed = false;
    //save direction routes
    private Route mRoute;
    private List<LatLng> mPathList = new ArrayList<>();
    private Point currentPoint;
    //search map
    private Geocoder geocoder;
    private Point mSearchedPoint;
    //widget
    private TextView mGuide;
    private TextView mVicinity;
    private Button mDirection;


    private ImageButton mShowGuide, mBookmarkBtn;
    private ListView mListGuide, mListBookmark;
    private RelativeLayout mRelDirection;
    private RelativeLayout mRelSearch;
    private MapListAdapter mapListAdapter;
    private AutoCompleteTextView mAutoCompleteTextView;
    private ImageButton mSearchBtn;
    private LinearLayout mLinearLayout;

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
    public static String getApiKeyFromManifestGoogleAPI(Context context) {
        String apiKey = null;

        try {
            String e = context.getPackageName();
            ApplicationInfo ai = context
                    .getPackageManager()
                    .getApplicationInfo(e, PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null) {
                apiKey = bundle.getString("com.google.android.geo.API_KEY");
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
        //current location
        locationSource = new FusedLocationSource(getActivity(), LOCATION_PERMISSION_REQUEST_CODE);

        //xml layout
        View view = inflater.inflate(R.layout.activity_map, container, false);
        mGuide = (TextView) view.findViewById(R.id.navermap_guide);
        mVicinity = (TextView) view.findViewById(R.id.direction_vicinity);
        mDirection = (Button) view.findViewById(R.id.direction_btn);
        mListGuide = (ListView) view.findViewById(R.id.direction_list);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.direction_lin);
        mShowGuide = (ImageButton) view.findViewById(R.id.direction_showguide);
        mRelDirection = (RelativeLayout) view.findViewById(R.id.map_direction);
        mRelSearch = (RelativeLayout) view.findViewById(R.id.map_search_rel_layout);
        mAutoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.map_search);
        mSearchBtn = (ImageButton) view.findViewById(R.id.map_search_btn);
        mBookmarkBtn = (ImageButton) view.findViewById(R.id.map_search_btn_bookmark);
        mListBookmark = (ListView) view.findViewById(R.id.map_bookmark_list);

        mLinearLayout.setVisibility(View.VISIBLE);

        mRelDirection.setVisibility(View.INVISIBLE);
        mRelSearch.setVisibility(View.INVISIBLE);

        if(mPoint != null) {
            mRelDirection.setVisibility(View.VISIBLE);
            mRelSearch.setVisibility(View.GONE);
            mVicinity.setText(mPoint.location);
            mDirection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isDrawed) {
                        if (currentPoint != null) {  //current location enabled;
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
                        } else {    //currentPoint is null
                            Toast.makeText(getContext(),"왼쪽 하단에 현재 위치 설정을 한번 눌러주세요 !",Toast.LENGTH_SHORT).show();
                        }
                    } //if
                    else
                        Toast.makeText(getContext(),"이미 길찾기를 하셨습니다 !", Toast.LENGTH_SHORT).show();
                } //onClick
            });
            mInfoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                @NonNull
                @Override
                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                    infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                        @Override
                        public boolean onClick(@NonNull Overlay overlay) {
                            System.out.println("click event");

                            return false;
                        }
                    });
                    return mPoint.location;
                }
            });

        } //if point null

        //mPoint null 이면 검색창 띄워주기
        else {
            mRelSearch.setVisibility(View.VISIBLE);
            mRelDirection.setVisibility(View.GONE);

            //어댑터 생성
            ArrayAdapter adapter = new GooglePlacesAutocompleteAdapter(getContext(),R.layout.layout_list_item);
            mAutoCompleteTextView.setAdapter(adapter);

            mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View view, int position, long id) {
                    String str = (String) parent.getItemAtPosition(position);
                    mAutoCompleteTextView.setText(str);
                }
            });

            mSearchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread() {
                        public void run() {
                            Point pp = new Point();
                            geocoder = new Geocoder(getContext());
                            pp = getLatlngFromLocation(mAutoCompleteTextView.getText().toString());

                            Bundle bun = new Bundle();
                            bun.putString("lat", String.valueOf(pp.x));
                            bun.putString("lng", String.valueOf(pp.y));
                            bun.putString("location", pp.location);
                            Message msg = handler2.obtainMessage();
                            msg.setData(bun);
                            handler2.sendMessage(msg);
                        }
                    }.start();

                   InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != getActivity().getCurrentFocus())
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
                                .getApplicationWindowToken(), 0);
                }
            });

            final boolean[] isBookmarkListOpen = new boolean[]{false};

            mBookmarkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: 지도에 북마크한 객체 표시하기
                    if(!isBookmarkListOpen[0]) {
                        isBookmarkListOpen[0] = true;
                        mListBookmark.setVisibility(View.VISIBLE);

                        //get from firebase
                        Query query = FirebaseDatabase.getInstance().getReference()
                                .child("bookmarks")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final ArrayList<Bookmark> BookmarkList = new ArrayList<>();

                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Bookmark BM = new Bookmark();
                                    BM.setImage_name(singleSnapshot.child("image_name").toString());
                                    ArrayList<Double> LatLng = new ArrayList<>();
                                    LatLng.add(0, Double.parseDouble(singleSnapshot.child("latlng").child("0").getValue().toString()));
                                    LatLng.add(1, Double.parseDouble(singleSnapshot.child("latlng").child("1").getValue().toString()));
                                    BM.setLatlng(LatLng);
                                    String []locationSplit = singleSnapshot.child("location").getValue().toString().split(" ");
                                    String trimmedLocation = "";
                                    for(int loop=2; loop < locationSplit.length; ++loop) {
                                        trimmedLocation += " " +  locationSplit[loop];
                                    }
                                    BM.setLocation(trimmedLocation);
                                    BM.setUser_id(singleSnapshot.child("user_id").toString());
                                    BM.setPhoto_id(singleSnapshot.child("photo_id").toString());

                                    BookmarkList.add(BM);
                                }
                                ArrayList<String> BookmarkLocationList = new ArrayList<>();
                                for (int i = 0; i < BookmarkList.size(); ++i) {
                                    BookmarkLocationList.add(i, BookmarkList.get(i).getLocation());
                                }
                                try {
                                    mapListAdapter = new MapListAdapter(getContext(), BookmarkLocationList);
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }
                                mListBookmark.setAdapter(mapListAdapter);

                                mListBookmark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                        double lat = BookmarkList.get(position).getLatlng().get(0);
                                        double lng = BookmarkList.get(position).getLatlng().get(1);

                                        LatLng itemLatLng = new LatLng(lat, lng);
                                        nMarker.setMap(null);
                                        nMarker.setPosition(itemLatLng);
                                        nMarker.setMap(nMap);

                                        final InfoWindow mInfo = new InfoWindow();
                                        mInfo.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                                            @NonNull
                                            @Override
                                            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                                String [] trimmed = BookmarkList.get(position).getLocation().split(" ");

                                                return trimmed[trimmed.length - 1];
                                            }
                                        });
                                        final boolean[] isInfoWindowOpen = {false};
                                        nMarker.setOnClickListener(new Overlay.OnClickListener() {
                                            @Override
                                            public boolean onClick(@NonNull Overlay overlay) {
                                                if(!isInfoWindowOpen[0]) {
                                                    mInfo.open(nMarker);
                                                    isInfoWindowOpen[0] = true;
                                                } else {
                                                    mInfo.close();
                                                    isInfoWindowOpen[0] = false;
                                                }
                                                return false;
                                            }
                                        });

                                        nMap.moveCamera(CameraUpdate.scrollAndZoomTo(itemLatLng, 14f));
                                    }
                                });

                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } //if
                    else {
                        mListBookmark.setVisibility(View.GONE);
                        isBookmarkListOpen[0] = false;
                    }


//                    try {
//                        mapListAdapter = new MapListAdapter(getContext(),bookmark_location);
//                    } catch (CloneNotSupportedException e) {
//                        e.printStackTrace();
//                    }
                }
            });

        }

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
                isDrawed = true;
                //draw path here
                PathOverlay path = new PathOverlay();
                path.setCoords(mPathList);
                path.setColor(Color.parseColor("#049DD9"));
                path.setMap(nMap);

                mListGuide.setVisibility(View.INVISIBLE);

                mShowGuide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                       if(mListGuide.getVisibility() == View.INVISIBLE) {
                           try {
                               mapListAdapter = new MapListAdapter(getContext(), mRoute.getGuideArray());
                           } catch (CloneNotSupportedException e) {
                               e.printStackTrace();
                           }
                           mListGuide.setAdapter(mapListAdapter);
                           mListGuide.setVisibility(View.VISIBLE);
                       }

                       else {
                           mListGuide.setAdapter(null);
                           mListGuide.setVisibility(View.INVISIBLE);
                       }
                    }
                });

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

                if(Min + (int)durationMinute >= 60) {
                   int toHrs = (Min + (int)durationMinute) / 60;
                   Min = (Min + (int)durationMinute) % 60;
                   Hrs = Hrs + toHrs;
                } else {
                    Min += (int)durationMinute;
                }
                float toKM = mRoute.getDistance() / 1000;

                mGuide.setText("총 거리 : " + toKM + "km " + "\n도착 시간 : " + Hrs +" 시 " + Min + " 분 도착 예정입니다.");
                mGuide.setTextSize(10);
                mGuide.setVisibility(View.VISIBLE);

                if(locationSource.getLastLocation() == null) {
                    Toast.makeText(getContext(), "현재위치정보를 한번 눌러주세요 !", Toast.LENGTH_LONG).show();
                    nMap.moveCamera(CameraUpdate.zoomTo(13f));
                }
                else {
                    LatLng currentPosition = new LatLng(locationSource.getLastLocation().getLatitude(), locationSource.getLastLocation().getLongitude());
                    nMap.moveCamera(CameraUpdate.scrollAndZoomTo(currentPosition, 13f));
                }

            }
        }
    };
    //handler
    @SuppressLint("HandlerLeak")
    Handler handler2 = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            Double lat = Double.parseDouble(bun.getString("lat"));
            Double lng = Double.parseDouble(bun.getString("lng"));
            final String location = bun.getString("location");

            if(lat != null && lng != null) {
                LatLng latLng = new LatLng(lat, lng);
                final Marker nMarker = new Marker();
                nMarker.setPosition(latLng);
                nMarker.setMap(nMap);

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
                mInfoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                    @NonNull
                    @Override
                    public CharSequence getText(@NonNull InfoWindow infoWindow) {
                        infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                            @Override
                            public boolean onClick(@NonNull Overlay overlay) {
                                System.out.println("click event");

                                return false;
                            }
                        });
                        return location;
                    }
                });

                nMap.moveCamera(CameraUpdate.scrollAndZoomTo(latLng, 14f));
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
        uiSettings.setLogoMargin(5, 1500, 440, 5);
        uiSettings.setAllGesturesEnabled(true);

        //location change listener
        nMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                currentPoint = new Point();
                currentPoint.x = location.getLatitude();
                currentPoint.y = location.getLongitude();
                nMap.setLocationTrackingMode(LocationTrackingMode.Follow);
//                Toast.makeText(getContext(),
//                        location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_LONG).show();
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
                "?start=" +  this.currentPoint.y   + "," +    this.currentPoint.x +
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
            String lng = mPath.get(i).substring(mPath.get(i).indexOf("[") + 1,mPath.get(i).indexOf(","));
            String lat = mPath.get(i).substring(mPath.get(i).indexOf(",") + 1,mPath.get(i).length()-1);

            System.out.println("added : " + lat + "," + lng);

            LatLng latLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
            mPathList.add(latLng);
        }
    }
    //-----------------------------------PLACE AUTO COMPLETE ---------------------------------------------------------//
    private static final String LOG_TAG = "GOOGLE_PLACE_AUTOCOMPLETE";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    //google place auto complete
    //place autocomplete custom version
    @SuppressLint("LongLogTag")
    public ArrayList autocomplete(String input) {

        //google api key
        final String GOOGLE_API_KEY = getApiKeyFromManifestGoogleAPI(getContext());

        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + GOOGLE_API_KEY);
            sb.append("&language=ko");
            sb.append("&components=country:kr");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
    //TODO: adapter upgrade -> recyclerview
    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return (String) resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
    //================================================================GEOCODING !=================================================================
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
