package com.seoullo.seoullotour.Utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.seoullo.seoullotour.Home.HomeActivity;
import com.seoullo.seoullotour.Models.Bookmark;
import com.seoullo.seoullotour.Models.Comment;
import com.seoullo.seoullotour.Models.Like;
import com.seoullo.seoullotour.Models.Place;
import com.seoullo.seoullotour.Profile.ProfileActivity;

import com.google.firebase.database.DatabaseReference;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.Models.Photo;
import com.seoullo.seoullotour.Models.User;
import com.seoullo.seoullotour.Models.UserAccountSettings;
import com.seoullo.seoullotour.Recommend.RecommendActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener {
        void onLoadMoreItems();
    }
    //time var
    private int SEC = 60;
    private int MIN = 60;
    private int HOUR = 24;
    private int DAY = 30;
    private int MONTH = 12;

    public RequestManager mRequestManager;
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private static final String TAG = "MainfeedListAdapter";

    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";

    //location and places
    private String mValue;
    private ArrayList<Photo> photosList = new ArrayList<>();
    private Place mPlace = new Place();

    private ArrayList<Place> placeList = new ArrayList<>();

    public MainfeedListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Photo> objects, RequestManager requestManager) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        mRequestManager = requestManager;
        photosList = objects;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder {
        CircleImageView mprofileImage;
        TextView username, timeDetla, caption, likes, comments, location, likecount;
        LinearLayout likeLayout;
        com.seoullo.seoullotour.Utils.SquareImageView image;
        ImageView heartRed, heartWhite, comment, options;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likeByCurrentUser, bookmarkByCurrentUser;
        Heart heart;
        Mark bookmark;
        GestureDetector detector;
        Photo photo;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {

        final ViewHolder holder;
        if (photosList.size() > position) {
//        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();
            holder.options = (ImageView) convertView.findViewById(R.id.btnShow);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = (com.seoullo.seoullotour.Utils.SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.comment = (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.likeLayout = (LinearLayout) convertView.findViewById(R.id.linLayout_like);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments_link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.timeDetla = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mprofileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = photosList.get(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();
            holder.location = (TextView) convertView.findViewById(R.id.show_location);
            holder.likecount = (TextView) convertView.findViewById(R.id.count_likes);
            holder.bookmark = new Mark( (ImageView) convertView.findViewById(R.id.image_bookmark_white),(ImageView) convertView.findViewById(R.id.image_bookmark_black));
            //            holder.bookmark.bookmarkBlack = (ImageView) convertView.findViewById(R.id.image_bookmark_black);
//            holder.bookmark.bookmarkWhite = (ImageView) convertView.findViewById(R.id.image_bookmark_white);
            //holder.bookmark = new Mark(holder.bookmarkWhite, holder.bookmarkBlack);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //get the current users username (need for checking likes string)
        getCurrentUsername();

        //get likes string
        getLikesString(holder);


        //set the caption
        holder.caption.setText(photosList.get(position).getCaption());
//        holder.username.setText(getItem(position).getUser_id());
        holder.location.setText(photosList.get(position).getLocation());
        //set the comment
        List<Comment> comments = photosList.get(position).getComments();

        holder.comments.setText("댓글 " + comments.size() + "개 모두 보기");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: loading comment thread for " + getItem(position).getPhoto_id());
                ((HomeActivity) mContext).onCommentThreadSelected(getItem(position),
                        mContext.getString(R.string.home_activity));

                //going to need to do something else?
                ((HomeActivity) mContext).hideLayout();

            }
        });

        //옵션 버튼 클릭
        final View finalConvertView = convertView;
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String photoUid = photosList.get(position).getUser_id();
                String myuid = FirebaseAuth.getInstance().getCurrentUser().getUid();


                if(photoUid.equals(myuid)){
                    PopupMenu popupMenu = new PopupMenu(parent.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.hello, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().equals("삭제")) {
                                deletePhoto(photosList.get(position),parent,position);
                            }
                            else if (item.getTitle().equals("신고")) {
                                Toast.makeText(parent.getContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        }
                    });
                }
                else {
                    PopupMenu popupMenu = new PopupMenu(parent.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.othermenu, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().equals("신고")) {
                                Toast.makeText(parent.getContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        }
                    });
                }
            }
        });


        //set the time it was posted
        String timestampDifference = getTimestampDifference(photosList.get(position));
        holder.timeDetla.setText(timestampDifference);


        //set the profile image
