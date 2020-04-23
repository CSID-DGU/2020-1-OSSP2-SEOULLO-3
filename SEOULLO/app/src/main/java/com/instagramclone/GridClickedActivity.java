package com.instagramclone;

///////////////////////////////////안쓰는 파일
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.instagramclone.Models.ContentDTO;
import com.instagramclone.R;
import com.instagramclone.databinding.ItemDetailviewBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.instagramclone.R.layout.item_detailview;

public class GridClickedActivity extends AppCompatActivity {

    ItemDetailviewBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, item_detailview);
        Intent data = getIntent();
        ContentDTO contentDTO = data.getParcelableExtra("object");


        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://tourism-5864e.appspot.com");
        storageReference.child("userProfileImages").child(contentDTO.uid).getDownloadUrl()
                .addOnSuccessListener( new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(GridClickedActivity.this)
                                .load(uri)
                                .apply(new RequestOptions().circleCrop()).into(binding.detailviewitemProfileImage);
                    }
                });

        storageReference.child("images").child(contentDTO.imageName).getDownloadUrl()
                .addOnSuccessListener( new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(GridClickedActivity.this)
                                .load(uri)
                                .into(binding.detailviewitemImageviewContent);
                    }
                });


        binding = DataBindingUtil.setContentView(this, item_detailview);
        binding.detailviewitemProfileTextview.setText(contentDTO.userId);
        binding.detailviewitemExplainTextview.setText(contentDTO.explain);
        //위치 추가됨! - 03/30 20:00
        binding.detailviewitemLocation.setText(contentDTO.location);
        if (contentDTO
                .favorites.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

            binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite);
        } else {

            binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border);
        }

        binding.detailviewitemFavoritecounterTextview.setText("좋아요 " + contentDTO.favoriteCount + "개");

    }
}
