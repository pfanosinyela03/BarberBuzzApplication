package com.example.barberbuzz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private val cartItems: MutableList<Product>,
    private val onRemoveFromCart: (Product) -> Unit,
    private val onQuantityChanged: (Int) -> Unit // Callback for quantity changes
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val quantityTextView: TextView = view.findViewById(R.id.quantityTextView)
        val increaseQuantityButton: Button = view.findViewById(R.id.increaseQuantityButton)
        val decreaseQuantityButton: Button = view.findViewById(R.id.decreaseQuantityButton)
        val removeProductButton: ImageView = view.findViewById(R.id.removeProductButton)

        init {
            // Increase quantity button
            increaseQuantityButton.setOnClickListener {
                val quantity = quantityTextView.text.toString().toInt()
                quantityTextView.text = (quantity + 1).toString()
                // Update the quantity in the product object
                val product = cartItems[adapterPosition]
                product.quantity = quantity + 1

                // Notify quantity change
                onQuantityChanged(cartItems.sumOf { it.quantity })
            }

            // Decrease quantity button
            decreaseQuantityButton.setOnClickListener {
                val quantity = quantityTextView.text.toString().toInt()
                if (quantity > 1) {
                    quantityTextView.text = (quantity - 1).toString()
                    // Update the quantity in the product object
                    val product = cartItems[adapterPosition]
                    product.quantity = quantity - 1

                    // Notify quantity change
                    onQuantityChanged(cartItems.sumOf { it.quantity })
                }
            }

            // Remove product button
            removeProductButton.setOnClickListener {
                val product = cartItems[adapterPosition]
                onRemoveFromCart(product) // Notify the listener to remove the item
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = cartItems[position]

        // Set product details
        holder.productName.text = product.name ?: "No name available"
        holder.productPrice.text = product.price.toString()
        holder.quantityTextView.text = product.quantity.toString()

        // Load image using Glide
        Glide.with(holder.productImage.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.productImage)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }
}
