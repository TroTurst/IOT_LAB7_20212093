package com.example.bici_pucp;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class BiciApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
