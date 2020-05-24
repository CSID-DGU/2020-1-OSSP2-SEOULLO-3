package com.seoullo.seoullotour.Recommend;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.seoullo.seoullotour.Models.Place;
import com.seoullo.seoullotour.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RecommendSecondFragment extends Fragment {

    private static final String TAG = "RecommendFirstFragment";

    //widget
    private TextView mTitle;
    private TextView mVicinity;
    private ImageView mImage;
    private TextView mDesc;
    private TextView mAnotherDecs;

    //var
    private Place mPlace;
    private String API_KEY;

    //init - google place api
    RecommendSecondFragment(Place ref) {
        this.mPlace = ref;
    }

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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view;

        API_KEY = getApiKeyFromManifest(this.getContext());

        view = inflater.inflate(R.layout.layout_recommend_first, container, false);
        mTitle = (TextView) view.findViewById(R.id.recommend_title);
        mVicinity = (TextView) view.findViewById(R.id.recommend_vicinity);
        mImage = (ImageView) view.findViewById(R.id.recommend_image);
        mDesc = (TextView) view.findViewById(R.id.recommend_desc);
        mAnotherDecs = (TextView) view.findViewById(R.id.recommend_anotherdesc);

        mTitle.setText(mPlace.getName());
        mVicinity.setText(mPlace.getVicinity());
        String desc = "This place is located at latitude : " + mPlace.getLatitude() + " and longitude : " + mPlace.getLongitude();
        mDesc.setText(desc);
        mAnotherDecs.setText("this is another part of desc");
        //Image
        final String targetUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                "maxwidth=" + 180 +
                "maxheight=" + 180 +
                "&photoreference=" + mPlace.getPhotoReference() +
                "&key=" + API_KEY;
        final Bitmap[] bitmap = new Bitmap[1];

        //반드시 메인스레드가 아닌 별도의 스레드를 생성해야함 : GridFragement와 같이
        Thread getImage = new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(targetUrl);

                    //서버로부터 요청을해서 비트맵으로 전환시킬꺼
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);  //get response
                    conn.connect();

                    InputStream input = conn.getInputStream();
                    bitmap[0] = BitmapFactory.decodeStream(input);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        getImage.start();
        try {
            // 메인thread는 다른 작업이 끝날때 까지 기다려야한다 반드시!
            getImage.join();
            mImage.setImageBitmap(bitmap[0]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return view;
    }
}
