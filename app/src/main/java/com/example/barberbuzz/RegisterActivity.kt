package com.example.barberbuzz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.barberbuzz.databinding.ActivityRegisterBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)



        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users").child("Clients") // Reference to Clients node

        binding.registerBtn.setOnClickListener {
            val signupUsername = binding.regemail.text.toString()
            val signupPassword = binding.regpassword.text.toString()
            val confirmPassword = binding.confirmpassword.text.toString()
            val fullName = binding.name.text.toString()
            val email = binding.useremail.text.toString() // Get email input

            if (signupUsername.isNotEmpty() && signupPassword.isNotEmpty() && confirmPassword.isNotEmpty() && fullName.isNotEmpty() && email.isNotEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // Check for valid email format
                    if (signupPassword.length < 8) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Password must be at least 8 characters",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (signupPassword == confirmPassword) {
                        signupUser(signupUsername, signupPassword, fullName, email) // Pass email
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Passwords do not match",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please enter a valid email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@RegisterActivity,
                    "All fields are mandatory",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.logredirect.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
    }

    // User registration
    private fun signupUser(username: String, password: String, fullName: String, email: String) {
        databaseReference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    val userData = UserData(username, password, fullName, email) // Use username as key
                    databaseReference.child(username).setValue(userData) // Store data under the username
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registered Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Username not available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Database Error: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
