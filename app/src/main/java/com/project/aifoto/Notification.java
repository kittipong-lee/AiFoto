package com.project.aifoto;

import java.util.Date;

public class Notification {

    private String comment_owner_id;
    private String post_id;
    private Date timestamp;private String post_owner_id;

    public Notification() {
    }

    public Notification(String comment_owner_id, String post_id, Date timestamp, String post_owner_id) {
        this.comment_owner_id = comment_owner_id;
        this.post_id = post_id;
        this.timestamp = timestamp;
        this.post_owner_id = post_owner_id;
    }

    public String getComment_owner_id() {
        return comment_owner_id;
    }

    public void setComment_owner_id(String comment_owner_id) {
        this.comment_owner_id = comment_owner_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPost_owner_id() {
        return post_owner_id;
    }

    public void setPost_owner_id(String post_owner_id) {
        this.post_owner_id = post_owner_id;
    }
}
