package com.example.barberbuzz

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Check if the user is already logged in
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        if (username != null) {
            // User is already logged in, redirect to MainActivity
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
            return // Exit the onCreate method
        }

        val regButton = findViewById<Button>(R.id.button2)
        val logButton = findViewById<Button>(R.id.button)

        regButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish() // Optional: close SplashActivity after starting the next activity
        }

        logButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Optional: close SplashActivity after starting the next activity
        }
    }
}
