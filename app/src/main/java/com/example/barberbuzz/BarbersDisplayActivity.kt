package com.example.barberbuzz

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue



class BarbersDisplayActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var barberAdapter: BarberAdapter
    private lateinit var barberReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barbers_display)



        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firebase reference for barbers
        barberReference = FirebaseDatabase.getInstance().getReference("users/Barbers")

        // Initialize adapter and set to RecyclerView
        barberAdapter = BarberAdapter(this, mutableListOf(), barberReference)
        recyclerView.adapter = barberAdapter

        // Retrieve and display barbers from Firebase
        retrieveBarbers()
    }



    private fun retrieveBarbers() {
        barberReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val barberList = mutableListOf<Barber>()
                for (barberSnapshot in dataSnapshot.children) {

                    val barber = barberSnapshot.getValue<Barber>()
                    if (barber != null && !barber.barberName.isNullOrEmpty()) {
                        barberList.add(barber)
                    }
                }
                barberAdapter.updateBarberList(barberList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@BarbersDisplayActivity, "Error loading barbers", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
