package com.example.barberbuzz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val cardUpdateUsername = findViewById<CardView>(R.id.cardUpdateUsername)
        val cardUpdateEmail = findViewById<CardView>(R.id.cardUpdateEmail)
        val cardChangePassword = findViewById<CardView>(R.id.cardChangePassword)

        // Set OnClickListeners to navigate to respective activities
        cardUpdateUsername.setOnClickListener {
            val intent = Intent(this, UpdateUsernameActivity::class.java)
            startActivity(intent)
        }

        cardUpdateEmail.setOnClickListener {
            val intent = Intent(this, UpdateEmailActivity::class.java)
            startActivity(intent)
        }

        cardChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
    }
}
