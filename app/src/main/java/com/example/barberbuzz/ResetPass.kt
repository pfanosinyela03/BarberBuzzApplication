package com.example.barberbuzz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.barberbuzz.databinding.ActivityResetPassBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResetPass : AppCompatActivity() {

    lateinit var binding: ActivityResetPassBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the passed username from ForgotPass1 activity
        username = intent.getStringExtra("USERNAME").toString()

        // Initialize Firebase database reference
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users").child("Clients")

        // Set click listener for the reset button
        binding.resetBtn.setOnClickListener {
            val newPassword = binding.resetpassword.text.toString()
            val confirmPassword = binding.confirmresetpassword.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this@ResetPass, "Both fields are required", Toast.LENGTH_SHORT).show()
            } else if (newPassword.length < 8) {
                Toast.makeText(this@ResetPass, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(this@ResetPass, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // If validation passes, update the password in the database
                resetUserPassword(username, newPassword)
            }
        }
    }

    fun resetUserPassword(username: String, newPassword: String) {
        // Directly access the username node under Clients
        databaseReference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Update the password
                    dataSnapshot.ref.child("password").setValue(newPassword)
                    Toast.makeText(this@ResetPass, "Password reset successfully", Toast.LENGTH_SHORT).show()

                    // Navigate to LoginActivity
                    val intent = Intent(this@ResetPass, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@ResetPass, "User not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ResetPass, "Password reset failed: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
