package com.seoullo.seoullotour.Bookmark;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.seoullo.seoullotour.Bookmark.cardviewpager.CardFragmentPagerAdapter;
import com.seoullo.seoullotour.Bookmark.cardviewpager.CardPagerAdapter;
import com.seoullo.seoullotour.Bookmark.cardviewpager.ShadowTransformer;
import com.seoullo.seoullotour.Home.GridFragment;
import com.seoullo.seoullotour.Home.HomeActivity;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.Utils.BottomNavigationViewHelper;

public class BookmarkActivity extends AppCompatActivity {

    private static final String TAG = "BookmarkActivity";
    private static final int ACTIVITY_NUM = 3;

    private Context mContext = BookmarkActivity.this;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //widgets
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;
    private ImageButton mImageButton;
    private ImageView mBookmarkImage;

    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private CardFragmentPagerAdapter mFragmentCardAdapter;
    private ShadowTransformer mFragmentCardShadowTransformer;

    private boolean mShowingFragments = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        Log.d(TAG, "onCreate: bookmark starting.");
        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);
        mImageButton = (ImageButton) findViewById(R.id.add_post);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mBookmarkImage = (ImageView) findViewById(R.id.bookmark_image);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.rel_layout_3, BookmarkFragment.newInstance()).addToBackStack(null).commit();

        setupBottomNavigationView();
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
