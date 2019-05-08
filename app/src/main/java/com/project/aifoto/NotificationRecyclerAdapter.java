package com.project.aifoto;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

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

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_list_item,viewGroup,false);
  //      context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        this.viewHolder =viewHolder;
        this.position = position;
        viewHolder.setNotificationInfo();

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private  View mView;

        private CircleImageView senderImageCircleImageView;
        private TextView senderUsernameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            senderImageCircleImageView = mView.findViewById(R.id.notification_image);
            senderUsernameTextView = mView.findViewById(R.id.notification_username);

        }

        public void setNotificationInfo(){

            String senderName = senderList.get(position).getName();
            String senderImageUrl = senderList.get(position).getImage();

            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.profile_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(senderImageUrl).into(senderImageCircleImageView);
            senderUsernameTextView.setText(senderName);
            //notification_username.setText(notificationList.get(i).getComment_owner_id());
        }
    }
}
