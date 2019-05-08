package com.project.aifoto;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class CommentsActivity extends AppCompatActivity {

    private Toolbar toolbarComment;

    private EditText editTextCommentField;
    private ImageView imageViewCommentPostBtn;

    private String postId;
    private String postOwnerId;
    private String currentUserId;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private List<Comments> commentsList;
    private List<User> userList;

    private RecyclerView recyclerViewCommentList;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    private ProgressBar progressBarComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        initialize();
    }

    private void initialize() {
        toolbarComment = findViewById(R.id.toolbarComment);
        setSupportActionBar(toolbarComment);
        getSupportActionBar().setTitle("Comments");

        postId = getIntent().getStringExtra("postId");
        postOwnerId = getIntent().getStringExtra("user_id");

        progressBarComment = findViewById(R.id.progressBarComment);

        editTextCommentField = findViewById(R.id.editTextCommentField);
        imageViewCommentPostBtn = findViewById(R.id.imageViewCommentPost);

        commentsList = new ArrayList<>();
        userList = new ArrayList<>();
        recyclerViewCommentList = findViewById(R.id.recyclerViewCommentList);

        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList,userList);

        recyclerViewCommentList.setHasFixedSize(true);
        recyclerViewCommentList.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCommentList.setAdapter(commentsRecyclerAdapter);

        initializeFirebaseAll();
        setOnClickListenerForCommentField();

        //Recycler View Firebase Comment List
        firebaseFirestore.collection("Posts/"+postId+"/Comments").addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String commentId = doc.getDocument().getId();
                            final Comments comments = doc.getDocument().toObject(Comments.class);

                            String commentUserId = doc.getDocument().getString("user_id");
                            firebaseFirestore.collection("Users").document(commentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        User user = task.getResult().toObject(User.class);

                                        userList.add(user);
                                        commentsList.add(comments);
                                        commentsRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                            });

                        }
                    }


                }
            }
        });


    }

    private void initializeFirebaseAll() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();
    }

    private void setOnClickListenerForCommentField() {
        imageViewCommentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentMessage = editTextCommentField.getText().toString();
                if(commentMessage!=null && !commentMessage.isEmpty()){

                    progressBarComment.setVisibility(View.VISIBLE);

                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", commentMessage);
                    commentsMap.put("user_id", currentUserId);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/"+postId+"/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this, "Error Comments : "+task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }else {

                                editTextCommentField.setText("");
                                createNotification(postId, currentUserId);
                            }
                        }
                    });
                }
            }
        });
    }

    public void createNotification(String postId, String currentUserId){
        String path = "Users/"+postOwnerId+"/Notifications";
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("comment_owner_id", currentUserId);
        notificationMap.put("post_id", postId);
        notificationMap.put("post_owner_id", postOwnerId);
        notificationMap.put("timestamp", FieldValue.serverTimestamp());


        storeMaptoFirebase(notificationMap,path);
    }

    private void storeMaptoFirebase(Map<String, Object> notificationMap, String path) {
        firebaseFirestore.collection(path).add(notificationMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getApplicationContext(), "Notification is stored to Firebase", Toast.LENGTH_LONG).show();
                progressBarComment.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error "+e.getMessage(), Toast.LENGTH_LONG).show();
                progressBarComment.setVisibility(View.INVISIBLE);
            }
        });
    }
}
