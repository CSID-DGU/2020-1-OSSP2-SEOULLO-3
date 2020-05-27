package com.seoullo.seoullotour.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.Profile.ProfileFragment;
import com.seoullo.seoullotour.R;

import java.util.ArrayList;

public class GridImageAdapter extends ArrayAdapter<String>  {

    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private String mAppend;
    private String uid;
    private int gridFlag;
    private int photosize;
    private ArrayList<String> imgURLs;
    private ArrayList<Photo> photos = new ArrayList<>();
    public RequestManager mRequestManager;
    private  ProfileFragment profileFragment;
    private int loadSize;


    public GridImageAdapter(Context context, int layoutResource, String append, ArrayList<String> imgURLs , String uid, int flag) {
        super(context, layoutResource, imgURLs);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        this.gridFlag = flag;
        this.uid = uid;
        this.layoutResource = layoutResource;
        mAppend = append;
        this.imgURLs = imgURLs;
    }
    public GridImageAdapter(Context context, int layoutResource, String append, ArrayList<String> imgURLs , String uid, int flag,
                            ArrayList<Photo> photos , RequestManager requestManager,ProfileFragment profileFragment) {
        super(context, layoutResource, imgURLs);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRequestManager =requestManager;
        this.profileFragment = profileFragment;
        mContext = context;
        this.gridFlag = flag;
        this.uid = uid;
        this.layoutResource = layoutResource;
        mAppend = append;
        this.imgURLs = imgURLs;
        this.photos = photos;
        loadSize = photos.size();
    }


    /**
     * SquareImageView change image propositions (to a square)
     * Progressbar is shown when images is loading
     */
    private static class ViewHolder {
        com.seoullo.seoullotour.Utils.SquareImageView image;
        //ProgressBar mProgressBar;
    }

    /**
     * Viewholder build pattern (Similar to recyclerview)
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        int holderIndex = photos.size()-1-position;
        if(convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            //holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.gridImageProgressbar);
            holder.image = (com.seoullo.seoullotour.Utils.SquareImageView) convertView.findViewById(R.id.gridImageView);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        String imgURL = getItem(position);
       // String photo_name = "photo" + Integer.toString(position + 1);

        if(gridFlag == 1) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://seoullo-4fbc1.appspot.com");
            //if(holderIndex >= 0) {
                storageReference.child("photos").child("users").child(uid).child(photos.get(position).getImage_name()).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                System.out.println(loadSize+"::loadSize");
                                //loadCount++;
                                mRequestManager.load(uri)
                                        .listener(
                                new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        //holder.mProgressBar.setVisibility(View.GONE);

                                        loadSize--;
                                        if(loadSize == 0){
                                            System.out.println("progress실험중");
                                            profileFragment.goneProgress();
                                        }
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        //holder.mProgressBar.setVisibility(View.GONE);

                                        loadSize--;
                                        if(loadSize == 0){
                                            System.out.println("progress실험중");
                                            profileFragment.goneProgress();
                                        }
                                        return false;
                                    }
                                })


                                        .into(holder.image);
                               // holder.mProgressBar.setVisibility(View.GONE);
//                                Glide.with(mContext)
//                                        .load(uri)
//                                        .into(holder.image);
//
                            }
                        });
           // }
        }
        else if(gridFlag == 2) {

            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(mAppend + imgURL, holder.image, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
//                    if (holder.mProgressBar != null) {
//                        //holder.mProgressBar.setVisibility(View.VISIBLE);
//                    }
                    loadSize++;
//                    }

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                    if (holder.mProgressBar != null) {
//                        holder.mProgressBar.setVisibility(View.GONE);
//                    }

                    loadSize--;
                    if(loadSize == 0){
                        System.out.println("progress실험중");
                        //profileFragment.goneProgress();
                    }

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    if (holder.mProgressBar != null) {
//                        holder.mProgressBar.setVisibility(View.GONE);
//                    }

                    loadSize--;
                    if(loadSize == 0){
                        System.out.println("progress실험중");
                       // profileFragment.goneProgress();
                    }

                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
//                    if (holder.mProgressBar != null) {
//                        holder.mProgressBar.setVisibility(View.GONE);
//                    }

                    loadSize--;
                    if(loadSize == 0){
                        System.out.println("progress실험중");
                       // profileFragment.goneProgress();
                    }
                }
            });
        }



        return convertView;
    }


}
