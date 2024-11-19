package com.example.barberbuzz

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.barberbuzz.databinding.ActivityLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var clientsReference: DatabaseReference
    private lateinit var adminReference: DatabaseReference
    private lateinit var barberReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        clientsReference = firebaseDatabase.reference.child("users").child("Clients") // Reference to Clients node




        binding.loginBtn.setOnClickListener {
            val loginUsername = binding.email.text.toString()
            val loginPassword = binding.password.text.toString()

            if (loginUsername.isNotEmpty() && loginPassword.isNotEmpty()) {
                // Regular user login
                loginUser(loginUsername, loginPassword)
            } else {
                Toast.makeText(this@LoginActivity, "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        binding.regredirect.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }

        binding.forgotpass.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPass1::class.java))
            finish()
        }
    }

    // User Login
    private fun loginUser(username: String, password: String) {
        clientsReference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.getValue(UserData::class.java)

                    if (userData != null && userData.password == password) {
                        // Login successful
                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()

                        // Store username, full name, and email in SharedPreferences
                        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("username", username) // Store username
                        editor.putString("fullName", userData.fullName) // Store full name
                        editor.putString("email", userData.email) // Store email
                        editor.apply()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
