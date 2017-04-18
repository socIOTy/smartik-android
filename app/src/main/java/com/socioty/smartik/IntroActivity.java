package com.socioty.smartik;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.View;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

/**
 * Created by serhiipianykh on 2017-04-15.
 */

public class IntroActivity extends MaterialIntroActivity {

    private static final String PREFS_KEY = "com.socioty.smartik.prefs";
    private static final String ONBOARDING_KEY = "com.socioty.smartik.onboardingstatus";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getOnboardingStatus()) this.finish();

        enableLastSlideAlphaExitTransition(true);
        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.first_slide_background)
                        .buttonsColor(R.color.first_slide_buttons)
                        .image(R.drawable.logo)
                        .title("Welcome")
                        .description("To work with our app you need to create a Samsung ARTIK Cloud account first")
                        .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.second_slide_background)
                .buttonsColor(R.color.second_slide_buttons)
                .image(R.drawable.rooms_img)
                .title("Rooms")
                .description("Set up rooms and control all devices assigned")
                .build());

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.third_slide_background)
                        .buttonsColor(R.color.third_slide_buttons)
                        .image(R.drawable.devices_img)
                        .title("Devices")
                        .description("Monitor and control all your devices from the app")
                        .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.fourth_slide_background)
                .buttonsColor(R.color.fourth_slide_buttons)
                .image(R.drawable.screnarios_img)
                .title("Scenarios")
                .description("Design and Apply scenarios to control action flow of your smart devices")
                .build());
    }

    @Override
    public void onFinish() {
        super.onFinish();
        setOnboardingStatus();
    }

    private boolean getOnboardingStatus() {
        SharedPreferences sharedPrefs = this.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(ONBOARDING_KEY, false);
    }

    private void setOnboardingStatus() {
        SharedPreferences sharedPrefs = this.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(ONBOARDING_KEY, true);
        editor.commit();
    }
}
