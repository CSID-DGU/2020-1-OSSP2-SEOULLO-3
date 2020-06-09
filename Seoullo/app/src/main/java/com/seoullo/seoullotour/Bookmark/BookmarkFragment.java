package com.seoullo.seoullotour.Bookmark;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.seoullo.seoullotour.Home.GridFragment;
import com.seoullo.seoullotour.Home.HomeFragment;
import com.seoullo.seoullotour.Models.Comment;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookmarkFragment extends Fragment {
    private static final String TAG = "BookmarkFragment";
    ScrollView scrollView;
    public RequestManager mRequestManager;


    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
//        scrollView = view.findViewById(R.id.horizontal_scrollView);
//        scrollView.setHorizontalScrollBarEnabled(true);
        mRequestManager = Glide.with(this);
        ArrayList<Photo> bookmark = new ArrayList<>();
        BookmarkRecyclerViewAdapter adapter = new BookmarkRecyclerViewAdapter(bookmark);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bookmarkfragment_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        Log.d(TAG, "bookmark ing");
        return view;
    }

    private class BookmarkRecyclerViewAdapter extends RecyclerView.Adapter<BookmarkRecyclerViewAdapter.ViewHolder> {

        private ArrayList<Photo> mBookmarkList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView nameTextView;
            public Button messageButton;
            public ImageView imageView;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.bookmark_image);
                nameTextView = (TextView) itemView.findViewById(R.id.text2);
                messageButton = (Button) itemView.findViewById(R.id.button);
            }
        }

//        public BookmarkRecyclerViewAdapter(ArrayList<Photo> bookmark) {
        public BookmarkRecyclerViewAdapter(ArrayList<Photo> bookmark) {

        mBookmarkList = new ArrayList<>();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child(getString(R.string.dbname_photos))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mBookmarkList.clear();
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
                                photo.setLikeCount(Integer.parseInt(objectMap.get("likeCount").toString()));
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

                                mBookmarkList.add(photo);
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });
        }

        @Override
        public BookmarkRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View bookmarkView = inflater.inflate(R.layout.recycler_bookmark_item, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(bookmarkView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(final BookmarkRecyclerViewAdapter.ViewHolder viewHolder, final int position) {
            // Get the data model based on position
            Photo photo = mBookmarkList.get(position);




///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference()
                    .child("photos").child("users").child(mBookmarkList.get(position).getUser_id())
                    .child(mBookmarkList.get(position).getImage_name());
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // Glide 이용하여 이미지뷰에 로딩
                        mRequestManager
                                .load(task.getResult())
                                .override(getResources().getDisplayMetrics().widthPixels,getResources().getDisplayMetrics().widthPixels / 3)
                                .into(viewHolder.imageView);

                    } else {
                    }
                }
            });
            // Set item views based on your views and data model
            TextView textView = viewHolder.nameTextView;
            ImageView imageView = viewHolder.imageView;
            Button button = viewHolder.messageButton;
            button.setEnabled(true);
            imageView.setEnabled(true);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.relLayout2, HomeFragment.newInstance(mBookmarkList.get(position),
                            mBookmarkList.get(position).getPhoto_id()));
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }

            });
        }


        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return mBookmarkList.size();
        }


//
//
//
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            int width = getResources().getDisplayMetrics().widthPixels;
//
//            ImageView imageView = new ImageView(parent.getContext());
//            imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, width));
//            imageView.setPadding(1, 1, 1, 1);
//            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//
//            return new CustomViewHolder(imageView);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull BookmarkRecyclerViewAdapter.ViewHolder holder, int position) {
//
//        }
//
//        @Override
//        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
//            final int finalPosition = position;
//
//            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//            StorageReference storageReference = firebaseStorage.getReference()
//                    .child("photos").child("users").child(mBookmarkList.get(position).getUser_id())
//                    .child(mBookmarkList.get(position).getImage_name());
//            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        // Glide 이용하여 이미지뷰에 로딩
//                        mRequestManager
//                                .load(task.getResult())
//                                .override(getResources().getDisplayMetrics().widthPixels,getResources().getDisplayMetrics().widthPixels / 3)
//                                .into(((BookmarkFragment.BookmarkRecyclerViewAdapter.CustomViewHolder) holder).imageView);
//                    } else {
//                    }
//                }
//            });
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                    fragmentTransaction.replace(R.id.relLayout2, HomeFragment.newInstance(mBookmarkList.get(position), mBookmarkList.get(position).getPhoto_id()));
//                    fragmentTransaction.addToBackStack(null);
//                    fragmentTransaction.commit();
//                }
//
//            });
//        }
//


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
    }
    
}