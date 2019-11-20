package com.slamtec.slamware.uicommander.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.slamtec.slamware.uicommander.SlamwareAgent;


public class BaseActivity extends AppCompatActivity {

    private static SlamwareAgent mSlamwareAgent;

    static {
        mSlamwareAgent = new SlamwareAgent();
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public SlamwareAgent getSlamwareAgent() {
        return mSlamwareAgent;
    }

}
