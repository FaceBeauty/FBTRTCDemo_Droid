package com.tencent.trtc.apiexample;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.nimo.facebeauty.FBEffect;
import com.nimo.facebeauty.FBEffect.InitCallback;

public class TRTCApplication extends Application {

    private static TRTCApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        instance = this;
        //todo --- facebeauty start ---
        FBEffect.shareInstance().initFaceBeauty( this, "YOUR_APP_ID", new InitCallback() {

            @Override public void onInitSuccess() {

            }

            @Override public void onInitFailure() {

            }
        });
        //todo --- facebeauty end ---
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
