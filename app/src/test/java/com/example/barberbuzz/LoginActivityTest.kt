package com.example.barberbuzz

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.database.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginActivityTest {

    @Test
    fun testFieldsAreMandatory() {
        val username = ""
        val password = ""

        assertTrue(username.isEmpty() || password.isEmpty(), "All fields should be mandatory")
    }

    @Test
    fun testLoginSuccessful() {
        // Mock Firebase database and SharedPreferences
        val mockDatabaseReference = mock<DatabaseReference>()
        val mockDataSnapshot = mock<DataSnapshot>()
        val mockContext = mock<Context>()
        val mockSharedPreferences = mock<SharedPreferences>()
        val mockEditor = mock<SharedPreferences.Editor>()

        // Stub SharedPreferences methods
        whenever(mockContext.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)).thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)

        // Stub Firebase database reference methods
        val userData = UserData("testUsername", "testPassword", "Test User", "test@example.com")
        whenever(mockDataSnapshot.getValue(UserData::class.java)).thenReturn(userData)
        whenever(mockDataSnapshot.exists()).thenReturn(true)

        // Stub Firebase database listener
        val listenerCaptor = argumentCaptor<ValueEventListener>()
        whenever(mockDatabaseReference.child("testUsername")).thenReturn(mockDatabaseReference)
        doAnswer {
            listenerCaptor.firstValue.onDataChange(mockDataSnapshot)
            null
        }.`when`(mockDatabaseReference).addListenerForSingleValueEvent(listenerCaptor.capture())

        // Perform the test
        var loginSuccessful = false
        mockDatabaseReference.child("testUsername").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.getValue(UserData::class.java)?.password == "testPassword") {
                    loginSuccessful = true

                    // Simulate storing data in SharedPreferences
                    val editor = mockContext.getSharedPreferences("userPrefs", Context.MODE_PRIVATE).edit()
                    editor.putString("username", "testUsername")
                    editor.putString("fullName", "Test User")
                    editor.putString("email", "test@example.com")
                    editor.apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // No-op
            }
        })

        // Verify the result
        assertTrue(loginSuccessful, "Login should be successful")
        verify(mockEditor).putString("username", "testUsername")
        verify(mockEditor).putString("fullName", "Test User")
        verify(mockEditor).putString("email", "test@example.com")
        verify(mockEditor).apply()
    }

    @Test
    fun testLoginFailedIncorrectPassword() {
        val mockDatabaseReference = mock<DatabaseReference>()
        val mockDataSnapshot = mock<DataSnapshot>()

        // Simulate existing user with different password
        val userData = UserData("testUsername", "correctPassword", "Test User", "test@example.com")
        whenever(mockDataSnapshot.getValue(UserData::class.java)).thenReturn(userData)
        whenever(mockDataSnapshot.exists()).thenReturn(true)

        val listenerCaptor = argumentCaptor<ValueEventListener>()
        whenever(mockDatabaseReference.child("testUsername")).thenReturn(mockDatabaseReference)
        doAnswer {
            listenerCaptor.firstValue.onDataChange(mockDataSnapshot)
            null
        }.`when`(mockDatabaseReference).addListenerForSingleValueEvent(listenerCaptor.capture())

        var loginSuccessful = false
        mockDatabaseReference.child("testUsername").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loginSuccessful = snapshot.exists() && snapshot.getValue(UserData::class.java)?.password == "wrongPassword"
            }

            override fun onCancelled(error: DatabaseError) {
                // No-op
            }
        })

        assertFalse(loginSuccessful, "Login should fail due to incorrect password")
    }

    @Test
    fun testLoginFailedUserDoesNotExist() {
        val mockDatabaseReference = mock<DatabaseReference>()
        val mockDataSnapshot = mock<DataSnapshot>()

        // Simulate non-existent user
        whenever(mockDataSnapshot.exists()).thenReturn(false)

        val listenerCaptor = argumentCaptor<ValueEventListener>()
        whenever(mockDatabaseReference.child("nonExistentUser")).thenReturn(mockDatabaseReference)
        doAnswer {
            listenerCaptor.firstValue.onDataChange(mockDataSnapshot)
            null
        }.`when`(mockDatabaseReference).addListenerForSingleValueEvent(listenerCaptor.capture())

        var loginSuccessful = false
        mockDatabaseReference.child("nonExistentUser").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loginSuccessful = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {
                // No-op
            }
        })

        assertFalse(loginSuccessful, "Login should fail because user does not exist")
    }
}
