package com.seoullo.seoullotour.Recommend;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.ArrayList;

public class RecommendFirstFragment extends Fragment {

    private static final String TAG = "RecommendFirstFragment";

    //widget
    private TextView mTitle;
    private TextView mVicinity;
    private ImageView mImage;
    private TextView mDesc;
    private TextView mAnotherDecs;
    private LinearLayout mScrollItems;
    private LinearLayout mViewpager;

    //var
    private Place mPlace;
    private String API_KEY;

    //init - google place api
    RecommendFirstFragment(Place ref, LinearLayout ll) {
        this.mPlace = ref;
        this.mViewpager = ll;
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
        mScrollItems = (LinearLayout) view.findViewById(R.id.scroll_type_item);

        mTitle.setText(mPlace.getName());
        mVicinity.setText(mPlace.getVicinity());
        ArrayList<String> mType = new ArrayList<>();
        mType = (ArrayList<String>) mPlace.getType().clone();

        for(int i=0; i< mType.size(); ++i) {
            TextView item = new TextView(this.getContext());

            switch(mType.get(i)) {
                case "tourist_attraction":
                    item.setText(Html.fromHtml("#관광지추천장소TOP5"));
                    break;
                case "point_of_interest":
                    item.setText(Html.fromHtml("#관심지역TOP10"));
                    break;                case "establishment":
                    item.setText(Html.fromHtml("#설립107주년"));
                    break;
            }

            item.setTextSize(10);
            item.setMovementMethod(new ScrollingMovementMethod());
            item.setPadding(20,0,20,0);
            mScrollItems.addView(item);
        }

        String desc = "This place is located at latitude : " + mPlace.getLatitude() + " and longitude : " + mPlace.getLongitude();
        mDesc.setText(desc);
        mAnotherDecs.setText("this is another part of desc");
        //Image
        final String targetUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                "maxwidth=" + 180 +
                "&photoreference=" + mPlace.getPhotoReference() +
                "&key=" + API_KEY;
        final Bitmap[] bitmap = new Bitmap[1];

        //반드시 메인스레드가 아닌 별도의 스레드를 생성해야함 : NextActivity와 같이
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


        Button mSlideUpBtn = (Button)view.findViewById(R.id.slideup_btn);

        mSlideUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }
}
