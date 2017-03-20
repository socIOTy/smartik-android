package com.socioty.smartik;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by yesha on 2017-03-15.
 */
@RunWith(AndroidJUnit4.class)

public class LoginActivityEspressoTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule(LoginActivity.class);

    @Test
    public void listGoesOverTheFold() {
        onView(withId(R.id.btn)).check(matches(isDisplayed()));

        onView(withId(R.id.btn)).check(matches(withText("login")));
    }


}
