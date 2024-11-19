package com.example.barberbuzz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView


class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button for appointment
        val appointmentButton = findViewById<CardView>(R.id.BookAppointmentHome)
        appointmentButton.setOnClickListener {
            val intent = Intent(this, HairstylesActivity::class.java)
            startActivity(intent)
        }


        //Button for Barbers
        val BarbersButton = findViewById<CardView>(R.id.BarbersHome)
        BarbersButton.setOnClickListener {
            val intent = Intent(this, BarbersDisplayActivity::class.java)
            startActivity(intent)
        }

        val viwappButton = findViewById<CardView>(R.id.ScheduleHome)
        viwappButton.setOnClickListener {
            val intent = Intent(this, AppointmentDetailsActivity::class.java)
            startActivity(intent)
        }

        val shopButton = findViewById<CardView>(R.id.ShopHome)
        shopButton.setOnClickListener {
            val intent = Intent(this, OnlineStoreActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firebase
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users")

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout)

        // Handle navtabView click to open the drawer
        val navtabView: ImageView = findViewById(R.id.navtabView)
        navtabView.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle navigation menu item clicks
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_appointments -> {
                    startActivity(Intent(this, AppointmentDetailsActivity::class.java))
                }
                R.id.nav_orders -> {
                    startActivity(Intent(this, OrdersActivity::class.java))
                }
                R.id.nav_book_appointment -> {
                    startActivity(Intent(this, HairstylesActivity::class.java))
                }
                R.id.nav_location -> {
                    startActivity(Intent(this, LocationActivity::class.java))
                }
                R.id.nav_feedback -> {
                    startActivity(Intent(this, Feedback::class.java))
                }
                R.id.nav_Settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_logout -> {
                    showLogoutConfirmationDialog()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer after selecting an item
            true
        }

        // Set Full Name and Email in the header
        val headerView: View = navigationView.getHeaderView(0)
        val fullNameTextView: TextView = headerView.findViewById(R.id.header_fullname)
        val emailTextView: TextView = headerView.findViewById(R.id.header_email)
        val headerImageView: ImageView = headerView.findViewById(R.id.header_image)

        // Retrieve username from SharedPreferences
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        // Check if username exists before retrieving data
        if (username != null) {
            retrieveUserData(username, fullNameTextView, emailTextView)
        } else {
            fullNameTextView.text = "User"
            emailTextView.text = "user@example.com"
        }

        // Load profile image from Firebase if it exists
        loadProfileImage(sharedPreferences)

        // Display the username in the TextView
        val usernameTextView: TextView = findViewById(R.id.textView2) // Get the TextView by ID
        usernameTextView.text = username ?: "User" // Set the username text

        // Handle ImageView click to upload a photo
        headerImageView.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun retrieveUserData(username: String, fullNameTextView: TextView, emailTextView: TextView) {
        database.child("Clients").child(username).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val fullName = snapshot.child("fullName").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)

                // Set the retrieved data in the TextViews
                fullNameTextView.text = fullName ?: "User"
                emailTextView.text = email ?: "user@example.com"

                // Optionally, update SharedPreferences if necessary
                val editor = getSharedPreferences("userPrefs", Context.MODE_PRIVATE).edit()
                editor.putString("fullName", fullName)
                editor.putString("email", email)
                editor.apply()
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImage(sharedPreferences: SharedPreferences) {
        val username = sharedPreferences.getString("username", null) // Use correct key
        username?.let {
            database.child("Clients").child(it).child("profileImageUrl").get().addOnSuccessListener { snapshot ->
                val imageUrl = snapshot.getValue(String::class.java)
                if (imageUrl != null) {
                    Glide.with(this)
                        .load(imageUrl) // Load the image from the URL
                        .circleCrop() // Apply circular crop transformation
                        .into(findViewById(R.id.header_image)) // Load into the ImageView
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data

            imageUri?.let {
                // Upload image to Firebase Storage
                uploadImageToFirebase(it)
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null) // Use correct key

        if (username != null) {
            val storageRef = storage.reference.child("profileImages/$username.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    // Get the image URL from Firebase Storage
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveImageUrlToDatabase(username, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUrlToDatabase(username: String, imageUrl: String) {
        database.child("Clients").child(username).child("profileImageUrl").setValue(imageUrl).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                loadProfileImage(getSharedPreferences("userPrefs", Context.MODE_PRIVATE)) // Reload the image
            } else {
                Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->
            // Perform logout operation here
            logoutUser()
        }
        builder.setNegativeButton("No") { dialog: DialogInterface, which: Int ->
            dialog.dismiss() // Dismiss the dialog
        }

        val dialog = builder.create()
        dialog.show()
    }

    // Method to handle user logout
    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear() // Clear user data
            apply()
        }

        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent) // Redirect to splash screen
        finish() // Close the current activity
    }
}
