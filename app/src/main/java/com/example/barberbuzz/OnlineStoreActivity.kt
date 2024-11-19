package com.example.barberbuzz


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils

class OnlineStoreActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var database: DatabaseReference
    private lateinit var toolbar: Toolbar
    private var cartItems: MutableList<Product> = mutableListOf()
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_store)

        // Initialize the toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Shop Online"
        

        // Get the username from SharedPreferences
        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "") ?: ""

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        productList = mutableListOf()

        // Set adapter
        productAdapter = ProductAdapter(productList) { product ->
            addToCart(product)
        }
        recyclerView.adapter = productAdapter

        // Initialize Firebase Database reference to "products" node
        database = FirebaseDatabase.getInstance().getReference("products")

        // Fetch products from Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let { productList.add(it.copy()) }
                }
                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OnlineStoreActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
            }
        })

        // Load the user's cart from Firebase and update badge count
        if (username.isNotEmpty()) {
            loadCartFromDatabase()
        }
    }

    // Load cart items from the Firebase database
    private fun loadCartFromDatabase() {
        // Change reference to "cart" node
        val cartRef = FirebaseDatabase.getInstance().getReference("cart").child(username)
        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItems.clear()
                var totalItemCount = 0
                for (cartSnapshot in snapshot.children) {
                    val product = cartSnapshot.getValue(Product::class.java)
                    product?.let {
                        cartItems.add(it)
                        totalItemCount += it.quantity // Summing the quantities of items in the cart
                    }
                }
                updateCartItemCount(totalItemCount) // Update the badge count
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OnlineStoreActivity, "Failed to load cart", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Add the product to the cart and save to Firebase
    private fun addToCart(product: Product) {
        val existingItem = cartItems.find { it.name == product.name }
        if (existingItem != null) {
            existingItem.quantity += 1 // Increase quantity if already in cart
        } else {
            cartItems.add(product.copy(quantity = 1)) // Add new item with quantity 1
        }
        updateCartInDatabase() // Update cart in the database
    }

    // Update the cart in the Firebase database under "cart" node
    private fun updateCartInDatabase() {
        if (username.isNotEmpty()) {
            val cartRef = FirebaseDatabase.getInstance().getReference("cart").child(username)
            cartRef.setValue(cartItems)
        }
    }

    // Update cart item count badge
    @OptIn(ExperimentalBadgeUtils::class)
    private fun updateCartItemCount(count: Int) {
        val badge: BadgeDrawable = BadgeDrawable.create(this)
        badge.number = count // Set the count of items in the cart from Firebase
        BadgeUtils.attachBadgeDrawable(badge, toolbar, R.id.action_cart)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                val intent = Intent(this, CartActivity::class.java)
                intent.putExtra("cartItems", ArrayList(cartItems)) // Pass cart items
                startActivityForResult(intent, 1001)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // Save cart items before going back
        updateCartInDatabase()
        super.onBackPressed()
    }
}
