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
        // Initialize Mockito
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testUsernameAlreadyExists() {
        // Mock Firebase database and reference
        val mockDatabaseReference = mock(DatabaseReference::class.java)
        val mockDataSnapshot = mock(DataSnapshot::class.java)
        val mockValueEventListener = argumentCaptor<ValueEventListener>()

        // Stub methods for database reference
        `when`(mockDatabaseReference.child("testUsername")).thenReturn(mockDatabaseReference)

        // Simulate a case where the username already exists
        `when`(mockDataSnapshot.exists()).thenReturn(true)


        doAnswer {
            val listener = it.arguments[0] as ValueEventListener
            listener.onDataChange(mockDataSnapshot)
            null
        }.`when`(mockDatabaseReference).addListenerForSingleValueEvent(mockValueEventListener.capture())

        // Perform the test
        var usernameExists = false
        mockDatabaseReference.child("testUsername").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usernameExists = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {
                // No-op for this test
            }
        })

        // Verify
        assertTrue(usernameExists, "Username should already exist in the database")
    }

    @Test
    fun testPasswordsMatch() {
        val password = "password123"
        val confirmPassword = "password123"
        assertEquals(password, confirmPassword)
    }

    @Test
    fun testPasswordsDoNotMatch() {
        val password = "password123"
        val confirmPassword = "differentPassword"
        assertNotEquals(password, confirmPassword)
    }

    @Test
    fun testPasswordLength() {
        val shortPassword = "short"
        val validPassword = "validPassword123"
        assertFalse(shortPassword.length >= 8)
        assertTrue(validPassword.length >= 8)
    }


}