//        final ImageLoader imageLoader = ImageLoader.getInstance();
//        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);


        //get the profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(photosList.get(position).getUser_id());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();

                    Log.d(TAG, "onDataChange: found user: "
                            + singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " +
                                    holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));

                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);

                        }
                    });

                    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                    StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://seoullo-4fbc1.appspot.com");
                    storageReference.child("photos").child("users").child(photosList.get(position).getUser_id()).child("profile_photo").getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mRequestManager.load(uri).into(holder.mprofileImage);
//                                    Glide.with(mContext)
////                                            .load(uri)
////                                            .into(holder.mprofileImage);

                                }
                            });
//                    storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(toString(getItem(position).getImage_path()));
//                    storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(getItem(position).getImage_path());
//                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            Glide.with(mContext)
//                                    .load(uri.toString())
//                                    .into(holder.image);
//                        }
//                    });


                    storageReference.child("photos").child("users").child(photosList.get(position).getUser_id()).child(holder.photo.getImage_name())
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mRequestManager.load(uri).into(holder.image);
//                                    Glide.with(mContext)
//                                            .load(uri)
//                                            .into(holder.image);
                                }
                            });
                    //Glide.with(mContext).load(getItem(position).getImage_path()).into(holder.image);


//                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
//                            holder.mprofileImage);
                    holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " +
                                    holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });


                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity) mContext).onCommentThreadSelected(getItem(position),
                                    mContext.getString(R.string.home_activity));

                            //another thing?
                            ((HomeActivity) mContext).hideLayout();
                        }
                    });
                    getBookmarkCurrentUser(holder);
                    holder.photo.setLatlng(photosList.get(position).getLatlng());
                    bookmarkClickEvent(holder);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //get location item
        Query query1 = reference
                .child("photos")
                .child(holder.photo.getPhoto_id())
                .child("location");
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String location = "not recognized";
                String jsonString = dataSnapshot.toString();
                mValue = jsonString.substring(jsonString.indexOf("value =") + 7, jsonString.length() - 1);
                mPlace.setVicinity(mValue);
                holder.location.setText(mValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("location add", "error !!");
            }
        });
        //get location item
        Query latlngQuery = reference
                .child("photos")
                .child(holder.photo.getPhoto_id())
                .child("latlng");
        latlngQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPlace.setLatitude(Double.parseDouble(dataSnapshot.child("0").getValue().toString()));
                mPlace.setLongitude(Double.parseDouble(dataSnapshot.child("1").getValue().toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //get place array
        Query placeQuery = mReference.child("photos").child(holder.photo.getPhoto_id()).child("places");
        placeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //15개 중에 3개를 저장할 예정
                if (dataSnapshot.getValue() != null) {
                    for (int i = 0; i < 3; ++i) {
                        Place place = new Place();
                        place.setPhotoReference(dataSnapshot.child(String.valueOf(i)).child("photoReference").getValue().toString());
                        place.setVicinity(dataSnapshot.child(String.valueOf(i)).child("vicinity").getValue().toString());
                        place.setName(dataSnapshot.child(String.valueOf(i)).child("name").getValue().toString());
                        place.setLatitude(Double.parseDouble(dataSnapshot.child(String.valueOf(i)).child("latitude").getValue().toString()));
                        place.setLongitude(Double.parseDouble(dataSnapshot.child(String.valueOf(i)).child("longitude").getValue().toString()));

                        ArrayList<String> temp = new ArrayList<>();
                        //type
                        for (int j = 0; j < dataSnapshot.child(String.valueOf(i)).child("type").getChildrenCount(); ++j) {
                            temp.add(dataSnapshot.child(String.valueOf(i)).child("type").child(String.valueOf(j)).getValue().toString());
                        }
                        place.setType(temp);
                        placeList.add(place);
                    }
                }
            }
                @Override
                public void onCancelled (@NonNull DatabaseError databaseError){

                }

        });


        //get the user object
        Query userQuery = mReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(photosList.get(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.getValue(User.class).getUsername());

                    holder.user = singleSnapshot.getValue(User.class);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG,"placeList size is : " + placeList.size());
                Intent intent = new Intent(mContext, RecommendActivity.class);
                intent.putExtra("firstPlace", mPlace);
                intent.putExtra("places", (ArrayList<Place>)placeList);
                intent.putExtra("user_id",photosList.get(position).getUser_id());
                intent.putExtra("image_name",holder.photo.getImage_name());
                intent.putExtra("photo_id", holder.photo.getPhoto_id());

                mContext.startActivity(intent);
            }
        });

        if (reachedEndOfList(position)) {
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position) {
        return position == photosList.size() - 1;
    }

    private void loadMoreData() {

        try {
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        } catch (ClassCastException e) {
            Log.e(TAG, "loadMoreData: ClassCastException: " + e.getMessage());
        }

        try {
            mOnLoadMoreItemsListener.onLoadMoreItems();
        } catch (NullPointerException e) {
            Log.e(TAG, "loadMoreData: ClassCastException: " + e.getMessage());
        }
    }

    public void bookmarkClickEvent (final ViewHolder mHolder){
        mHolder.bookmark.bookmarkWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewBookmark(mHolder);
//                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//
//                Query query = reference
//                        .child(mContext.getString(R.string.dbname_bookmarks))
//                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                        .child(mHolder.photo.getPhoto_id());
//
//                query.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                            String keyID = singleSnapshot.getKey();
//                            //case1: Then user already liked the photo
//                            if (mHolder.bookmarkByCurrentUser &&
//                                    singleSnapshot.getValue(Bookmark.class).getUser_id()
//                                            .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                                mReference.child(mContext.getString(R.string.dbname_bookmarks))
//                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                        .child(mHolder.photo.getPhoto_id())
//                                        .removeValue();
//
////                                mReference.child(mContext.getString(R.string.dbname_user_photos))
////                                        .child(mHolder.photo.getUser_id())
////                                        .child(mHolder.photo.getPhoto_id())
////                                        .child(mContext.getString(R.string.field_bookmarks))
////                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
////                                        .removeValue();
//                                mHolder.bookmark.toggleBookmark();
//                            }
//                            //case2: The user has not liked the photo
//                            else if (!mHolder.bookmarkByCurrentUser) {
//                                //add new like
//                                addNewBookmark(mHolder);
//                                break;
//                            }
//                        }
//                        if (!dataSnapshot.exists()) {
//                            //add new like
//                            addNewBookmark(mHolder);
//                        }
//                    }
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
            }
        });
        mHolder.bookmark.bookmarkBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                mReference.child(mContext.getString(R.string.dbname_bookmarks))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mHolder.photo.getPhoto_id())
                        .removeValue();
                mHolder.bookmark.toggleBookmark();
