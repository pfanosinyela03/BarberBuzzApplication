package com.example.barberbuzz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class OrdersActivity : AppCompatActivity() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private val ordersList: MutableList<List<Product>> = mutableListOf() // Store orders
    private lateinit var database: DatabaseReference
    private lateinit var username: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        // Initialize RecyclerView
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)

        // Get the logged-in username and email from SharedPreferences
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "") ?: ""




        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("orders")

        setupToolbar()

        // Load orders from Firebase
        loadOrders()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false) // Disable default title

        }


    }

    // Handle back button action to navigate to ShopOnline
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, OnlineStoreActivity::class.java))
                finish() // Close current activity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadOrders() {
        if (username.isNotEmpty()) {
            database.child(username).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ordersList = mutableListOf<Order>() // Updated to store Order objects
                    for (orderSnapshot in snapshot.children) {
                        // Create a list to hold products for this order
                        val orderItems = mutableListOf<Product>()

                        // Retrieve order ID and status
                        val orderId = orderSnapshot.child("orderId").getValue(String::class.java) ?: ""
                        val status = orderSnapshot.child("status").getValue(String::class.java) ?: ""

                        // Retrieve each product in the order
                        orderSnapshot.child("items").children.forEach { productSnapshot ->
                            val product = productSnapshot.getValue(Product::class.java)
                            if (product != null) {
                                orderItems.add(product) // Add each product to the order items list
                            }
                        }

                        // Create an Order object and add it to the ordersList
                        ordersList.add(Order(orderItems, orderId, status))
                    }
                    // Set adapter with the updated ordersList
                    ordersAdapter = OrdersAdapter(ordersList)
                    ordersRecyclerView.adapter = ordersAdapter
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }


}
