package com.example.barberbuzz

import android.content.Intent
import android.widget.Toast
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.barberbuzz.ResetPass
import com.google.firebase.database.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ResetPassTest {

    @Mock
    private lateinit var mockDatabaseReference: DatabaseReference

    @Mock
    private lateinit var mockDataSnapshot: DataSnapshot

    @Mock
    private lateinit var mockDatabaseError: DatabaseError

    private lateinit var activityScenario: ActivityScenario<ResetPass>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Set up the mock database reference
        `when`(mockDatabaseReference.child(anyString())).thenReturn(mockDatabaseReference)

        // Launch the activity
        activityScenario = ActivityScenario.launch(ResetPass::class.java)
    }

    @Test
    fun testPasswordResetUserNotFound() {
        // Simulate the scenario where the user is not found in the database
        `when`(mockDataSnapshot.exists()).thenReturn(false)

        // Capture the listener being set on the database reference
        val captor = ArgumentCaptor.forClass(ValueEventListener::class.java)
        verify(mockDatabaseReference).addListenerForSingleValueEvent(captor.capture())

        // Simulate a database call that would be triggered in resetUserPassword()
        captor.value.onDataChange(mockDataSnapshot)

        // Verify that the Toast for "User not found" was shown
        // Using a mock to capture Toast (this part will depend on your Toast mocking strategy)
        // For example:
        // verify(mockToast).makeText(any(), eq("User not found"), anyInt())

        // You could assert that a Toast message was triggered or check for the behavior.
        // You can use an Espresso-based test to assert UI elements like Toast.
    }

    @Test
    fun testPasswordResetSuccess() {
        // Simulate the scenario where the user exists and password is updated
        `when`(mockDataSnapshot.exists()).thenReturn(true)

        // Capture the listener being set on the database reference
        val captor = ArgumentCaptor.forClass(ValueEventListener::class.java)
        verify(mockDatabaseReference).addListenerForSingleValueEvent(captor.capture())

        // Simulate a database call that would be triggered in resetUserPassword()
        captor.value.onDataChange(mockDataSnapshot)

        // Verify that the password is updated in the database
        verify(mockDataSnapshot.ref).child("password").setValue("newPassword123")

        // Use onActivity to access the activity and perform any actions or assertions
        activityScenario.onActivity { activity ->
            // Optionally, check that the correct Intent is fired to navigate to LoginActivity
            val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
            verify(activity).startActivity(intentCaptor.capture())

            val capturedIntent = intentCaptor.value
            assertTrue(capturedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
            assertTrue(capturedIntent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0)
        }
    }

    @Test
    fun testPasswordResetFailure() {
        // Simulate a failure in the database call (e.g., network failure)
        `when`(mockDatabaseReference.addListenerForSingleValueEvent(any())).thenAnswer {
            val listener = it.getArgument<ValueEventListener>(0)
            listener.onCancelled(mockDatabaseError)
        }

        // Simulate the reset password call
        activityScenario.onActivity { activity ->
            activity.resetUserPassword("nonexistentUser", "newPassword123")
        }

        // Verify that the Toast for "Password reset failed" was shown
        // You might need to mock Toast here or capture it similarly to other verifications.
    }
}
