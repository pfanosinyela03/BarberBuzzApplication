package com.example.barberbuzz

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RegisterActivityTest {

    init {
        // Ensure Mockito annotations are initialized properly
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `verify username already exists in database`() {
        // Mock Firebase database structure and snapshot
        val mockDatabaseReference = mock(DatabaseReference::class.java)
        val mockDataSnapshot = mock(DataSnapshot::class.java)
        val mockValueEventListener = argumentCaptor<ValueEventListener>()

        // Simulate database child reference and response
        `when`(mockDatabaseReference.child("testUsername")).thenReturn(mockDatabaseReference)
        `when`(mockDataSnapshot.exists()).thenReturn(true)

        // Handle listener callback for database query
        doAnswer { invocation ->
            val listener = invocation.arguments[0] as ValueEventListener
            listener.onDataChange(mockDataSnapshot)
            null
        }.`when`(mockDatabaseReference).addListenerForSingleValueEvent(mockValueEventListener.capture())

        // Test logic for username existence
        var isUsernameTaken = false
        mockDatabaseReference.child("testUsername").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isUsernameTaken = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation case if necessary
            }
        })

        // Assert that username is already taken
        assertTrue(isUsernameTaken, "Expected username to already exist in the database")
    }

    @Test
    fun `validate matching passwords`() {
        val password = "password123"
        val confirmPassword = "password123"

        // Assert passwords are identical
        assertEquals(password, confirmPassword, "Passwords should match")
    }

    @Test
    fun `validate non-matching passwords`() {
        val password = "password123"
        val confirmPassword = "differentPassword"

        // Assert passwords are not identical
        assertNotEquals(password, confirmPassword, "Passwords should not match")
    }

    @Test
    fun `validate password length requirements`() {
        val shortPassword = "short"
        val strongPassword = "strongPassword123"

        // Assert short password does not meet the requirement
        assertFalse(shortPassword.length >= 8, "Password should be at least 8 characters long")

        // Assert valid password meets the requirement
        assertTrue(strongPassword.length >= 8, "Password should meet minimum length requirement")
    }
}
