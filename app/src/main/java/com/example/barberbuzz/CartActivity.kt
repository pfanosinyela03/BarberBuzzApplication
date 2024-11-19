package com.example.barberbuzz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlin.random.Random

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    val cartItems: MutableList<Product> = mutableListOf()
    private lateinit var toolbar: Toolbar
    lateinit var database: DatabaseReference
    lateinit var username: String
    lateinit var totalTextView: TextView
    lateinit var email: String // To store the retrieved email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        setupToolbar()

        totalTextView = findViewById(R.id.totalTextView)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val cartItemList = intent.getSerializableExtra("cartItems") as? List<Product> ?: emptyList()
        cartItems.addAll(cartItemList)

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance().getReference("cart")

        // Get the logged-in username from SharedPreferences
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "") ?: ""

        // Set adapter with removal and quantity change functions
        cartAdapter = CartAdapter(cartItems, { product ->
            cartItems.remove(product)
            updateCartInDatabase()
            cartAdapter.notifyDataSetChanged()
            calculateTotalPriceAndItems()
        }) { count ->
            updateCartInDatabase()
            calculateTotalPriceAndItems()
        }

        recyclerView.adapter = cartAdapter

        calculateTotalPriceAndItems()

        val placeOrderButton: Button = findViewById(R.id.placeOrderButton)
        placeOrderButton.setOnClickListener {
            // First, retrieve the email before placing the order
            retrieveUserEmail { email ->
                this.email = email
                placeOrder() // Proceed to place the order once email is retrieved
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, OnlineStoreActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Function to retrieve the email of the logged-in user from Clients under the users node
    fun retrieveUserEmail(callback: (String) -> Unit) {
        val clientRef = FirebaseDatabase.getInstance().getReference("users").child("Clients").child(username)

        clientRef.child("email").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val email = snapshot.getValue(String::class.java) ?: ""
                callback(email) // Pass the retrieved email back
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }

    fun placeOrder() {
        if (cartItems.isNotEmpty()) {
            val ordersDatabase = FirebaseDatabase.getInstance().getReference("orders")
            val orderId = generateUniqueId()
            val orderStatus = "pending"

            // Create an order map with email and order data
            val orderData = mapOf(
                "status" to orderStatus,
                "items" to cartItems,
                "totalPrice" to calculateTotalPrice(),
                "orderId" to orderId,
                "email" to email // Include the user's email in the order details
            )

            ordersDatabase.child(username).child(orderId).setValue(orderData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    removeCartFromDatabase()
                    showOrderNotification(orderId)
                } else {
                    // Handle the error
                }
            }
        } else {
            // Handle the case where the cart is empty
        }
    }

    private fun showOrderNotification(orderId: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "order_notifications"
        val channelName = "Order Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, OrdersActivity::class.java).apply {
            putExtra("orderId", orderId)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            } else {
                createNotification(notificationManager, channelId, orderId, pendingIntent)
            }
        } else {
            createNotification(notificationManager, channelId, orderId, pendingIntent)
        }
    }

    private fun createNotification(notificationManager: NotificationManager, channelId: String, orderId: String, pendingIntent: PendingIntent) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Order Placed")
            .setContentText("Your order $orderId has been placed.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }

    private fun generateUniqueId(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..5).map { characters[Random.nextInt(characters.length)] }.joinToString("")
    }

    private fun removeCartFromDatabase() {
        if (username.isNotEmpty()) {
            database.child(username).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    clearCart()
                } else {
                    // Handle the error
                }
            }
        }
    }

    private fun clearCart() {
        cartItems.clear()
        updateCartInDatabase()
        cartAdapter.notifyDataSetChanged()
        totalTextView.text = "Total: R0\nItems: 0"
    }

    fun calculateTotalPriceAndItems() {
        val totalPrice = cartItems.sumOf {
            val price = it.price?.replace("R", "")?.trim()?.toDoubleOrNull() ?: 0.0
            price * it.quantity
        }
        val totalItems = cartItems.sumOf { it.quantity }

        totalTextView.text = "Total: R$totalPrice\nItems: $totalItems"
    }

    fun calculateTotalPrice(): Double {
        return cartItems.sumOf {
            val price = it.price?.replace("R", "")?.trim()?.toDoubleOrNull() ?: 0.0
            price * it.quantity
        }
    }

    fun updateCartInDatabase() {
        if (username.isNotEmpty()) {
            database.child(username).setValue(cartItems).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successfully updated cart in the database
                }
            }
        }
    }

    override fun onBackPressed() {
        updateCartItemCount()
        super.onBackPressed()
    }

    private fun updateCartItemCount() {
        val count = cartItems.sumOf { it.quantity }
        val resultIntent = Intent()
        resultIntent.putExtra("cartItemCount", count)
        setResult(RESULT_OK, resultIntent)
    }
}
