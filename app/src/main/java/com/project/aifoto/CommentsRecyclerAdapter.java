package com.project.aifoto;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    private List<Comments> commentsList;
    private List<User> userList;

    public Context context;

    private CommentsRecyclerAdapter.ViewHolder holder;
    private int position;

    private Uri imageUri;

    public CommentsRecyclerAdapter() {}

    public CommentsRecyclerAdapter(List<Comments> commentsList, List<User> userList){

        this.commentsList = commentsList;
        this.userList = userList;


    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();
        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsRecyclerAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        this.holder = holder;
        this.position = position;



        setCommentMessage();
//        setCommentUsername();
        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        String name = userList.get(position).getName();
        String imageUrl = userList.get(position).getImage();
        holder.setUserData(name,imageUrl);

    }

    private void setCommentMessage() {
        String commentMessage = commentsList.get(position).getMessage();
        holder.setComment_message(commentMessage);
    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView comment_message;
        private  TextView commentUsername;

        private CircleImageView circleViewUserImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }



        public void setUserData(String name, String image){
            circleViewUserImage = mView.findViewById(R.id.comment_image);
            commentUsername = mView.findViewById(R.id.comment_username);

            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.profile_placeholder);

            commentUsername.setText(name);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(image).into(circleViewUserImage);
        }

    }

}
