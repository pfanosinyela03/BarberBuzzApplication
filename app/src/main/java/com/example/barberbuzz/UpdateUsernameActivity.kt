package com.example.barberbuzz

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class UpdateUsernameActivity : AppCompatActivity() {

    private lateinit var currentUsernameInput: EditText
    private lateinit var newUsernameInput: EditText
    private lateinit var updateButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_username)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Find EditTexts and Button
        currentUsernameInput = findViewById(R.id.currentUsernameInput)
        newUsernameInput = findViewById(R.id.newUsernameInput)
        updateButton = findViewById(R.id.updateButton)

        // Get username from SharedPreferences
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        currentUsername = sharedPreferences.getString("username", "") ?: ""

        // Set current username to the EditText and disable editing
        currentUsernameInput.setText(currentUsername)
        currentUsernameInput.isEnabled = false // Disable editing

        updateButton.setOnClickListener {
            val newUsername = newUsernameInput.text.toString().trim() // Trim whitespace

            // Check if new username is not empty
            if (newUsername.isNotEmpty()) {
                checkUsernameExists(newUsername)
            } else {
                Toast.makeText(this, "Please enter a new username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUsernameExists(newUsername: String) {
        // Reference to the users/Clients/<newUsername> path
        val usernameRef = database.child("users").child("Clients").child(newUsername)

        usernameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // If the dataSnapshot exists, the username already exists
                if (dataSnapshot.exists()) {
                    Toast.makeText(this@UpdateUsernameActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                } else {
                    // If the username does not exist, update it in the database
                    updateUsernameInDatabase(newUsername)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UpdateUsernameActivity, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUsernameInDatabase(newUsername: String) {
        // Reference to the current user's path
        val currentUserRef = database.child("users").child("Clients").child(currentUsername)

        // Copy existing user data
        currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Check if the current user exists
                if (dataSnapshot.exists()) {
                    // Create a mutable map to hold the user data
                    val userData = dataSnapshot.value as? Map<String, Any> ?: return
                    val updatedUserData = userData.toMutableMap()

                    // Update the username field in the user data
                    updatedUserData["username"] = newUsername

                    // Create a new entry for the new username
                    database.child("users").child("Clients").child(newUsername).setValue(updatedUserData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Remove the old username entry
                                currentUserRef.removeValue().addOnCompleteListener { deleteTask ->
                                    if (deleteTask.isSuccessful) {
                                        // Update SharedPreferences with new username
                                        val editor = getSharedPreferences("userPrefs", Context.MODE_PRIVATE).edit()
                                        editor.putString("username", newUsername) // Update username in SharedPreferences
                                        editor.apply()

                                        Toast.makeText(this@UpdateUsernameActivity, "Username updated successfully", Toast.LENGTH_SHORT).show()
                                        finish() // Close the activity
                                    } else {
                                        Toast.makeText(this@UpdateUsernameActivity, "Failed to delete old username", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this@UpdateUsernameActivity, "Failed to create new username entry", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this@UpdateUsernameActivity, "Current username does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UpdateUsernameActivity, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
