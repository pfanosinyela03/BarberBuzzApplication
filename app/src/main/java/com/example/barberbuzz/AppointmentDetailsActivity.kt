package com.example.barberbuzz

import AppointmentsAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AppointmentDetailsActivity : AppCompatActivity() {

    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private val appointmentsList = mutableListOf<Appointment>()
    private lateinit var adapter: AppointmentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_details)

        setupToolbar()
        initializeRecyclerView()
        initializeFirebaseDatabase()

        val username = getUsernameFromPreferences()

        if (username == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if the user is not logged in
            return
        }

        // Retrieve and display all appointments for the logged-in user
        loadAppointments(username)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false) // Disable default title

        }

        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = "Appointments" // Title is set in XML, but can set it programmatically if needed
    }

    private fun initializeRecyclerView() {
        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)
        appointmentsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up adapter and assign it to RecyclerView
        adapter = AppointmentsAdapter(appointmentsList, this)
        appointmentsRecyclerView.adapter = adapter
    }

    private fun initializeFirebaseDatabase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("appointments")
    }

    private fun getUsernameFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("username", null)
    }



    private fun loadAppointments(username: String) {
        databaseReference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    appointmentsList.clear() // Clear the list before adding new items
                    for (appointmentSnapshot in snapshot.children) {
                        val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                        appointment?.let {
                            it.id = appointmentSnapshot.key // Set the unique ID for each appointment
                            appointmentsList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged() // Notify adapter of data changes
                } else {
                    Toast.makeText(this@AppointmentDetailsActivity, "No appointments for this user.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AppointmentDetailsActivity, "Failed to load appointments: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
