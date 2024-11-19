package com.example.barberbuzz

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.avitrek.FeedbackData
import com.example.barberbuzz.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Feedback : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etFeedback: EditText
    private lateinit var btnSubmit: Button
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE)

        // Initialize UI elements
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etFeedback = findViewById(R.id.etFeedback)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Set onClickListener for the submit button
        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val feedback = etFeedback.text.toString().trim()

            // Validate input
            if (name.isEmpty() || email.isEmpty() || feedback.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else {
                submitFeedback(name, email, feedback)
            }
        }
    }

    // Function to submit feedback to Firebase under the username node
    private fun submitFeedback(name: String, email: String, feedback: String) {
        // Get the stored username from SharedPreferences
        val username = sharedPreferences.getString("username", null)

        if (username != null) {
            // Generate a unique key for the feedback
            val feedbackId = database.child("feedback").child(username).push().key

            // Create a feedback object
            val feedbackData = FeedbackData(name, email, feedback)

            // Save feedback to Firebase under the feedback/{username}/{feedback_id} node
            feedbackId?.let {
                database.child("feedback").child(username).child(it).setValue(feedbackData)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                            clearFields()  // Optionally clear input fields after submission
                        } else {
                            Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Clear the input fields after feedback submission
    private fun clearFields() {
        etName.text.clear()
        etEmail.text.clear()
        etFeedback.text.clear()
    }
}


