package com.zybooks.inspobook

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class BottomNavigationUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun ensureLoggedIn() {
        // perform login
        try {
            onView(withId(R.id.editTextId)).perform(
                click(),
                replaceText("test@test.com"),
                closeSoftKeyboard()
            )

            onView(withId(R.id.editTextPassword)).perform(
                click(),
                replaceText("test123"),
                closeSoftKeyboard()
            )

            onView(withId(R.id.buttonLogin)).perform(click())

            Thread.sleep(4000)

        } catch (e: NoMatchingViewException) {
            // do nothing
        }
    }

    @Test
    fun bottomNavigationNavigatesBetweenScreens() {

        // go to Image Search
        onView(withId(R.id.search)).perform(click())
        onView(withId(R.id.searchEditText)).check(matches(isDisplayed()))

        // go to Profile
        onView(withId(R.id.profile)).perform(click())
        onView(withId(R.id.username)).check(matches(isDisplayed()))

        // go to Settings
        onView(withId(R.id.settings)).perform(click())
        onView(withText("App Settings")).check(matches(isDisplayed()))

        // go back Home
        onView(withId(R.id.mybooks)).perform(click())
        onView(withId(R.id.inspoBookRecyclerView)).check(matches(isDisplayed()))
    }
}
