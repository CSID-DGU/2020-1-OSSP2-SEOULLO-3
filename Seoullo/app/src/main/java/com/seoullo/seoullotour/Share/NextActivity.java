package com.seoullo.seoullotour.Share;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seoullo.seoullotour.Models.Place;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.Utils.FirebaseMethods;
import com.seoullo.seoullotour.Utils.UniversalImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private EditText mCaption;
    private RecyclerView mAdapter;
    private AutoCompleteTextView mAuto;
    //vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgUrl;
    private String imgName;

    private Bitmap bitmap;
    private Intent intent;
    //place location
    private String location;
    private static String API_KEY = "";
    private Geocoder geocoder;
    private ArrayList<Place> placeList = new ArrayList<>();

    public NextActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = (EditText) findViewById(R.id.caption);
        mAdapter = (RecyclerView) findViewById(R.id.recyclerview_autocomplete);
        //API KEY init
        API_KEY = getApiKeyFromManifest(this);

        //autocomplete text
        mAuto = findViewById(R.id.places_autocomplete_edit_text);
        ArrayAdapter arrayAdapter = new GooglePlacesAutocompleteAdapter(getApplicationContext(), R.layout.layout_list_item);
        mAuto.setAdapter(arrayAdapter);
        mAuto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                String str = (String) adapterView.getItemAtPosition(position);
                mAuto.setText(str);
                location = str;
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        });

        setupFirebaseAuth();

        ImageView backArrow = (ImageView) findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        TextView share = (TextView) findViewById(R.id.tvShare);
        //TODO : 장소검색이 안되는 경우 처리 -> 알림창 !
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                //-------------------------------------------DB에 place올릴 스레드------------------------------------------
                //http server new thread() : android.os.NetworkOnMainThreadException
                // Thread로 웹서버에 접속
                new Thread() {
                    public void run() {
                        System.out.println("THREAD RUN");
                        new Point();
                        Point pp;
                        geocoder = new Geocoder(getApplicationContext());
                        System.out.println(mAuto.getText().toString());
                        pp = getLatlngFromLocation(mAuto.getText().toString());

                        String json = getNearby(pp);

                        Bundle bun = new Bundle();
                        bun.putString("json", json);
                        Message msg = handler.obtainMessage();
                        msg.setData(bun);
                        handler.sendMessage(msg);
                    }
                }.start();
                //==============================================place저장중===================================================
                //upload image to firebase
                Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
                String caption = mCaption.getText().toString();

                if (intent.hasExtra(getString(R.string.selected_image))) {
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    imgName = intent.getStringExtra("image_name");
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl, null ,location, imgName, placeList);

                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl, null, location,imgName, placeList);
                }
            }
        });
        setImage();
    }

    private void someMethod() {

        /**
         * Step 1) Create a data model for Photos
         * Step 2) Add properties to the Photo Objects (caption, date, imageURL, photo_id, tags, user_id)
         * Step 3) Count the number of pre-uploaded photos for a user
         * Step 4)
         * a) Upload photo and insert two new nodes in the Firebase Database
         * b) insert into 'photo' node
         * c) insert into 'user_photos' node
         */

    }

    /**
     * Gets image url from incoming intent and displays selected image
     */
    private void setImage(){
        intent = getIntent();
        ImageView image = (ImageView) findViewById(R.id.imageShare);

        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image url " + imgUrl);
            UniversalImageLoader.setImage(imgUrl, image, null, mAppend);

        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got a new bitmap");
            image.setImageBitmap(bitmap);

        }
    }

     /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        Log.d(TAG, "onDataChange: image count: " + imageCount);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count: " + imageCount);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
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
    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
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
    static class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
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
    //TODO: nearby recommended places !! -> 파싱을 고쳐야함 04/06
    public String getNearby(Point pDTO) {
        double latitude, longitude;
        latitude = pDTO.x; longitude = pDTO.y;
        String jsonText = "";

        String httpURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + latitude + "," + longitude +
                "&radius=3000" +
                "&type=" + "tourist_attraction" +
                "&language=" + "ko" +
                //"&keyword=cruise" +
                "&key=" + API_KEY;
        jsonText = httpConnection(httpURL);

        return jsonText;
    }
    //get json
    public String httpConnection(String targetUrl) {
        URL url = null;
        HttpURLConnection conn = null;
        String jsonData = "";
        BufferedReader br = null;
        StringBuffer sb = null;
        String returnText = "";

        try {
            url = new URL(targetUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            sb = new StringBuffer();

            while ((jsonData = br.readLine()) != null) {
                sb.append(jsonData);
            }
            returnText = sb.toString();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("end of http request !");
        return returnText;
    }

    //handler
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String json = bun.getString("json");
            //json PARSING
            jsonParsing(json);
        }
    };

    //json parsing
    private void jsonParsing(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray placeArray = jsonObject.getJSONArray("results");
            System.out.println("JSON PARSING !");
            for(int i=0; i<placeArray.length(); i++)
            {
                JSONObject placeJsonObject = placeArray.getJSONObject(i);

                JSONObject geometry = (JSONObject) placeJsonObject.get("geometry");
                JSONObject location = (JSONObject) geometry.get("location");
                String lat = location.get("lat").toString();
                String lng = location.get("lng").toString();

                Place placeDTO = new Place();

                //array
                JSONArray photoArray = placeJsonObject.getJSONArray("photos");
                if(photoArray.length() != 0) {
                    JSONObject photos = photoArray.getJSONObject(0);
                    //photo : [ { photo_reference } ]
                    placeDTO.setPhotoReference((String) photos.get("photo_reference"));
                }
                JSONArray typeArray = (JSONArray)placeJsonObject.getJSONArray("types");
                if(typeArray.length() != 0) {
                    ArrayList<String> temp = new ArrayList<>();
                    for(int j=0; j<typeArray.length();++j) {
                        temp.add(typeArray.getString(j));
                    }
                    placeDTO.setType(temp);
                }

                placeDTO.setName(placeJsonObject.get("name").toString());
                placeDTO.setLatitude(Double.parseDouble(lat));
                placeDTO.setLongitude(Double.parseDouble(lng));
                placeDTO.setVicinity(placeJsonObject.get("vicinity").toString());

                placeList.add(placeDTO);    //여기서 리스트에 추가!
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //google api key
    public static String getApiKeyFromManifest(Context context) {
        String apiKey = null;

        try {
            String e = context.getPackageName();
            ApplicationInfo ai = context
                    .getPackageManager()
                    .getApplicationInfo(e, PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if(bundle != null) {
                apiKey = bundle.getString("com.google.android.geo.API_KEY");
            }
        } catch (Exception var6) {
            Log.d(TAG, "Caught non-fatal exception while retrieving apiKey: " + var6);
        }

        return apiKey;
    }
}
