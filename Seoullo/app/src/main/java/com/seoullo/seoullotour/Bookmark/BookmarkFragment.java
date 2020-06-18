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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.seoullo.seoullotour.Home.GridFragment;
import com.seoullo.seoullotour.Home.HomeFragment;
import com.seoullo.seoullotour.Models.Bookmark;
import com.seoullo.seoullotour.Models.Comment;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookmarkFragment extends Fragment {
    private static final String TAG = "BookmarkFragment";

    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mAllUserPosts;
    private ListView mListView;
    private Button addBookmarkButton;
    private com.seoullo.seoullotour.Utils.MainfeedListAdapter mAdapter;
    private int mResults;
    public RequestManager mRequestManager;

    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);

        mRequestManager = Glide.with(this);
        ArrayList<Photo> bookmark = new ArrayList<>();
        addBookmarkButton = (Button) view.findViewById(R.id.add_bookmark_button);
        BookmarkRecyclerViewAdapter adapter = new BookmarkRecyclerViewAdapter(bookmark);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bookmarkfragment_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        mListView = (ListView) view.findViewById(R.id.listView);
        mAllUserPosts = new ArrayList<>();
        mPhotos = new ArrayList<>();
        mRequestManager = Glide.with(this);
        buttonEvent();
        return view;
    }

    public void buttonEvent(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final String[] photo_id = new String[1];
        reference.child(getString(R.string.dbname_photos))
                .child(getString(R.string.field_photo_id))
                .orderByChild(getString(R.string.field_likes_count))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                Photo photo = new Photo();
                                Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                photo_id[0] = objectMap.get(getString(R.string.field_photo_id)).toString();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        addBookmarkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.rel_layout_3, HomeFragment.newInstance(photo_id[0]));
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    private class BookmarkRecyclerViewAdapter extends RecyclerView.Adapter<BookmarkRecyclerViewAdapter.ViewHolder> {
        private ArrayList<Bookmark> mBookmarkList;
        private ArrayList<Photo> mBookmarkPhotos;

        public class ViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public ImageView imageView;
            public TextView locationTextView;
            public TextView postTextView;
            public TextView countLikeTextView;
            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.post_image);
                postTextView = (TextView) itemView.findViewById(R.id.post_text);
                locationTextView = (TextView) itemView.findViewById(R.id.show_location);
                countLikeTextView = (TextView) itemView.findViewById(R.id.count_likes);
            }
        }

        //        public BookmarkRecyclerViewAdapter(ArrayList<Photo> bookmark) {
        public BookmarkRecyclerViewAdapter(ArrayList<Photo> bookmark) {

            mBookmarkList = new ArrayList<>();

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child(getString(R.string.dbname_bookmarks))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                    .orderByChild(getString(R.string.field_photo_id))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mBookmarkList.clear();
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                Bookmark bookmark = new Bookmark();
                                Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                bookmark.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                                bookmark.setImage_name(objectMap.get("image_name").toString());
                                bookmark.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                                bookmark.setLocation(objectMap.get("location").toString());
                                bookmark.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                                bookmark.setLikeCount(Integer.parseInt(objectMap.get("likeCount").toString()));
                                bookmark.setLatlng((ArrayList<Double>) objectMap.get("latlng"));
                                mBookmarkList.add(bookmark);
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
            View bookmarkView = inflater.inflate(R.layout.layout_bookmark_list_item, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(bookmarkView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(final BookmarkRecyclerViewAdapter.ViewHolder viewHolder, final int position) {
            // Get the data model based on position
            TextView postTextView = viewHolder.postTextView;
            TextView locationTextView = viewHolder.locationTextView;
            TextView countLikeTextView = viewHolder.countLikeTextView;

            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference()
//                    .child(getString(R.string.dbname_bookmarks))
//                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("photos").child("users")
                    .child(mBookmarkList.get(position).getUser_id())
                    .child(mBookmarkList.get(position).getImage_name());
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mRequestManager
                                .load(uri)
                                .into(viewHolder.imageView);
                        }
                    });

            // Set item views based on your views and data model
            ImageView imageView = viewHolder.imageView;
            imageView.setEnabled(true);
            locationTextView.setText(mBookmarkList.get(position).getLocation());
            countLikeTextView.setText("Likes " + mBookmarkList.get(position).getLikeCount());
            postTextView.setText(mBookmarkList.get(position).getCaption());
            postTextView.setEnabled(true);
            locationTextView.setEnabled(true);
            countLikeTextView.setEnabled(true);

            //TODO: 클릭하면 게시물로 이동하도록 해야 함
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.rel_layout_3, HomeFragment.newInstance(mBookmarkList.get(position),
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