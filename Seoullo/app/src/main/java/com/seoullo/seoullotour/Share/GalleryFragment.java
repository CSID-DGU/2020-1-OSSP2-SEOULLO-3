package com.seoullo.seoullotour.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.seoullo.seoullotour.Home.HomeActivity;
import com.seoullo.seoullotour.Profile.AccountSettingsActivity;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.Utils.FilePaths;
import com.seoullo.seoullotour.Utils.FileSearch;
import com.seoullo.seoullotour.Utils.GridImageAdapter;
import com.seoullo.seoullotour.Share.ShareActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";


    //constants
    private static final int NUM_GRID_COLUMNS = 3;

    //widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;
    private TextView empty;
    private TextView nextScreen;

    //vars
    private String imageName;
    private ArrayList<String> directories;
    private String mAppend = "file:/";
    private String mSelectedImage;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        galleryImage = (ImageView) view.findViewById(R.id.galleryImageView);
        gridView = (GridView) view.findViewById(R.id.gridView);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        empty = (TextView) view.findViewById(android.R.id.empty);

        directories = new ArrayList<>();
        Log.d(TAG, "onCreateView: started.");

        ImageView shareClose = (ImageView) view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the gallery fragment.");
                getActivity().finish();
            }
        });


        nextScreen = (TextView) view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen.");

                if (isRootTask()) {
                    Intent intent = new Intent(getActivity(), com.seoullo.seoullotour.Share.NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    intent.putExtra("image_name",imageName);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    intent.putExtra("image_name",imageName);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }

            }
        });

        init();
        return view;
    }

    private boolean isRootTask() {
        if (((com.seoullo.seoullotour.Share.ShareActivity) getActivity()).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void init() {
        FilePaths filePaths = new FilePaths();

        //check for other folders indide "/storage/emulated/0/pictures"
        if (FileSearch.getDirectoryPaths(filePaths.ROOT_DIR) != null) {
            directories = FileSearch.getDirectoryPaths(filePaths.ROOT_DIR);
            directories.add(filePaths.DOWNLOAD);
//            directories.add(filePaths.PICTURES);
        }

        ArrayList<String> directoryNames = new ArrayList<>();
        for (int i = 0; i < directories.size(); i++) {
            Log.d(TAG, "init: directory: " + directories.get(i));
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index);
            directoryNames.add(string);

            if (directoryNames.isEmpty()) {
                gridView.setEmptyView(empty);
                nextScreen.setVisibility(View.INVISIBLE);
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, directoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                directorySpinner.setAdapter(adapter);

                directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(TAG, "onItemClick: selected: " + directories.get(position));

                        //setup our image grid for the directory chosen
                        setupGridView(directories.get(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }

                });
            }
        }

    }


    private void setupGridView(String selectedDirectory) {
        Log.d(TAG, "setupGridView: directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);


        //set the grid column width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        //use the grid adapter to adapter the images to gridview
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, mAppend, imgURLs , FirebaseAuth.getInstance().getCurrentUser().getUid());
        gridView.setAdapter(adapter);

        //set the first image to be displayed when the activity fragment view is inflated
        try {

            setImage(imgURLs.get(0), galleryImage, mAppend);
            mSelectedImage = imgURLs.get(0);
            imageName = imgURLs.get(0).substring(imgURLs.get(0).lastIndexOf("/")+1);


        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "setupGridView: ArrayIndexOutOfBoundsException: " + e.getMessage());
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected an image: " + imgURLs.get(position));

                setImage(imgURLs.get(position), galleryImage, mAppend);
                mSelectedImage = imgURLs.get(position);
                imageName = imgURLs.get(position).substring(imgURLs.get(position).lastIndexOf("/")+1);
            }
        });

    }


    private void setImage(String imgURL, ImageView image, String append) {
        Log.d(TAG, "setImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
