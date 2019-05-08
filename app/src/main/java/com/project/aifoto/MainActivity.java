package com.project.aifoto;

import android.content.Intent;
import android.icu.text.Replaceable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener,
        NotificationFragment.OnFragmentInteractionListener , AccountFragment.OnFragmentInteractionListener{

    private Toolbar toolbarMain;
    private BottomNavigationView bottomNavMain;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();


     //   NavigationListener();

    }



    private void initialize() {
        toolbarMain = findViewById(R.id.toolbarNewPost);

        setSupportActionBar(toolbarMain);
        getSupportActionBar().setTitle("WePost");

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //

        if(mAuth.getCurrentUser()!=null) {
        //
           // initializeFragment();
            bottomNavMain = findViewById(R.id.bottomNavMain);

            //Fragments
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();

            bottomNavMain.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.btnNavigationActionHome:
                            replaceFragment(homeFragment);
                            return true;

                        case R.id.btnNavigationActionAccount:
                            replaceFragment(accountFragment);
                            return true;

                        case R.id.btnNavigationActionNotification:
                            replaceFragment(notificationFragment);
                            return true;

                        default:
                            return false;
                    }

                }
            });
        }
    }

    private void initializeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if(!homeFragment.isAdded()&&!notificationFragment.isAdded()&&!accountFragment.isAdded()) {
            fragmentTransaction.add(R.id.FrameLayoutMainContainer, homeFragment);
            fragmentTransaction.add(R.id.FrameLayoutMainContainer, notificationFragment);
            fragmentTransaction.add(R.id.FrameLayoutMainContainer, accountFragment);

            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.hide(accountFragment);

            fragmentTransaction.commit();
        }
    }

    private void NavigationListener() {

    }

    @Override
    protected void onStart(){
        super.onStart();

       // FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            initializeFragment();
            //replaceFragment(homeFragment);
            currentUserId = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            Intent setupAccountIntent = new Intent(MainActivity.this, SetupAccountActivity.class);
                            startActivity(setupAccountIntent);
                        }
                    }else{
                        String errorMsg = task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error : "+errorMsg,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            switchToLoginPage();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.btn_action_logout:
                logOut();return true;
            case R.id.btn_action_account_setting:
                Intent accountSettingIntent = new Intent(MainActivity.this,SetupAccountActivity.class);
                startActivity(accountSettingIntent);return true;

            default: return false;
        }


    }

    private void logOut() {


        Map<String, Object> tokenMapRemove = new HashMap<>();
        tokenMapRemove.put("token_id", FieldValue.delete());
        firebaseFirestore.collection("Users").document(currentUserId).update(tokenMapRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mAuth.signOut();
                switchToLoginPage();
            }
        });


    }

    private void switchToLoginPage() {

        Intent loginIntent = new Intent(MainActivity.this,LogInActivity.class);
        startActivity(loginIntent);
        finish();
    }

    public void floatingBtnClick(View view) {
        Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.replace(R.id.FrameLayoutMainContainer,fragment);
        if(fragment == homeFragment){
            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationFragment);
        }else if(fragment == accountFragment){
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);
        } else if(fragment == notificationFragment){
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);
        }
        fragmentTransaction.show(fragment);

        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
