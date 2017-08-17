package org.loklak.wok.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import org.loklak.wok.ui.fragment.TweetHarvestingFragment;
import org.loklak.wok.R;

public class TweetHarvestingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_harvesting);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (savedInstanceState == null) {
            TweetHarvestingFragment tweetHarvestingFragment = new TweetHarvestingFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, tweetHarvestingFragment)
                    .commit();
        }
    }
}
