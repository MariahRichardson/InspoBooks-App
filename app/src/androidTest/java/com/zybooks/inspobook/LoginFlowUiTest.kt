package com.zybooks.inspobook

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFlowUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun loginNavigatesToInspoBooks() {
        // type username
        onView(withId(R.id.editTextId)).perform(
            click(), typeText("test@test.com"), closeSoftKeyboard())

        // type password
        onView(withId(R.id.editTextPassword)).perform(
            click(), typeText("test123"), closeSoftKeyboard())

        // click login button
        onView(withId(R.id.buttonLogin)).perform(click())

        Thread.sleep(4000)

        // check InspoBooksFragment loaded
        onView(withId(R.id.inspoBookRecyclerView))
            .check(matches(isDisplayed()))
    }
}
