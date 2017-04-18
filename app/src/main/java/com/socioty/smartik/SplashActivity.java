package com.socioty.smartik;

import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by serhiipianykh on 2017-04-17.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onStart() {
        super.onStart();

        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(new Intent(this, LoginActivity.class))
                .addNextIntent(new Intent(this, IntroActivity.class))
                .startActivities();
    }
}
