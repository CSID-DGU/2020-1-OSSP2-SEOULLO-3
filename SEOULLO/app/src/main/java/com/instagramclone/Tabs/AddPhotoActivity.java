package com.instagramclone.Tabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.instagramclone.MainActivity;
import com.instagramclone.Models.ContentDTO;
import com.instagramclone.R;
import com.instagramclone.databinding.ActivityAddPhotoBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.instagramclone.Utils.StatusCode.PICK_IMAGE_FROM_ALBUM;

public class AddPhotoActivity extends AppCompatActivity implements View.OnClickListener {

    // Data Binding
    private ActivityAddPhotoBinding binding;
    private String photoUrl;
    private String imageName;
    // Firebase Storage, Database, Auth
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    //사진 미 선택시 뒤로가
    private int checkPhotoSelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_photo);

        //ImageView Button EditText 찾아오고 버튼 세팅하기
        binding.addphotoBtnUpload.setOnClickListener(this);

        //권한 요청 하는 부분
        ActivityCompat.requestPermissions
                (this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        //사진올리기
        binding.addphotoImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM);
            }
        });

        //사진선택 확인 : init
        checkPhotoSelected = 0;
        // Firebase storage
        firebaseStorage = FirebaseStorage.getInstance();

        // Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 앨범에서 사진 선택시 호출 되는 부분
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_FROM_ALBUM && resultCode == RESULT_OK) {

            String[] proj = {MediaStore.Images.Media.DATA};
            CursorLoader cursorLoader = new CursorLoader(this, data.getData(), proj,
                    null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            //이미지 경로
            photoUrl = cursor.getString(column_index);
            imageName = photoUrl.substring(photoUrl.lastIndexOf("/")+1);
            //이미지뷰에 이미지 세팅
            binding.addphotoImage.setImageURI(data.getData());
            //사진 선택
            checkPhotoSelected++;
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.addphoto_btn_upload && photoUrl != null) {

            binding.progressBar.setVisibility(View.VISIBLE);

            File file = new File(photoUrl);
            Uri contentUri = Uri.fromFile(file);
            StorageReference storageRef1 =
                    firebaseStorage.getReferenceFromUrl("gs://tourism-5864e.appspot.com").child("images").child(contentUri.getLastPathSegment());
            UploadTask uploadTask = storageRef1.putFile(contentUri);
            uploadTask
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            binding.progressBar.setVisibility(View.GONE);

                            Toast.makeText(AddPhotoActivity.this, getString(R.string.upload_success),
                                    Toast.LENGTH_SHORT).show();

                            @SuppressWarnings("VisibleForTests")
                            //Uri uri = taskSnapshot.getDownloadUrl();///////////////////////////////////////////////////////////////////////////////////////
                                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            //디비에 바인딩 할 위치 생성 및 컬렉션(테이블)에 데이터 집합 생성
                            DatabaseReference images = firebaseDatabase.getReference().child("images").push();

                            //시간 생성
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            ContentDTO contentDTO = new ContentDTO();

                            //이미지 주소
                            contentDTO.imageUrl = uri.toString();
                            //유저의 UID
                            contentDTO.uid = firebaseAuth.getCurrentUser().getUid();
                            //게시물의 설명
                            contentDTO.explain = binding.addphotoEditExplain.getText().toString();
                            //유저의 아이디
                            contentDTO.userId = firebaseAuth.getCurrentUser().getEmail();
                            //게시물 업로드 시간
                            contentDTO.timestamp = simpleDateFormat.format(date);
                            contentDTO.imageName = imageName;
                            //게시글 장소 ! - 추가됨 03/30 15:50
                            contentDTO.location = binding.addphotoLocation.getText().toString();

                            //게시물을 데이터를 생성 및 엑티비티 종료
                            images.setValue(contentDTO);

                            setResult(RESULT_OK);

                            this.finish();
                        }

                        private void finish() {
                            AddPhotoActivity.super.finish();
                            startActivity(new Intent(AddPhotoActivity.this, MainActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            binding.progressBar.setVisibility(View.GONE);

                            Toast.makeText(AddPhotoActivity.this, getString(R.string.upload_fail),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

}