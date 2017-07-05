package org.loklak.android.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.loklak.android.ui.fragment.TweetHarvestingFragment;
import org.loklak.android.wok.R;

public class TweetHarvestingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_harvesting);
        if (savedInstanceState == null) {
            TweetHarvestingFragment tweetHarvestingFragment = new TweetHarvestingFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, tweetHarvestingFragment)
                    .commit();
        }
    }
}
