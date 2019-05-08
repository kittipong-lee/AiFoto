package com.project.aifoto;


import java.util.Date;

public class MyPost extends  PostId {
    public String user_id, image_url, desc, thumb_image;
    public Date timestamp;







    public MyPost(){}

    public MyPost(String user_id, String image_url, String desc, String thumb_image,Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.thumb_image = thumb_image;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public Date getTimestamp() {
        return timestamp;
    }

//    public void setTimestamp(Date timestamp) {
//        this.timestamp = timestamp;
//    }
}
