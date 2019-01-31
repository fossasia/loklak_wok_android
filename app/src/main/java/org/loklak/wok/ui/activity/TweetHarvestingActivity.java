package org.loklak.wok.ui.activity;

import . . .

public class TweetHarvestingActivity extends AppCompatActivity {

    long BackPressed;

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

    @Override
    public void onBackPressed() {
        if (BackPressed+1000>System.currentTimeMillis()) {
            
            super.onBackPressed();
            return;
        }
        else{
            Toast.makeText(getBaseContext(),"Press once again to exit",Toast.LENGTH_SHORT).show();
        }
        BackPressed = System.currentTimeMillis();
    }
}
