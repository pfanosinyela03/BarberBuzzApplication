package com.example.barberbuzz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.barberbuzz.databinding.ActivityForgotPass1Binding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ForgotPass1 : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPass1Binding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPass1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users").child("Clients")

        // Set button click listener for submitting username
        binding.subBtn.setOnClickListener {
            val username = binding.forgotpassemail.text.toString()

            if (username.isNotEmpty()) {
                // Call function to verify if the username exists
                verifyUsername(username)
            } else {
                Toast.makeText(this@ForgotPass1, "Username is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to verify if the username exists in the database
    private fun verifyUsername(username: String) {
        databaseReference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // If username exists, redirect to ResetPassword activity
                    Toast.makeText(this@ForgotPass1, "Username verified", Toast.LENGTH_SHORT).show()

                    // Pass the username to ResetPassword activity
                    val intent = Intent(this@ForgotPass1, ResetPass::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish()
                } else {
                    // If username does not exist, notify the user
                    Toast.makeText(this@ForgotPass1, "Username not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Toast.makeText(this@ForgotPass1, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
