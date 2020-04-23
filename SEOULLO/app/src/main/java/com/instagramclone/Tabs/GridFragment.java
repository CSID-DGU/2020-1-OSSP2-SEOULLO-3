package com.instagramclone.Tabs;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.instagramclone.MainActivity;
import com.instagramclone.Models.ContentDTO;
import com.instagramclone.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class GridFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.gridfragment_recyclerview);
        recyclerView.setAdapter(new GridFragmentRecyclerViewAdatper());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        return view;
    }



    // Recycler View Adapter
    class GridFragmentRecyclerViewAdatper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<ContentDTO> contentDTOs;


        public GridFragmentRecyclerViewAdatper() {

            contentDTOs = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("images").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    contentDTOs.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        contentDTOs.add(snapshot.getValue(ContentDTO.class));
                    }

                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //현재 사이즈 뷰 화면 크기의 가로 크기의 1/3값을 가지고 오기
            int width = getResources().getDisplayMetrics().widthPixels / 3;

            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, width));
            imageView.setPadding(1,1,1,1);

            return new CustomViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://tourism-5864e.appspot.com");
            storageReference.child("images").child(contentDTOs.get(position).imageName).getDownloadUrl()
                    .addOnSuccessListener( new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    Glide.with(holder.itemView.getContext())
                            .load(uri)
                            .apply(new RequestOptions().centerCrop())
                            .into(((CustomViewHolder) holder).imageView);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    });
            ((MainActivity) getActivity()).endOfProgress();
//            Glide.with(holder.itemView.getContext())
//                    .load(contentDTOs.get(position).imageUrl)
//                    .apply(new RequestOptions().centerCrop())
//                    .into(((CustomViewHolder) holder).imageView);
        }

        @Override
        public int getItemCount() {
            return contentDTOs.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public ImageView imageView;

            public CustomViewHolder(ImageView imageView) {
                super(imageView);
                this.imageView = imageView;


                imageView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Log.d("Recyclerview", "position = "+ getAdapterPosition());
                    }
                });
                imageView.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        Log.d("Recyclerview", "position = " + getAdapterPosition());
                        return false;
                    }
                });

            }

            @Override
            public void onClick(View v) {

            }

        }
    }
}