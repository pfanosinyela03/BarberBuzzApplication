package com.example.barberbuzz

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.barberbuzz.CartActivity
import com.example.barberbuzz.Product
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CartActivityTest {

    private lateinit var cartActivity: CartActivity

    @Before
    fun setUp() {
        // Initialize CartActivity and mock Looper.myLooper()
        Mockito.mockStatic(Looper::class.java)
        Mockito.`when`(Looper.myLooper()).thenReturn(Looper.getMainLooper())

        // Initialize the CartActivity instance
        cartActivity = CartActivity()



        // Initialize any mocks if needed (this could be added later for other tests)
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testCalculateTotalPrice() {
        // Prepare some sample cart items
        val product1 = Product("Shampoo", "R50") // Assuming Product constructor accepts price as String
        val product2 = Product("Conditioner", "R80")
        cartActivity.cartItems.clear()
        cartActivity.cartItems.addAll(listOf(product1, product2))

        // Call the method to calculate total price
        val totalPrice = cartActivity.calculateTotalPrice()

        // Expected total price = (50 * 2) + (80 * 1) = 180
        assertEquals(180.0, totalPrice, 0.0)
    }

    @Test
    fun testCalculateTotalPriceAndItems() {
        // Prepare some sample cart items
        val product1 = Product("Shampoo", "R50") // Assuming Product constructor accepts price as String
        val product2 = Product("Conditioner", "R80")
        cartActivity.cartItems.clear()
        cartActivity.cartItems.addAll(listOf(product1, product2))

        // Mock the totalTextView text value for comparison
        val totalTextView = cartActivity.totalTextView

        // Call the method to calculate total price and items
        cartActivity.calculateTotalPriceAndItems()

        // Expected total = "Total: R180.0\nItems: 3"
        assertEquals("Total: R180.0\nItems: 3", totalTextView.text.toString())
    }
}
