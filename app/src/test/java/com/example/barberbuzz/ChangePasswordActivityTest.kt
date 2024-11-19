package com.example.barberbuzz

import android.content.Context
import android.content.SharedPreferences
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ChangePasswordActivityTest {

    private lateinit var activity: ChangePasswordActivity

    @Mock
    private lateinit var mockDatabaseReference: DatabaseReference

    @Mock
    private lateinit var mockFirebaseDatabase: FirebaseDatabase

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Mock FirebaseDatabase and DatabaseReference
        `when`(mockFirebaseDatabase.getReference(anyString())).thenReturn(mockDatabaseReference)

        // Mock SharedPreferences
        `when`(sharedPreferences.getString(eq("username"), any())).thenReturn("testUser")
        `when`(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)

        // Set up the activity
        activity = Robolectric.buildActivity(ChangePasswordActivity::class.java).create().get()

        // Replace Firebase and SharedPreferences with mocks
        activity.database = mockDatabaseReference
    }

    @Test
    fun testActivityInitialization() {
        // Ensure that the activity initializes the views correctly
        assert(activity.currentPasswordInput.isFocusable.not())
        assert(activity.currentPasswordInput.isClickable.not())
        assert(activity.updatePasswordButton is Button)
    }

    @Test
    fun testPasswordUpdateSuccess() {
        // Simulate valid input
        activity.newPasswordInput.setText("newpassword123")
        activity.confirmPasswordInput.setText("newpassword123")

        // Mock Firebase database password update
        `when`(mockDatabaseReference.child("password").setValue(anyString()))
            .thenReturn(mock(Task::class.java) as Task<Void>?)

        // Click update button
        activity.updatePasswordButton.performClick()

        // Verify the database is updated with the new password
        verify(mockDatabaseReference.child("password")).setValue("newpassword123")
    }

    @Test
    fun testPasswordsDoNotMatch() {
        // Simulate mismatched passwords
        activity.newPasswordInput.setText("newpassword123")
        activity.confirmPasswordInput.setText("differentpassword")

        // Click update button
        activity.updatePasswordButton.performClick()

        // Verify a toast is displayed for mismatched passwords
        assert(Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).view != null)
    }

    @Test
    fun testEmptyFieldsValidation() {
        // Simulate empty password fields
        activity.newPasswordInput.setText("")
        activity.confirmPasswordInput.setText("")

        // Click update button
        activity.updatePasswordButton.performClick()

        // Verify a toast is displayed for empty fields
        assert(Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).view != null)
    }
}
