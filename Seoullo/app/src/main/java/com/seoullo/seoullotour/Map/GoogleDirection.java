package com.seoullo.seoullotour.Map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.naver.maps.geometry.LatLng;
import com.seoullo.seoullotour.Models.Point;
import com.seoullo.seoullotour.Models.Route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class GoogleDirection {

    Point currentPoint, mPoint;
    Route mRoute;
    List<LatLng> mPathList;

    private String GOOGLE_API_KEY;
    private Context mContext;

    GoogleDirection(Point ref1, Point ref2, Route ref3, List<LatLng> ref4, Context ref5){
        this.currentPoint = ref1;
        this.mPoint = ref2;
        this.mRoute = ref3;
        this.mPathList = ref4;
        this.mContext = ref5;
    }

    //currentPoint = origin , mPoint = destination
    void HttpConnection(String mode) throws IOException, CloneNotSupportedException, JSONException {

        GOOGLE_API_KEY = getApiKeyFromManifestGoogleAPI(mContext);

        mode = "transit";

        String result = null;
        //Google Http
        String mURL = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" +  this.currentPoint.x   + "," +    this.currentPoint.y +
                "&destination=" + mPoint.x + "," + mPoint.y +
                "&mode=" + mode +
                "&transit_mdoe=subway" +
                "&language=ko" +
                "&key=" + GOOGLE_API_KEY;
                ;
        // Open the connection
        URL url = new URL(mURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
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

        JSONArray routeArray = jsonObject.getJSONArray("routes");

        /*
        *   TODO : JSON parsing
        *      "routes" : [                       --> JSONArray
        *                   {
        *                       "legs" : [         --> JSONArray
        *                                   {
        *                                       "arrivial_time" : { "text":"오전 11:16", "value":456723 },    -->JSONObject
        *                                       "departure_time": { "text":"오전 11:40", "value":678967 },
        *                                       "distance" : { "text":"5.0km", "value":5066 },
        *                                       "end_address" : "대한민국 서울특별시 ~ ",
        *                                       "start_address" : "대한민국 서울특뱔시 ~",
        *                                       "steps" : [             --> JSONArray
        *                                                   {
        *                                                       "distance" : { "text" : "0.1km", "value" : 134 },    --> JSONObject
        *                                                       "duaration" : { "text" : "2분", "value" : 134 },
        *                                                       "end_location" : { "lat" : 37.65432, "lng" : 127.6543 },
        *                                                       "html_instructions" : "안암오거리까지 도보",
        *                                                       "start_location" : { "lat" : 37.23456, "lng" : 127.234567 },
        *                                                       "steps" : [             --> JSONArray
        *                                                                   { ...
        *                                                                       "travel_mode" : "WALKING"
        *                                                                   }
        *                                                                 ],
        *                                                       "travel_mode" : "WALKING"
        *                                                   },
        *                                                   {
        *                                                       "distance" : { "text" : "4.4 km", "value" : 4396 },
        *                                                                   ...
        *                                                   }   steps가 계속 반복되는데 같은 수단일 경우 하위 객체로 들어감
        *                                   } ],
        *       >> 이 정도 parsing 작업 해주면 될 거 같다.
        *
        */

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
            Log.d("GoogleDirection", "Caught non-fatal exception while retrieving apiKey: " + var6);
        }

        return apiKey;
    }
}
