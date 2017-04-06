package com.socioty.smartik;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by yesha on 2017-03-20.
 */

@RunWith(AndroidJUnit4.class)

public class NavigationEspressoTest {

    @Rule
    public ActivityTestRule<ControlPanelActivity> mActivityRule = new ActivityTestRule(ControlPanelActivity.class);

    @Test
    public void navigation() {
        onView(withId(R.id.frame_layout)).check(matches(isDisplayed()));

        //Checks for the Bottom Navigation Bar
        onView(withId(R.id.navigation)).check(matches(isDisplayed()));

        //Checks for the menu items
        onView(withId(R.id.rooms_action_item)).check(matches(isDisplayed()));

        //Checks for the menu item clickable
        onView(withId(R.id.status_action_item)).perform(click());


        onView(withId(R.id.scenarios_action_item)).perform(click());

        onView(withId(R.id.devices_action_item)).check(matches(isDisplayed()));
        //onView(withId(R.id.logout)).check(matches(isDisplayed()));
    }
}
