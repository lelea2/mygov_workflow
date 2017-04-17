package com.kdao.mygov_workflow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;

import com.kdao.mygov_workflow.util.PreferenceData;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                PreferenceData.clearLoggedInEmailAddress(getApplicationContext());
                boolean userLoggedIn = PreferenceData.getUserLoggedInStatus(getApplicationContext());
                if (userLoggedIn == false) { //navigate to signin page if user is not signin yet*/
                    Intent signinIntent = new Intent(SplashActivity.this, SigninActivity.class);
                    SplashActivity.this.startActivity(signinIntent);
                } else {
                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(mainIntent);
                }
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_TIME);
    }
}
