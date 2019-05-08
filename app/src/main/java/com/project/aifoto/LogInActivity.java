package com.project.aifoto;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class LogInActivity extends AppCompatActivity {

    private EditText editTextLoginEmail;
    private EditText editTextLoginPW;
    private Button btnLogin;
    private Button btnLoginRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initialize();
    }

    private void initialize() {
        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginPW = findViewById(R.id.editTextLoginPW);

        btnLogin = findViewById(R.id.buttonLogin);
        btnLoginRegister = findViewById(R.id.buttonLoginReg);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressBarLogin = findViewById(R.id.progressLogin);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(LogInActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void btnLoginClick(View view) {


        String loginEmail = editTextLoginEmail.getText().toString();
        String loginPW = editTextLoginPW.getText().toString();

        if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPW)) {


            mAuth.signInWithEmailAndPassword(loginEmail, loginPW).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBarLogin.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        sendToMainActivity();

                        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( LogInActivity.this,  new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                String token_id = instanceIdResult.getToken();
                                Log.e("Token", token_id);

                                String currentId = mAuth.getCurrentUser().getUid();
                                Map<String, Object> tokenMap = new HashMap<>();
                                tokenMap.put("token_id", token_id);

                                firebaseFirestore.collection("Users").document(currentId).update(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBarLogin.setVisibility(View.INVISIBLE);
                                    }
                                });

                            }
                        });
                    } else {
                        String errorMsg = task.getException().getMessage();
                        Toast.makeText(LogInActivity.this, "Error : " + errorMsg, Toast.LENGTH_LONG).show();
                        progressBarLogin.setVisibility(View.INVISIBLE);
                    }

                }
            });
        }
    }

    public void btnLoginRegisClick(View view) {
        Intent regIntent = new Intent(LogInActivity.this, RegisterActivity.class);
        startActivity(regIntent);
        //finish();
    }




}