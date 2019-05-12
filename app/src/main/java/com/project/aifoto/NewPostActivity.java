package com.project.aifoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;


public class NewPostActivity extends AppCompatActivity {

    private Toolbar toolbarNewPost;

    private ImageView imageNewPost;
    private EditText editTextDesc;
    private Button btnPost;

    private Uri imagePostURI;
    private ProgressBar progressBarNewPost;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth firebaseAuth;
    private String currentUserID;

    private Bitmap compressedImageFile;

    private static final int MAX_LENGTH = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        initialize();
    }

    private void initialize() {
        toolbarNewPost = findViewById(R.id.toolbarNewPost);
        setSupportActionBar(toolbarNewPost);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextDesc = findViewById(R.id.editTextDesc);
        btnPost = findViewById(R.id.btnPost);
        imageNewPost = findViewById(R.id.imageNewPost);
        progressBarNewPost = findViewById(R.id.progressBarNewPost);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

    }

    public void imageNewPostClick(View view) {
        pickAndCropImage();
    }

    private void pickAndCropImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512,512)
                .setAspectRatio(2,1)
                .start(NewPostActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imagePostURI = result.getUri();
//                isProfileImageChanged = true;
                imageNewPost.setImageURI(imagePostURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void btnPostClick(View view) {
        final String desc = editTextDesc.getText().toString();
        if(!TextUtils.isEmpty(desc) && imagePostURI!=null){
            progressBarNewPost.setVisibility(View.VISIBLE);
            final String randomName = random();
            final StorageReference filePath = storageReference.child("post_images").child(randomName+getString(R.string.image_format));

            filePath.putFile(imagePostURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){

                        File newImageFile= new File(imagePostURI.getPath());

                        try {
                            compressedImageFile = new Compressor(NewPostActivity.this)
                                    .setMaxHeight(100)
                                    .setMaxWidth(100)
                                    .setQuality(2)
                                    .compressToBitmap(newImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();

                        UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                .child(randomName+".jpg")
                                .putBytes(thumbData);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                final StorageReference filePathThumb = storageReference.child("post_images/thumbs")
                                        .child(randomName+".jpg");

                                filePathThumb.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        final String downloadUrlThumbImage = uri.toString();

                                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String downloadUrl = uri.toString();
                                                Map<String, Object> postMap = new HashMap<>();
                                                postMap.put("image_url", downloadUrl);
                                                postMap.put("thumb_image", downloadUrlThumbImage);
                                                postMap.put("desc",desc);
                                                postMap.put("timestamp", FieldValue.serverTimestamp());
                                                postMap.put("user_id", currentUserID);

                                                firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(NewPostActivity.this,"Post was added",Toast.LENGTH_LONG).show();
                                                            Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
                                                            startActivity(mainIntent);
                                                            finish();
                                                        }else{

                                                        }
                                                        progressBarNewPost.setVisibility(View.INVISIBLE);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                //Error Handling
                            }
                        });

                        //thumbFilePath.putFile(compressedImageFile);


                    }else{
                        progressBarNewPost.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


}
