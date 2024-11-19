package com.example.barberbuzz

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UpdateEmailActivity : AppCompatActivity() {

    private lateinit var currentEmailInput: EditText
    private lateinit var newEmailInput: EditText
    private lateinit var updateButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_email)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Find EditTexts and Button
        currentEmailInput = findViewById(R.id.currentEmailInput)
        newEmailInput = findViewById(R.id.newEmailInput)
        updateButton = findViewById(R.id.updateButton)

        // Get username from SharedPreferences
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        currentUsername = sharedPreferences.getString("username", "") ?: ""

        // Retrieve current email from database
        retrieveCurrentEmail()

        // Disable editing for the current email input
        currentEmailInput.isEnabled = false // Disable editing

        updateButton.setOnClickListener {
            val newEmail = newEmailInput.text.toString()

            // Check if new email is not empty
            if (newEmail.isNotEmpty()) {
                updateEmailInDatabase(newEmail)
            } else {
                Toast.makeText(this, "Please enter a new email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retrieveCurrentEmail() {
        // Reference to the current user's email path
        val emailRef = database.child("users").child("Clients").child(currentUsername).child("email")

        emailRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val currentEmail = dataSnapshot.getValue(String::class.java)
                    currentEmailInput.setText(currentEmail) // Set the current email in the EditText
                } else {
                    Toast.makeText(this@UpdateEmailActivity, "Email not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UpdateEmailActivity, "Failed to retrieve email: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateEmailInDatabase(newEmail: String) {
        // Reference to the current user's path
        val userRef = database.child("users").child("Clients").child(currentUsername)

        // Update the email
        userRef.child("email").setValue(newEmail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Update email in SharedPreferences
                val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("email", newEmail) // Update email
                editor.apply()

                Toast.makeText(this, "Email updated successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity
            } else {
                Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
