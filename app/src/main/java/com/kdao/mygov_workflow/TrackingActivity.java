package com.kdao.mygov_workflow;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import android.support.annotation.IdRes;

public class TrackingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        setupBottomBar();
    }

    /**
     * Private function to set up bottom bar
     */
    private void setupBottomBar() {
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTabPosition(2);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_home) {
                    // The tab with id R.id.tab_favorites was selected,
                    // change your content accordingly.
                    Intent newIntent = new Intent(TrackingActivity.this, MainActivity.class);
                    startActivity(newIntent);
                } else if (tabId == R.id.tab_claim) {
                    Intent newIntent = new Intent(TrackingActivity.this, ClaimActivity.class);
                    startActivity(newIntent);
                } else if (tabId == R.id.tab_message) {
                    Intent newIntent = new Intent(TrackingActivity.this, MessageActivity.class);
                    startActivity(newIntent);
                }
            }
        });

    }
}
