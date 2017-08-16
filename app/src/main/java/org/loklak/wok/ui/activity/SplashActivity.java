package org.loklak.wok.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import org.loklak.wok.R;


public class SplashActivity extends AppCompatActivity {

    private Handler mHandler;

    private Runnable mRunnable = () -> {
        Intent intent = new Intent(this, TweetHarvestingActivity.class);
        startActivity(intent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable, 3000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeCallback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeCallback();
    }

    private void removeCallback() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }
}
