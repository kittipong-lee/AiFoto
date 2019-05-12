package com.project.aifoto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;


public class SetupAccountActivity extends AppCompatActivity {

    private ImageView setupDefaultImage;
    private Toolbar toolbarSetupAccount;

    private EditText editTextSetupName;
    private ProgressBar progressBarSetupAccount;
    private Button btnSaveAccountSetting;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Uri mainImageUri = null;

    private String userId;
    private String userName;

    private String downloadUri;

    private boolean isProfileImageChanged;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);
        initialize();
    }

    private void initialize() {
        setupDefaultImage = findViewById(R.id.imageSetupDefaultProfile);
        toolbarSetupAccount = findViewById(R.id.toolbarSetupAccount);
        setSupportActionBar(toolbarSetupAccount);
        getSupportActionBar().setTitle("Account Setup");

        editTextSetupName = findViewById(R.id.editTextName);

        btnSaveAccountSetting = findViewById(R.id.buttonSetup);


        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressBarSetupAccount = findViewById(R.id.progressBarSetupAccount);

        progressBarSetupAccount.setVisibility(View.VISIBLE);
        btnSaveAccountSetting.setEnabled(false);

        userId = firebaseAuth.getCurrentUser().getUid();

        isProfileImageChanged = false;

        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);

                        editTextSetupName.setText(name);

                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.default_image);

                        Glide.with(SetupAccountActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(setupDefaultImage);

                        Toast.makeText(SetupAccountActivity.this, "Data Exists ", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(SetupAccountActivity.this, "Data Doesn't Exist ", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupAccountActivity.this, "Firestore Error: " + error, Toast.LENGTH_LONG).show();

                }
                progressBarSetupAccount.setVisibility(View.INVISIBLE);
                btnSaveAccountSetting.setEnabled(true);
            }
        });
    }

    public void imageClick(View view) {

        //Check if user uses Mashmello or higher version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(SetupAccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(SetupAccountActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(SetupAccountActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                // Toast.makeText(SetupAccountActivity.this,"You already have a permission", Toast.LENGTH_LONG).show();

                pickAndCropImage();

            }
        } else {
            pickAndCropImage();
        }
    }

    private void pickAndCropImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupAccountActivity.this);
    }


    //
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                isProfileImageChanged = true;
                setupDefaultImage.setImageURI(mainImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void btnSaveAccountSettingClick(View view) {
        final StorageReference imagePath = storageReference.child("profile_images").child(userId + ".jpg");
        userName = editTextSetupName.getText().toString();

        if (!TextUtils.isEmpty(userName) && mainImageUri != null) {
            progressBarSetupAccount.setVisibility(View.VISIBLE);
            if (isProfileImageChanged) {

                imagePath.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            uploadToFirestore(task, imagePath);

                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(SetupAccountActivity.this, "Image Error: " + error, Toast.LENGTH_LONG).show();
                        }

                        progressBarSetupAccount.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                uploadToFirestore(null, imagePath);
            }
        }
    }

    private void uploadToFirestore(@NonNull final Task<UploadTask.TaskSnapshot> task, StorageReference imagePath) {
        // String downloadUri = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
        // Toast.makeText(SetupAccountActivity.this,"The image is uploaded",Toast.LENGTH_LONG).show();

        imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                if (task != null) {
                    downloadUri = uri.toString();
                } else {
                    downloadUri = mainImageUri.toString();
                }
                Toast.makeText(SetupAccountActivity.this, downloadUri, Toast.LENGTH_LONG).show();


                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String token_id = instanceIdResult.getToken();
                        Log.e("Token", token_id);

                        Map<String, String> userMap = new HashMap<>();
                        userMap.put("name", userName);
                        userMap.put("image", downloadUri);
                        userMap.put("token_id", token_id);

                        Log.d("This is URL Na jaa", downloadUri);

                        firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    Toast.makeText(SetupAccountActivity.this, "Setting Updated", Toast.LENGTH_LONG).show();
                                    Intent mainIntent = new Intent(SetupAccountActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupAccountActivity.this, "Firestore Error: " + error, Toast.LENGTH_LONG).show();

                                }
                                progressBarSetupAccount.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetupAccountActivity.this, "Mai dai na", Toast.LENGTH_LONG).show();
            }
        });
    }
}
