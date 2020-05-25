package com.seoullo.seoullotour.Home;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.seoullo.seoullotour.Models.Comment;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.Models.User;
import com.seoullo.seoullotour.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridFragment extends Fragment {
    private static final String TAG = "GridFragment";
    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mAllUserPosts;
    private ListView mListView;
    private com.seoullo.seoullotour.Utils.MainfeedListAdapter mAdapter;
    private int mResults;
    public RequestManager mRequestManager;

    public static GridFragment newInstance() {
        return new GridFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.gridfragment_recyclerview);
        recyclerView.setAdapter(new GridFragmentRecyclerViewAdatper());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mListView = (ListView) view.findViewById(R.id.listView);
        mAllUserPosts = new ArrayList<>();
        mPhotos = new ArrayList<>();
        mRequestManager = Glide.with(this);

        return view;
    }


    // Recycler View Adapter
    class GridFragmentRecyclerViewAdatper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<Photo> photos;


        public GridFragmentRecyclerViewAdatper() {

            photos = new ArrayList<>();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child(getString(R.string.dbname_photos))
//                    .orderByChild(getString(R.string.field_likes_count))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            Collections.sort(photos, ComparatorByLikeNum);
                            photos.clear();
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                                Photo photo = new Photo();
                                Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                                photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                                photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                                photo.setImage_name(objectMap.get("image_name").toString());
                                photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                                photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                                photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                                photo.setLikeCount(Integer.parseInt( objectMap.get("likeCount").toString()));
//                                photos.add(singleSnapshot.getValue(Photo.class));
                                ArrayList<Comment> comments = new ArrayList<Comment>();
                                for (DataSnapshot dSnapshot : singleSnapshot
                                        .child(getString(R.string.field_comments)).getChildren()) {
                                    Comment comment = new Comment();
                                    comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                    comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                    comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                    comments.add(comment);
                                }
                                photo.setComments(comments);

                                photos.add(photo);

                                Log.d(TAG, "포토사이즈" + photos.size());
                            }
                            displayPhotos();
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
        private void displayPhotos(){
            if (photos != null){
                try{
                    Collections.sort(photos, new Comparator<Photo>() {
                        @Override
                        public int compare(Photo photo1, Photo photo2) {
                            return Integer.valueOf(photo2.getLikeCount()).compareTo(photo1.getLikeCount());
                        }
                    });
                }catch (NullPointerException e){
                    Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
                }catch (IndexOutOfBoundsException e){
                    Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
                }
            }
        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //현재 사이즈 뷰 화면 크기의 가로 크기의 1/3값을 가지고 오기
            int width = getResources().getDisplayMetrics().widthPixels / 3;

            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, width));
            imageView.setPadding(1, 1, 1, 1);
            imageView.setCropToPadding(true);

            return new CustomViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference()
                    .child("photos").child("users").child(photos.get(position).getUser_id()).child(photos.get(position).getImage_name());
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // Glide 이용하여 이미지뷰에 로딩
                        Glide.with(holder.itemView.getContext())
                                .load(task.getResult())
                                .into(((CustomViewHolder) holder).imageView);
                    } else {
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    //transAction animation  ++++
//                    fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left,R.anim.exit_to_right);
                    fragmentTransaction.replace(R.id.relLayout2, HomeFragment.newInstance(photos.get(position), photos.get(position).getPhoto_id()));
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }

            });
//            ((HomeActivity) getActivity()).endOfProgress();
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "포토사이즈" + photos.size());

            return photos.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public ImageView imageView;

            public CustomViewHolder(ImageView imageView) {
                super(imageView);
                this.imageView = imageView;


                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("Recyclerview", "position = " + getAdapterPosition());
                    }
                });
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
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
//        public Comparator<Photo> ComparatorByLikeNum = new Comparator<Photo>() {
//            @Override
//            public int compare(Photo o1, Photo o2) {
//                return getString(o2.getLikeCount()).compareTo(getString(o1.getLikeCount()));
//            }
//        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
 /*   private void getPhotos() {
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.dbname_photos))
//                .child(getString(R.string.field_photo_id))
                .orderByChild(getString(R.string.field_likes_count))
//                .orderByValue()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                            Photo photo = new Photo();
                            Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                            photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                            photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                            photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                            Log.d(TAG, "getPhoto_id" + photo.getPhoto_id());
                            photo.setImage_name(objectMap.get("image_name").toString());
                            photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                            photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                            photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                            ArrayList<Comment> comments = new ArrayList<Comment>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child(getString(R.string.field_comments)).getChildren()) {
                                Comment comment = new Comment();
                                comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                comments.add(comment);
                            }

                            photo.setComments(comments);
                            mPhotos.add(photo);
                            Log.d(TAG, "포토아이디" + photo.getPhoto_id());
                        }
//                            displayPhotos();
                        try {
                            //최신순으로 보여줌.
//                Collections.sort(mPhotos, new Comparator<Photo>() {
//                    @Override
//                    public int compare(Photo o1, Photo o2) {
//                        return o2.getDate_created().compareTo(o1.getDate_created());
//                    }
//                });
                            mResults = 10;
                            mAdapter = new com.seoullo.seoullotour.Utils.MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPhotos, mRequestManager);
                            mListView.setAdapter(mAdapter);

                        } catch (NullPointerException e) {
                            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
                        } catch (IndexOutOfBoundsException e) {
                            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
*/