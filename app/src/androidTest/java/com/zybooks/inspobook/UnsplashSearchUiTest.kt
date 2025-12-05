package com.zybooks.inspobook

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UnsplashSearchUiTest {

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
    fun searchUsingEnterKeyShowsResults() {

        // navigate to search
        onView(withId(R.id.search)).perform(click())

        // type search keyword
        onView(withId(R.id.searchEditText)).perform(
            typeText("cats"),
            closeSoftKeyboard()
        )

        // trigger IME search button
        onView(withId(R.id.searchEditText)).perform(pressImeActionButton())

        Thread.sleep(3000)

        onView(withId(R.id.photosRecyclerView))
            .check(matches(isDisplayed()))
    }
}
