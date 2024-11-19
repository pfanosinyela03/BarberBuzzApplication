package com.example.barberbuzz

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ChangePasswordActivity : AppCompatActivity() {

    lateinit var database: DatabaseReference
    lateinit var currentPasswordInput: EditText
    lateinit var newPasswordInput: EditText
    lateinit var confirmPasswordInput: EditText
    lateinit var updatePasswordButton: Button
    private var currentPassword: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        currentPasswordInput = findViewById(R.id.currentPasswordInput)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        updatePasswordButton = findViewById(R.id.updatePasswordButton)

        // Ensure currentPasswordInput is non-editable
        currentPasswordInput.isFocusable = false
        currentPasswordInput.isClickable = false

        // Get username from SharedPreferences
        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)

        if (username != null) {
            // Initialize Firebase Database reference to users -> Clients -> username
            database = FirebaseDatabase.getInstance().getReference("users").child("Clients").child(username)

            // Retrieve current password from database
            database.child("password").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentPassword = snapshot.getValue(String::class.java)
                    currentPasswordInput.setText(currentPassword)  // Display current password
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChangePasswordActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                }
            })

            // Set onClickListener for Update Password Button
            updatePasswordButton.setOnClickListener {
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                // Validate inputs
                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else if (newPassword.length < 8) {
                    Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                } else if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    // Update password in the database
                    database.child("password").setValue(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }
    }
}
