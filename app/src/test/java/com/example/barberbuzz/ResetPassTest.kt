package com.example.barberbuzz

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class ResetPassTest {

    @Mock
    private lateinit var mockDatabaseReference: DatabaseReference

    @Mock
    private lateinit var mockDataSnapshot: DataSnapshot

    private lateinit var resetPass: ResetPass

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Initialize activity with a mock database reference
        resetPass = ResetPass().apply {
            databaseReference = mockDatabaseReference
        }
    }

    @Test
    fun `test resetUserPassword updates password successfully`() {
        // Arrange
        val username = "testUser"
        val newPassword = "newPassword123"

        // Simulate user exists in Firebase
        Mockito.`when`(mockDataSnapshot.exists()).thenReturn(true)
        Mockito.`when`(mockDataSnapshot.ref).thenReturn(mockDatabaseReference)

        // Simulate Firebase callback
        Mockito.doAnswer { invocation ->
            val listener = invocation.getArgument<DatabaseReference.CompletionListener>(1)
            listener.onComplete(null, mockDatabaseReference)
            null
        }.`when`(mockDatabaseReference).setValue(Mockito.eq(newPassword), Mockito.any())

        // Act
        resetPass.resetUserPassword(username, newPassword)

        // Assert
        Mockito.verify(mockDatabaseReference).setValue(newPassword)
    }

    @Test
    fun `test resetUserPassword shows error for non-existent user`() {
        // Arrange
        val username = "nonExistentUser"
        val newPassword = "newPassword123"

        // Simulate user does not exist in Firebase
        Mockito.`when`(mockDataSnapshot.exists()).thenReturn(false)

        // Act
        resetPass.resetUserPassword(username, newPassword)

        // Assert
        Mockito.verify(mockDatabaseReference, Mockito.never()).setValue(Mockito.anyString())
    }
}

