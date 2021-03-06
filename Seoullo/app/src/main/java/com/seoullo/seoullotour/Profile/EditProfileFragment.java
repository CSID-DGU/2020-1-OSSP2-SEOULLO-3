package com.seoullo.seoullotour.Profile;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
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
import com.seoullo.seoullotour.Models.UserSettings;
import com.seoullo.seoullotour.R;
import com.seoullo.seoullotour.Share.ShareActivity;
import com.seoullo.seoullotour.Utils.FirebaseMethods;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    //Edit Profile Fragment Widgets
    private EditText mDisplayName,mDescription;
    private TextView mChangeProfilePhoto, mUsername;
    private CircleImageView mProfilePhoto;

    // vars
    private UserSettings mUserSettings;
    // glide
    private RequestManager mRequestManager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.username);
        mDescription = (EditText) view.findViewById(R.id.description);

        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods = new FirebaseMethods(getActivity());

        mRequestManager = Glide.with(this);
        //setProfileImage();
        setupFirebaseAuth();

        //back arrow for navigating back to "ProfileActivity"
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                getActivity().finish();
            }
        });

        // green checkmark icon to update user settings information
        TextView checkmark =(TextView) view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes.");
                saveProfileSettings();
                getActivity().finish();
            }
        });

        return view;
    }

    /**
     * Retrieves the data from widgets and submits it to database
     * Checks that username is unique
     */
    private void saveProfileSettings() {
        final String displayName = mDisplayName.getText().toString();
        final String description = mDescription.getText().toString();


//        // case1: if user changed to same name.
//        if (!mUserSettings.getUser().getUsername().equals(username)) {
//            checkIfUsernameExists(username);
//        }
//        // case2: if user change their email
//        if (!mUserSettings.getUser().getEmail().equals(email)) {
//            //step1 Re-Auth
//            //      - Confirm password and email
//
//            //주석처리 5.17
////            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
//////            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
//////            dialog.setTargetFragment(EditProfileFragment.this, 1);
//            //step2 Check if email already exists
//
//
//            //      - 'fetchProvidersForEmail(String email)
//            //step3 change email
//            //      - submit new email to database and authentication
//
//        }


        /**
         * Change fields in Settings that doesn't require unique values
         */
        if (!mUserSettings.getSettings().getDisplay_name().equals(displayName)) {
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null ,0);
        }

        if (!mUserSettings.getSettings().getDescription().equals(description)) {
            mFirebaseMethods.updateUserAccountSettings(null, null, description ,0);
        }

    }

    /**
     * Check if @param username already exists in database
     * @param username
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if " + username + " already exists");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "saved username.", Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH" + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "That username already exists.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void setProfileWidgets(UserSettings userSettings) {
        mUserSettings = userSettings;

        User user = userSettings.getUser();
        String userid = user.getUser_id();

        UserAccountSettings settings = userSettings.getSettings();

        if(userid != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://seoullo-4fbc1.appspot.com");
            //프로필이미지가 없는 사용자 처리해줘야함 ! ! ! 2020/05/16
            storageReference.child("photos").child("users").child(userid).child("profile_photo").getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mRequestManager.load(uri).into(mProfilePhoto);
//                        Glide.with(getActivity())
//                                .load(uri)
//                                .into(mProfilePhoto);
                        }
                    });
            //  UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

            mDisplayName.setText(settings.getDisplay_name());
            mUsername.setText(settings.getUsername());
            mDescription.setText(settings.getDescription());

            mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: changing profile photo");
                    Intent intent = new Intent(getActivity(), ShareActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //268435456
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }
            });
            mProfilePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: changing profile photo");
                    Intent intent = new Intent(getActivity(), ShareActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //268435456
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }
            });
        }
    }

    /*
    ------------------------------------ Firebase ---------------------------------------------
       */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        myRef.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //retrieve user information from the database

                UserSettings userset = mFirebaseMethods.getUserSettings(dataSnapshot);
                System.out.println(userset + "datachange userset ::");
                setProfileWidgets(userset);
                //retrieve images for the user in question
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}