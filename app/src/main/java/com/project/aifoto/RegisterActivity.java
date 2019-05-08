package com.project.aifoto;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPW;
    private EditText editTextConfirmPW;

    private ProgressBar progressBarRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialize();
    }

    private void initialize() {
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextRegEmail);
        editTextPW =findViewById(R.id.editTextRegPW);
        editTextConfirmPW = findViewById(R.id.editTextRegConfirmPW);

        progressBarRegister = findViewById(R.id.progressRegis);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            switchToMainPage();
        }
    }

    private void switchToMainPage() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void btnRegisterClick(View view) {
        String email = editTextEmail.getText().toString();
        String password = editTextPW.getText().toString();
        String conFirmPW = editTextConfirmPW.getText().toString();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) &&!TextUtils.isEmpty(conFirmPW)){
            if(password.equals(conFirmPW)){

                progressBarRegister.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            switchToSetupAccount();
                        }else{
                            String errorMsg = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Error : " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                        progressBarRegister.setVisibility(View.INVISIBLE);
                    }
                });
            }else{
                Toast.makeText(RegisterActivity.this, "Confirm Password doesn't match with Password", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void switchToSetupAccount() {
        Intent setupAccountIntent = new Intent(RegisterActivity.this, SetupAccountActivity.class);
        startActivity(setupAccountIntent);
        finish();
    }

    public void btnRegisterLoginClick(View view) {
        finish();
    }
}
