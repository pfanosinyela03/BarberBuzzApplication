package com.example.barberbuzz

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class CartActivityTest {

    private lateinit var cartItems: MutableList<Product>
    private lateinit var mockRemoveCallback: (Product) -> Unit
    private lateinit var mockQuantityCallback: (Int) -> Unit
    private lateinit var cartAdapter: CartAdapter

    @Before
    fun setUp() {
        // Set up sample cart items
        cartItems = mutableListOf(
            Product("1", "Product 1", "url1", 1),
            Product("2", "Product 2", "url2", 2)
        )

        // Mock callbacks
        mockRemoveCallback = mock<(Product) -> Unit>()
        mockQuantityCallback = mock<(Int) -> Unit>()

        // Create the CartAdapter instance
        cartAdapter = CartAdapter(cartItems, mockRemoveCallback, mockQuantityCallback)
    }

    @Test
    fun `test item count matches cart size`() {
        // Verify the adapter returns the correct number of items
        assertEquals(2, cartAdapter.itemCount)
    }

    @Test
    fun `test remove item callback is invoked`() {
        // Simulate removing the first item
        val productToRemove = cartItems[0]
        cartAdapter.onRemoveFromCart(productToRemove)

        // Verify the callback is triggered with the correct product
        verify(mockRemoveCallback).invoke(productToRemove)
    }
}
