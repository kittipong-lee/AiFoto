package com.project.aifoto;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class PostId {

    @Exclude
    public  String PostId;

    public <T extends PostId> T withId(@NonNull final String id){
        this.PostId = id;
        return (T) this;
    }
}
