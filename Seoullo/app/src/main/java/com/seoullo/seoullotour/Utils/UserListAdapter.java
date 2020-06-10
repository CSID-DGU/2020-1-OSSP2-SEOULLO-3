package com.seoullo.seoullotour.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.seoullo.seoullotour.Models.User;
import com.seoullo.seoullotour.Models.UserAccountSettings;
import com.seoullo.seoullotour.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends ArrayAdapter<User>{

    private static final String TAG = "UserListAdapter";

    public RequestManager mRequestManager;
    private LayoutInflater mInflater;
    private List<User> mUsers = null;
    private int layoutResource;
    private Context mContext;

    public UserListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<User> objects,RequestManager requestManager) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        mRequestManager = requestManager;
        this.mUsers = objects;
    }

    private static class ViewHolder{
        TextView username, email;
        CircleImageView profileImage;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }


        holder.username.setText(getItem(position).getUsername());
        holder.email.setText(getItem(position).getEmail());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.getValue(UserAccountSettings.class).toString());
                    //Glide 프로필 사진 로드
                    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                    StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://seoullo-4fbc1.appspot.com");
                    storageReference.child("photos").child("users").child(getItem(position).getUser_id()).child("profile_photo").getDownloadUrl()
                            .addOnSuccessListener( new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mRequestManager.load(uri).into(holder.profileImage);
//                        Glide.with(getActivity())
//                                .load(uri)
//                                .into(mProfilePhoto);
                                }
                            });


                    //이전이미지로드
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return convertView;
    }
}
