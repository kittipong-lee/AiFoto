package com.project.aifoto;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ImageView setupDefaultImage;
    // private Toolbar toolbarSetupAccount;

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

    private View view;
    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        this.savedInstanceState = savedInstanceState;

        view = inflater.inflate(R.layout.fragment_account, container, false);

        initialize();


        // Inflate the layout for this fragment
        return view;
    }

    private void initialize() {
        setupDefaultImage = view.findViewById(R.id.imageSetupProfileFragment);
        //  toolbarSetupAccount = view.findViewById(R.id.toolbarSetupAccount);
        //view.setSupportActionBar(toolbarSetupAccount);
        // getSupportActionBar().setTitle("Account Setup");

        editTextSetupName = view.findViewById(R.id.editTextAccountNameFragment);

        btnSaveAccountSetting = view.findViewById(R.id.buttonAccountSetupFragment);


        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressBarSetupAccount = view.findViewById(R.id.progressBarSetupAccountFragment);

        progressBarSetupAccount.setVisibility(View.VISIBLE);
        btnSaveAccountSetting.setEnabled(false);

        userId = firebaseAuth.getCurrentUser().getUid();

        isProfileImageChanged = false;
        btnSaveAccountSettingClick();
        imageClick();


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

                        Glide.with(container.getContext()).setDefaultRequestOptions(placeHolderRequest).load(image).into(setupDefaultImage);
                        Toast.makeText(container.getContext(), "Data Exists ", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(container.getContext(), "Data Doesn't Exist ", Toast.LENGTH_LONG).show();

                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(container.getContext(), "Firestore Error: " + error, Toast.LENGTH_LONG).show();

                }
                progressBarSetupAccount.setVisibility(View.INVISIBLE);
                btnSaveAccountSetting.setEnabled(true);
            }
        });
    }

    public void imageClick() {

        setupDefaultImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAndCropImage();
            }
        });


    }

    private void pickAndCropImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(getContext(), this);

    }

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

    public void btnSaveAccountSettingClick() {

        btnSaveAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                    Toast.makeText(getContext(), "Image Error: " + error, Toast.LENGTH_LONG).show();
                                }

                                progressBarSetupAccount.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {
                        uploadToFirestore(null, imagePath);
                    }
                }
            }
        });


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
                Toast.makeText(getContext(), downloadUri, Toast.LENGTH_LONG).show();


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

                                    Toast.makeText(getContext(), "Setting Updated", Toast.LENGTH_LONG).show();
                                    Intent mainIntent = new Intent(getContext(), MainActivity.class);
                                    startActivity(mainIntent);
                                    // finish();
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(getContext(), "Firestore Error: " + error, Toast.LENGTH_LONG).show();

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
                Toast.makeText(getContext(), "Mai dai na", Toast.LENGTH_LONG).show();
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
