package com.project.aifoto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.admin.v1beta1.Index;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    private List<MyPost> postList;
    private List<User> userList;
    private Context context;
    private TextView postDate;
    private TextView postUsername;
    private CircleImageView postUserImage;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public PostRecyclerAdapter(List<MyPost> postList, List<User> userList) {
        this.postList = postList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_items, viewGroup, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {

        final String postId = postList.get(position).PostId;
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String postDesc = postList.get(position).getDesc();
        viewHolder.setDescText(postDesc);

        String postImageUrl = postList.get(position).getImage_url();
        String postImageThumbUrl = postList.get(position).getThumb_image();
        viewHolder.setPostImage(postImageUrl, postImageThumbUrl);

        String postUserId = postList.get(position).getUser_id();

        String userName = userList.get(position).getName();
        String userImage = userList.get(position).getImage();

        viewHolder.setUserData(userName, userImage);
        viewHolder.enableDeleteBtn(postUserId, currentUserId);

        try {
            long millisecond = postList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            viewHolder.setTime(dateString);

        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            setLikeImage(viewHolder, postId, currentUserId);
            managePostLikeDataInDatabase(viewHolder, postId, currentUserId);
            setLikeCount(viewHolder, postId, currentUserId);
            setCommentsCount(viewHolder, postId);
            switchToCommentActivityIfClicked(viewHolder, postId, postUserId, postDesc, postImageThumbUrl);
            viewHolder.buttonDeleteOnClick(viewHolder, postId, position);
        }
    }

    private void setLikeCount(final ViewHolder viewHolder, String postId, String currentUserId) {
        firebaseFirestore.collection("Posts").document(postId).collection("Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        try {
                            if (!queryDocumentSnapshots.isEmpty()) {

                                int likesCount = queryDocumentSnapshots.size();
                                viewHolder.updateLikesCount(likesCount);
                            } else {
                                viewHolder.updateLikesCount(0);
                            }
                        } catch (Exception er) {
                            Log.e("Firebase Exception", e.getMessage());
                            Log.e(" Exception", er.getMessage());
                        }
                    }
                });
    }

    public void setCommentsCount(final ViewHolder viewHolder, String postId) {
        firebaseFirestore.collection("Posts").document(postId).collection("Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        try {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                int commentsCount = queryDocumentSnapshots.size();
                                viewHolder.updateCommentsCount(commentsCount);
                            } else {
                                viewHolder.updateCommentsCount(0);
                            }
                        } catch (Exception er) {
                            Log.e("Firebase Exception", e.getMessage());
                            Log.e(" Exception", er.getMessage());
                        }
                    }
                });
    }

    private void setLikeImage(final ViewHolder viewHolder, String postId, String currentUserId) {
        firebaseFirestore.collection("Posts").document(postId).collection("Likes")
                .document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                try {
                    if (documentSnapshot.exists()) {
                        viewHolder.btnPostLike.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));
                    } else {
                        viewHolder.btnPostLike.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));
                    }
                } catch (Exception er) {
                    Log.e("Firebase Exception", e.getMessage());
                    Log.e(" Exception", er.getMessage());
                }
            }
        });
    }

    private void managePostLikeDataInDatabase(ViewHolder viewHolder, final String postId, final String currentUserId) {
        viewHolder.btnPostLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts").document(postId).collection("Likes")
                        .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            //Posts/postId/Likes
                            firebaseFirestore.collection("Posts").document(postId).collection("Likes")
                                    .document(currentUserId).set(likesMap);
                        } else {
                            firebaseFirestore.collection("Posts").document(postId).collection("Likes")
                                    .document(currentUserId).delete();
                        }
                    }
                });


            }
        });
    }

    private void switchToCommentActivityIfClicked(ViewHolder viewHolder, final String postId, final String userId,
                                                  final String postDesc, final String postImageThumbUrl) {
        viewHolder.imageViewCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("postId", postId);
                commentIntent.putExtra("user_id", userId);
                commentIntent.putExtra("post_desc", postDesc);
                commentIntent.putExtra("post_image_url", postImageThumbUrl);
                context.startActivity(commentIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView descView;
        private ImageView postImageView;
        private ImageView btnPostLike;
        private TextView textViewPostLikeCount;
        private TextView textViewCommentsCount;

        private ImageView imageViewCommentBtn;
        private ImageView btnPostDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            imageViewCommentBtn = mView.findViewById(R.id.imageViewComment);

            btnPostLike = mView.findViewById(R.id.btnPostLike);
            // textViewPostLikeCount = mView.findViewById(R.id.textViewPostLikeCount);
            btnPostDelete = mView.findViewById(R.id.btnPostDelete);
        }

        public void setDescText(String descText) {
            descView = mView.findViewById(R.id.textViewPostDesc);
            descView.setText(descText);
        }

        public void setPostImage(String downloadUri, String imageThumbUrl) {
            postImageView = mView.findViewById(R.id.imageViewPost);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(Glide.with(context).load(imageThumbUrl)).into(postImageView);
        }

        public void setTime(String date) {
            postDate = mView.findViewById(R.id.textViewPostDate);
            postDate.setText(date);
        }

        public void setUserData(String name, String image) {
            postUserImage = mView.findViewById(R.id.circleUserImagePost);
            postUsername = mView.findViewById(R.id.textViewPostUsername);

            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.profile_placeholder);

            postUsername.setText(name);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(image).into(postUserImage);
        }

        public void updateLikesCount(int likesCount) {
            textViewPostLikeCount = mView.findViewById(R.id.textViewPostLikeCount);
            textViewPostLikeCount.setText(likesCount + " People Love This");
        }

        public void updateCommentsCount(int commentsCount) {

            textViewCommentsCount = mView.findViewById(R.id.textViewComments);
            textViewCommentsCount.setText(commentsCount + " Comments");

        }

        public void enableDeleteBtn(String postUserId, String currentUserId) {
            if (postUserId.equals(currentUserId)) {
                btnPostDelete.setEnabled(true);
                btnPostDelete.setVisibility(View.VISIBLE);
            } else {
                btnPostDelete.setEnabled(false);
                btnPostDelete.setVisibility(View.INVISIBLE);
            }
        }

        public void buttonDeleteOnClick(final ViewHolder viewHolder, final String postId, final int position) {
            viewHolder.btnPostDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
                    builder.setTitle("Delete Post Confirm !");
                    builder.setMessage("Are you sure you want to delete");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //postList.remove(position);
                            //userList.remove(position);
                            //notifyItemRemoved(position);
                            firebaseFirestore.collection("Posts").document(postId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //viewHolder.showDeleteConfirmDialog(position);
                                    if (position == postList.size() - 1) {
                                        postList.remove(position);
                                        userList.remove(position);
                                        notifyItemRemoved(position);
                                    } else {
                                        int shift = 1;
                                        while (true) {
                                            try {
                                                int shiftedPosition = position - shift;
                                                postList.remove(shiftedPosition);
                                                userList.remove(shiftedPosition);
                                                notifyItemRemoved(position);
                                                Toast.makeText(mView.getContext(), "Post Deleted", Toast.LENGTH_SHORT).show();
                                                break;
                                            } catch (IndexOutOfBoundsException e) {
                                                shift++;
                                            }
                                        }
                                    }
                                }
                            });

                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(mView.getContext(), "Post Deletion Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.show();

                }
            });
        }

        public void showDeleteConfirmDialog(final int position) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
            builder.setTitle("Delete Post Confirm !");
            builder.setMessage("Are you sure you want to delete");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postList.remove(position);
                    userList.remove(position);
                    Toast.makeText(mView.getContext(), "Post Deleted", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(mView.getContext(), "Post Deletion Cancelled", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();
        }
    }
}
