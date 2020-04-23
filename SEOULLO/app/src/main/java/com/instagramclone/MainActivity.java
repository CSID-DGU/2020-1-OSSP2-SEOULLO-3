package com.instagramclone;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.loader.content.CursorLoader;

import com.instagramclone.Tabs.AddPhotoActivity;
import com.instagramclone.Tabs.BookmarkFragment;
import com.instagramclone.Tabs.GridFragment;
import com.instagramclone.Tabs.MapFragment;
import com.instagramclone.Tabs.UserFragment;
import com.instagramclone.R;
import com.instagramclone.Utils.BottomNavigationViewHelper;
import com.instagramclone.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.instagramclone.Utils.StatusCode.FRAGMENT_ARG;
import static com.instagramclone.Utils.StatusCode.PICK_IMAGE_FROM_ALBUM;
import static com.instagramclone.Utils.StatusCode.PICK_PROFILE_FROM_ALBUM;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG ="MAINACTIVITY";
    private static final int ACTIVITY_NUM = 0;
    // Data Binding
    private ActivityMainBinding binding;
    //fragment change
    private int checkFragment = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//FULLSCREEN-MODE
//        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
//        int newUiOptions = uiOptions;
//
//        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
//        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Bottom Navigation View
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
        binding.bottomNavigation.setSelectedItemId(R.id.action_home);
    }

    public void replaceFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.main_content, fragment).addToBackStack(null).commit();
    }
    public void addFragment(Fragment fragment){
        getFragmentManager().beginTransaction().add(R.id.main_content, fragment).addToBackStack(null).commit();
    }

    public void endOfProgress() {
        binding.progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_home:

//               Fragment gridFragment = new GridFragment();
//
//                Bundle bundle_1 = new Bundle();
//                bundle_1.putInt(FRAGMENT_ARG, 1);
//
//                gridFragment.setArguments(bundle_1);
//
//                getFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.main_content, gridFragment)
//                        .commit();
                androidx.fragment.app.Fragment gridFragment = new GridFragment();
                Bundle bundle_1 = new Bundle();
                bundle_1.putInt(FRAGMENT_ARG, 1);

                gridFragment.setArguments(bundle_1);

                //백스택 지우기
                getSupportFragmentManager()
                        .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, gridFragment)
                        .commit();

                return true;

            case R.id.action_bookmark:

                setToolbarDefault();

                androidx.fragment.app.Fragment bookmarkFragment = new BookmarkFragment();

                String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Bundle bundle_2 = new Bundle();
                bundle_2.putString("loginUid", userid);
                bundle_2.putInt(FRAGMENT_ARG, 2);

                bookmarkFragment.setArguments(bundle_2);
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.main_content, bookmarkFragment)
//                        .commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, bookmarkFragment)
                        .commit();

                return true;

            case R.id.action_map:


                if(checkFragment != 0) {

                }
                setToolbarDefault();

                androidx.fragment.app.Fragment mapFragment = new MapFragment();

                Bundle bundle_3 = new Bundle();
                bundle_3.putInt(FRAGMENT_ARG,3);

                mapFragment.setArguments(bundle_3);
//                getFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.main_content, mapFragment)
//                        .commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, mapFragment)
                        .commit();

                return true;

            case R.id.action_account:

                setToolbarDefault();

                androidx.fragment.app.Fragment userFragment = new UserFragment();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Bundle bundle_4 = new Bundle();
                bundle_4.putString("destinationUid", uid);
                bundle_4.putInt(FRAGMENT_ARG, 4);

                userFragment.setArguments(bundle_4);

//                getFragmentManager().beginTransaction()
//                        .replace(R.id.main_content, userFragment)
//                        .commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, userFragment)
                        .addToBackStack(null)
                        .commit();

                return true;
        }

        return false;
    }
    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getApplicationContext(), this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    public void setToolbarDefault() {

        binding.toolbarTitleImage.setVisibility(View.VISIBLE);
        binding.toolbarBtnBack.setVisibility(View.GONE);
        binding.toolbarUsername.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 앨범에서 Profile Image 사진 선택시 호출 되는 부분분
        if (requestCode == PICK_PROFILE_FROM_ALBUM && resultCode == RESULT_OK) {

            String[] proj = {MediaStore.Images.Media.DATA};
            CursorLoader cursorLoader = new CursorLoader(this, data.getData(), proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            //이미지 경로
            String photoPath = cursor.getString(column_index);

            //유저 Uid
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //파일 업로드
            File f = new File(photoPath);
            FirebaseStorage
                    .getInstance()
                    .getReference()
                    .child("userProfileImages")
                    .child(uid)
                    .putFile(Uri.fromFile(f))
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            @SuppressWarnings("VisibleForTests")
                            String url = task.getResult().getStorage().getDownloadUrl().toString();
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put(uid, url);
                            FirebaseDatabase.getInstance().getReference().child("profileImages").updateChildren(map);
                        }
                    });
        } else if (requestCode == PICK_IMAGE_FROM_ALBUM && resultCode == RESULT_OK) {

            binding.bottomNavigation.setSelectedItemId(R.id.action_account);
        }
    }

    public ActivityMainBinding getBinding() {

        return binding;
    }

    @Override
    public void onBackPressed() {

        //안드로이드 X
        androidx.fragment.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_content);
        int fragmentNum = fragment.getArguments().getInt(FRAGMENT_ARG, 0);

        // TODO : Refactoring 필요자
        // 03/30 그리드 > 상세보기 > 탭이동 > 뒤로가기 - 그리드가 다른 탭에서 나오는 걸 방지
        if (fragmentNum == 2 ||fragmentNum == 3 || fragmentNum == 4) {
            if(checkFragment != 0) {
                //화면이 남아있는걸로 하고싶을때 고치
            }
           // binding.bottomNavigation.setSelectedItemId(R.id.action_home);
        } else {
            super.onBackPressed();
            onResume();
        }
    }

    public void onClick_add_review(View view) {

        startActivity(new Intent(MainActivity.this, AddPhotoActivity.class));
    }

}
