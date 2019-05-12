package com.project.aifoto;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private List<User> senderList;
    private Context context;
    private ViewHolder viewHolder;
    private int position;

    public NotificationRecyclerAdapter(Context context, List<Notification> notificationList, List<User> senderList) {
        this.notificationList = notificationList;
        this.context = context;
        this.senderList = senderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_list_item, viewGroup, false);
        //      context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        this.viewHolder = viewHolder;
        this.position = position;
        viewHolder.setNotificationInfo();

        try {
            long millisecond = notificationList.get(position).getTimestamp().getTime();
            //postList.get(i).getTimestamp().g

            String dateString = DateFormat.format("yyyy-MM-dd hh:mm:ss a", new Date(millisecond)).toString();
            viewHolder.setTime(dateString);

        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        String postId = notificationList.get(position).getPost_id();
        String postOwnerId = notificationList.get(position).getPost_owner_id();
        viewHolder.switchToCommentActivityIfClicked(viewHolder, postId, postOwnerId);

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private CircleImageView senderImageCircleImageView;
        private TextView senderUsernameTextView;
        private TextView notificationTimestamp;
        private TextView postDescTextView;
        private ConstraintLayout layoutNotificationItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            senderImageCircleImageView = mView.findViewById(R.id.notification_image);
            senderUsernameTextView = mView.findViewById(R.id.notification_username);
            notificationTimestamp = mView.findViewById(R.id.notification_timestamp);
            layoutNotificationItem = mView.findViewById(R.id.layout_notification_item);
            postDescTextView = mView.findViewById(R.id.notification_post_desc);
        }

        public void setNotificationInfo() {

            String senderName = senderList.get(position).getName();
            String senderImageUrl = senderList.get(position).getImage();
            String postDesc = notificationList.get(position).getPost_desc();

            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.profile_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(senderImageUrl).into(senderImageCircleImageView);
            senderUsernameTextView.setText(senderName + " commented on your post:");
            postDescTextView.setText(postDesc);
            //notification_username.setText(notificationList.get(i).getComment_owner_id());
        }

        public void setTime(String dateString) {
            notificationTimestamp.setText(dateString);
        }

        private void switchToCommentActivityIfClicked(ViewHolder viewHolder, final String postId, final String userId) {
            viewHolder.layoutNotificationItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent commentIntent = new Intent(context, CommentsActivity.class);
                    commentIntent.putExtra("postId", postId);
                    commentIntent.putExtra("user_id", userId);
                    context.startActivity(commentIntent);
                }
            });
        }
    }
}
