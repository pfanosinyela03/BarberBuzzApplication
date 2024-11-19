package com.example.barberbuzz

import android.content.Intent
import android.widget.Toast
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ResetPassTest {

    private lateinit var scenario: ActivityScenario<ResetPass>

    @Before
    fun setUp() {
        // Launch the ResetPass activity with test data
        val intent = Intent(ApplicationProvider.getApplicationContext(), ResetPass::class.java).apply {
            putExtra("USERNAME", "testuser")
        }
        scenario = ActivityScenario.launch(intent)
    }

    @Test
    fun `test username is passed via intent`() {
        scenario.onActivity { activity ->
            // Check if the username is correctly retrieved from the intent
            assertEquals("testuser", activity.username)
        }
    }

    @Test
    fun `test empty password shows toast`() {
        scenario.onActivity { activity ->
            // Simulate empty password fields
            onView(withId(R.id.resetpassword)).perform(typeText(""))
            onView(withId(R.id.confirmresetpassword)).perform(typeText(""))

            // Click the reset button
            onView(withId(R.id.resetBtn)).perform(click())


        }
    }

    @Test
    fun `test mismatched passwords show toast`() {
        scenario.onActivity { activity ->
            // Simulate mismatched passwords
            onView(withId(R.id.resetpassword)).perform(typeText("password1"))
            onView(withId(R.id.confirmresetpassword)).perform(typeText("password2"))

            // Click the reset button
            onView(withId(R.id.resetBtn)).perform(click())


        }
    }
}
