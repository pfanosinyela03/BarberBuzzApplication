package com.example.barberbuzz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter(
    private val productList: List<Product>,
    private val onAddToCartClicked: (Product) -> Unit // Listener for add to cart
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val addToCartButton: Button = view.findViewById(R.id.addToCartButton)

        init {
            addToCartButton.setOnClickListener {
                val product = productList[adapterPosition] // Get the product from the list
                onAddToCartClicked(product) // Notify the listener
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Set product details
        holder.productName.text = product.name ?: "No name available"
        holder.productPrice.text = product.price ?: "0"

        // Load image using Glide
        Glide.with(holder.productImage.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.placeholder_image)  // Placeholder image
            .error(R.drawable.error_image)  // Error image if load fails
            .into(holder.productImage)
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
