package com.example.attendanceuserapp;

import android.app.Application;

public class UserDetails extends Application {
    private String uid;

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