//                Query query = reference
//                        .child(mContext.getString(R.string.dbname_bookmarks))
//                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                        .child(mHolder.photo.getPhoto_id());
//
//                query.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                            String keyID = singleSnapshot.getKey();
//                            //case1: Then user already liked the photo
//                            if (mHolder.bookmarkByCurrentUser &&
//                                    singleSnapshot.getValue(Bookmark.class).getUser_id()
//                                            .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                                mReference.child(mContext.getString(R.string.dbname_bookmarks))
//                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                        .child(mHolder.photo.getPhoto_id())
//                                        .removeValue();
//
////                                mReference.child(mContext.getString(R.string.dbname_user_photos))
////                                        .child(mHolder.photo.getUser_id())
////                                        .child(mHolder.photo.getPhoto_id())
////                                        .child(mContext.getString(R.string.field_bookmarks))
////                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
////                                        .removeValue();
//                                mHolder.bookmark.toggleBookmark();
//                            }
//                            //case2: The user has not liked the photo
//                            else if (!mHolder.bookmarkByCurrentUser) {
//                                //add new like
//                                addNewBookmark(mHolder);
//                                break;
//                            }
//                        }
//                        if (!dataSnapshot.exists()) {
//                            //add new like
//                            addNewBookmark(mHolder);
//                        }
//                    }
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
            }
        });

    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        ViewHolder mHolder;

        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String keyID = singleSnapshot.getKey();
                        //case1: Then user already liked the photo
                        if (mHolder.likeByCurrentUser &&
                                singleSnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                            subtractLike(mHolder, keyID);
//                            break;
                            int likeCount = mHolder.photo.subtractLikeCount();

                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mHolder.photo.setLikeCount(likeCount);
                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes_count))
                                    .setValue(likeCount);

                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(mHolder.photo.getUser_id())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .removeValue();

                            mHolder.heart.toggleLike();

                            getLikesString(mHolder);
                        }
                        //case2: The user has not liked the photo
                        else if (!mHolder.likeByCurrentUser) {
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()) {
                        //add new like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewBookmark(final ViewHolder holder) {
        Log.d(TAG, "addNewBookmark: adding new bookmark");

        String newLikeID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Bookmark bookmark = new Bookmark();
        bookmark.setUser_id(holder.photo.getUser_id());
        bookmark.setPhoto_id(holder.photo.getPhoto_id());
        bookmark.setImage_name(holder.photo.getImage_name());
        bookmark.setLocation(holder.photo.getLocation());
        bookmark.setLatlng(holder.photo.getLatlng());
        bookmark.setLikeCount(holder.photo.getLikeCount());
        bookmark.setCaption(holder.photo.getCaption());

        Log.d(TAG, bookmark.getLocation() + " , loclocloc" + bookmark.getLatlng());

//        mReference.child(mContext.getString(R.string.dbname_photos))
//                .child(holder.photo.getPhoto_id())
        mReference.child(mContext.getString(R.string.dbname_bookmarks))
                .child(newLikeID)
                .child(holder.photo.getPhoto_id())
                .setValue(bookmark);

//        mReference.child(mContext.getString(R.string.dbname_user_photos))
//                .child(holder.photo.getUser_id())
//                .child(holder.photo.getPhoto_id())
//                .child(mContext.getString(R.string.field_bookmarks))
//                .child(newLikeID)
//                .setValue(bookmark);

        holder.bookmark.toggleBookmark();
    }//.removeValue();
    private void deleteBookmarkphoto(final String photoid){
        final ArrayList<String> Userids = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("bookmarks")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Userids.add(singleSnapshot.getKey());
                    }
                    for(int i =0; i < Userids.size() ; i++) {
                        mReference.child("bookmarks")
                                .child(Userids.get(i))
                                .child(photoid)
                                .removeValue();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void deletePhoto(final Photo photo, final ViewGroup parent, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
        builder.setTitle("게시글 삭제");
        builder.setMessage("정말로 삭제하시겠습니까 ?");
        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteBookmarkphoto(photo.getPhoto_id());

                mReference.child(mContext.getString(R.string.dbname_photos))
                        .child(photo.getPhoto_id())
                        .removeValue();
                mReference.child(mContext.getString(R.string.dbname_user_photos))
                        .child(photo.getUser_id())
                        .child(photo.getPhoto_id())
                        .removeValue();

                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://seoullo-4fbc1.appspot.com");
                storageReference.child("photos").child("users").child(photo.getUser_id()).child(photo.getImage_name()).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(parent.getContext(), "게시글이 삭제되었습니다 !", Toast.LENGTH_SHORT).show();
                                photosList.remove(position);
                                notifyDataSetChanged();

                            }
                        });
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {       //취소 버튼을 생성하고 클릭시 동작을 구현합니다.
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alert = builder.create();                                                       //빌더를 이용하여 AlertDialog객체를 생성합니다.
        alert.show();                                                                                    //AlertDialog를 띄웁니



    }

    private void getBookmarkCurrentUser(final ViewHolder holder) {
        Log.d(TAG, "getBookmarkCurrentUser");

        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//            reference.child(mContext.getString(R.string.dbname_photos))
//                    .child(holder.photo.getPhoto_id())
            reference.child(mContext.getString(R.string.dbname_bookmarks))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(holder.photo.getPhoto_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        holder.bookmark.toggleBookmark();
                    }

//                            holder.bookmarkByCurrentUser = true;
//                            holder.bookmark.bookmarkBlack.setEnabled(true);
//                            holder.bookmark.bookmarkWhite.setEnabled(false);


//                    holder.users = new StringBuilder();
//                    if(dataSnapshot.getValue() != null) {
//                        Bookmark books = dataSnapshot.getValue(Bookmark.class);
//                        String currentuid = books.getUser_id();
//                        if (currentuid == null) {
//                        } else {
//                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//                            Query query = reference
//                                    .child(mContext.getString(R.string.dbname_bookmarks))
//                                    .orderByChild(mContext.getString(R.string.field_user_id))
//                                    .equalTo(books.getUser_id());
//                            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                                        Log.d(TAG, "onDataChange: found bookmark: " +
//                                                singleSnapshot.getValue(User.class).getUsername());
////                                        holder.users.append(singleSnapshot.getValue(User.class).getUsername());
////                                        holder.users.append(",");
//                                    }
//
//                                    String[] splitUsers = holder.users.toString().split(",");
//                                    Log.d(TAG, "HOLDER.user: " + holder.users.toString());
//                                    Log.d(TAG, "Currentuser: " + currentUsername);
//
//                                    if (holder.users.toString().contains(currentUsername)) {
//                                        Log.d(TAG, "holder.bookmarkByCurrentUser = true");
//                                        holder.bookmarkByCurrentUser = true;
//                                        holder.bookmark.bookmarkBlack.setEnabled(true);
//                                        holder.bookmark.bookmarkWhite.setEnabled(false);
//                                    } else {
//                                        Log.d(TAG, "holder.bookmarkByCurrentUser = false");
//                                        holder.bookmarkByCurrentUser = false;
//                                        holder.bookmark.bookmarkBlack.setEnabled(false);
//                                        holder.bookmark.bookmarkWhite.setEnabled(true);
//
//
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//                                    holder.bookmarkByCurrentUser = true;
//                                }
//                            });
//                        }
//
//                        if (!dataSnapshot.exists()) {
//                            holder.bookmarkByCurrentUser = false;
//                        } else {
//                        }
//
//
//                        try {
//                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//
//                            }
//                        } catch (NullPointerException e) {
//                            Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
//                        }
//                    }
                    }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    holder.likecount.setText("좋아요 " + "0" + "개");
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "getLikesString: NullPointerException: " + e.getMessage());
//            holder.likesString = "";
            holder.likeByCurrentUser = false;
            //setup likes string
            setupLikesString(holder, null, null);
        }
    }

    private void addNewLike(final ViewHolder holder) {
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        int likeCount = holder.photo.addLikeCount();
        holder.photo.setLikeCount(likeCount);

        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes_count))
                .setValue(likeCount);

        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();

        getLikesString(holder);
    }
    private void getCurrentUsername() {
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting likes string");

        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    holder.likecount.setText("좋아요 " + dataSnapshot.getChildrenCount() + "개");

                    //textview 동적생성
                    final TextView item = new TextView(getContext());
                    final TextView info = new TextView(getContext());

                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: found like: " +
                                            singleSnapshot.getValue(User.class).getUsername());

                                    holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(",");

                                }

                                String[] split = holder.users.toString().split(",");

                                int length = split.length;

                                if (holder.users.toString().contains(currentUsername)) {//mitch, mitchell.tabian
                                    holder.likeByCurrentUser = true;
                                } else {
                                    holder.likeByCurrentUser = false;
                                }

                                if (length > 0) {
                                    holder.likecount.setText("좋아요 " + length + "개");
                                    item.setText(split[length-1]);
                                    item.setTypeface(null, Typeface.BOLD);
                                    info.setText("님 외 "+length+"명이 좋아합니다");
                                }
                                else {

                                    holder.likecount.setText("좋아요 " + "0" + "개");
                                }

                                //setup likes string
                                setupLikesString(holder, item, info);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                holder.likeByCurrentUser = true;
                            }
                        });
                    }

                    if (!dataSnapshot.exists()) {
//                        holder.likesString = "";
                        holder.likeByCurrentUser = false;
                        //setup likes string
                        //holder.likecount.setText("좋아요 " + holder.photo.getLikeCount() + "개");
                        setupLikesString(holder,null, null);
                    } else {

                        //holder.likecount.setText("좋아요 " + dataSnapshot.getChildrenCount() + "개");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    holder.likecount.setText("좋아요 " + "0" + "개");
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "getLikesString: NullPointerException: " + e.getMessage());
//            holder.likesString = "";
            holder.likeByCurrentUser = false;
            //setup likes string
            setupLikesString(holder, null, null);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupLikesString(final ViewHolder holder, TextView item, TextView info) {

        if (holder.likeByCurrentUser) {
            Log.d(TAG, "setupLikesString: photo is liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return holder.detector.onTouchEvent(event);
                }
            });
        } else {
            Log.d(TAG, "setupLikesString: photo is not liked by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likeLayout.removeAllViews();
        if(item != null && info != null) {
            //holder.likes.setText(likesString);
            holder.likeLayout.addView(item);
            holder.likeLayout.addView(info);
        }
    }

    private void setupBookmarksString(final ViewHolder holder, String likesString) {

        if (holder.likeByCurrentUser) {
            Log.d(TAG, "setupLikesString: photo is liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return holder.detector.onTouchEvent(event);
                }
            });
        } else {
            Log.d(TAG, "setupLikesString: photo is not liked by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likes.setText(likesString);
    }

    /**
     * Returns a string representing the number of days ago the post was made
     *
     * @return
     */
//        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");
//
//        String difference = "";
//        Calendar c = Calendar.getInstance();
//        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
//        Date today = c.getTime();
//        sdf.format(today);
//        Date timestamp;
//        final String photoTimestamp = photo.getDate_created();
//        try {
//            timestamp = sdf.parse(photoTimestamp);
//            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
//        } catch (ParseException e) {
//            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage());
//            difference = "0";
//        }
//        return difference;
//    }
    private String getTimestampDifference(Photo photo) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date dateTime = new Date();
        try {
            dateTime = transFormat.parse(photo.getDate_created());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calculateTime(dateTime);
    }

    public String calculateTime(Date date) {

        long curTime = System.currentTimeMillis();
        long regTime = date.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;

        if (diffTime < SEC) {
            // 1분 미만을 모두 "방금전"으로 표기
            msg = "방금전";
        } else if ((diffTime /= SEC) < MIN) {
            // min
            msg = diffTime + "분전";
        } else if ((diffTime /= MIN) < HOUR) {
            // hour
            msg = (diffTime) + "시간전";
        } else if ((diffTime /= HOUR) < DAY) {
            // day
            msg = (diffTime) + "일전";
        } else if ((diffTime /= DAY) < MONTH) {
            // day
            msg = (diffTime) + "달전";
        } else {
            msg = (diffTime) + "년전";
        }

        return msg;
    }


}
