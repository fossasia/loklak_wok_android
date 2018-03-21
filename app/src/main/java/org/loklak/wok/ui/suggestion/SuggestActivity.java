package org.loklak.wok.ui.suggestion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.loklak.wok.R;

public class SuggestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest);
        if (savedInstanceState == null) {
            SuggestFragment suggestFragment = new SuggestFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, suggestFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_button_enter, R.anim.back_button_exit);
    }
}
