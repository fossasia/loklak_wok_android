package org.loklak.wok.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.loklak.wok.ui.fragment.TweetPostingFragment;
import org.loklak.wok.R;


public class TweetPostingActivity extends AppCompatActivity {

    private final String LOG_TAG = TweetPostingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_posting);
        if (savedInstanceState == null) {
            TweetPostingFragment tweetPostingFragment = new TweetPostingFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, tweetPostingFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_button_top_enter, R.anim.back_button_bottom_exit);
    }
}
