package com.example.barberbuzz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HairstylesActivity : AppCompatActivity() {

    private lateinit var hairstylesRecyclerView: RecyclerView
    private lateinit var hairstyleAdapter: HairstyleAdapter
    private val hairstylesList = mutableListOf<Hairstyle>()
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hairstyles)

        // Initialize RecyclerView
        hairstylesRecyclerView = findViewById(R.id.hairstylesRecyclerView)
        hairstylesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("hairstyles")



        // Fetch hairstyles from Firebase
        fetchHairstyles()
    }



    private fun fetchHairstyles() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                hairstylesList.clear() // Clear the list before adding new data
                for (hairstyleSnapshot in dataSnapshot.children) {
                    val hairstyle = hairstyleSnapshot.getValue(Hairstyle::class.java)
                    hairstyle?.let { hairstylesList.add(it) }
                }

                // Set up the adapter with a click listener for each item
                hairstyleAdapter = HairstyleAdapter(hairstylesList) { selectedHairstyle ->
                    // When a hairstyle is clicked, navigate to BookAppointmentActivity
                    val intent = Intent(this@HairstylesActivity, BookAppointmentActivity::class.java).apply {
                        putExtra("hairstyle_name", selectedHairstyle.name)
                        putExtra("hairstyle_duration", selectedHairstyle.duration)
                        putExtra("hairstyle_price", selectedHairstyle.price)
                        putExtra("hairstyle_image_url", selectedHairstyle.imageUrl)
                    }
                    startActivity(intent)
                }

                hairstylesRecyclerView.adapter = hairstyleAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("HairstylesActivity", "Database error: ${databaseError.message}")
            }
        })
    }
}
