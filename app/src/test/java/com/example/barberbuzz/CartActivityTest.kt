package com.example.barberbuzz


import android.view.ViewGroup
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class CartActivityTest {

    @Mock
    lateinit var mockView: ViewGroup

    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartItems: MutableList<Product>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        // Initialize test data
        cartItems = mutableListOf(
            Product("Product 1", "R50.00", "image_url_1", 1),
            Product("Product 2", "R30.00", "image_url_2", 2)
        )

        cartAdapter = CartAdapter(cartItems, { product -> }, { count -> })
    }

    @Test
    fun testIncreaseQuantity() {
        // Get the ViewHolder and trigger the quantity increase
        val viewHolder = cartAdapter.onCreateViewHolder(mockView, 0)

        // Set up mock view
        viewHolder.quantityTextView.text = "1"
        viewHolder.increaseQuantityButton.performClick()

        // Check if quantity increased to 2
        assertEquals("2", viewHolder.quantityTextView.text.toString())
    }

    @Test
    fun testDecreaseQuantity() {
        // Get the ViewHolder and trigger the quantity decrease
        val viewHolder = cartAdapter.onCreateViewHolder(mockView, 0)

        // Set up mock view
        viewHolder.quantityTextView.text = "2"
        viewHolder.decreaseQuantityButton.performClick()

        // Check if quantity decreased to 1
        assertEquals("1", viewHolder.quantityTextView.text.toString())
    }

    @Test
    fun testRemoveProduct() {
        // Set up mock view
        val viewHolder = cartAdapter.onCreateViewHolder(mockView, 0)
        val product = cartItems[0]

        // Trigger the remove button
        viewHolder.removeProductButton.performClick()


    }
}
