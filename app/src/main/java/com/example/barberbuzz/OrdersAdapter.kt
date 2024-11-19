package com.example.barberbuzz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrdersAdapter(private val orders: List<Order>) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderDetailsTextView: TextView = view.findViewById(R.id.orderDetailsTextView)
        val totalPriceTextView: TextView = view.findViewById(R.id.totalPriceTextView) // Total price TextView
        val orderIdTextView: TextView = view.findViewById(R.id.orderIdTextView) // Order ID TextView
        val statusTextView: TextView = view.findViewById(R.id.statusTextView) // Status TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val productsInOrder = order.items

        // Prepare order details
        val orderDetails = StringBuilder()
        var totalPrice = 0.0 // Initialize total price for this order

        productsInOrder.forEach { product ->
            val price = product.price?.replace("R", "")?.trim()?.toDoubleOrNull() ?: 0.0
            totalPrice += price * product.quantity // Calculate total price
            orderDetails.append("${product.name} (${product.price}) x${product.quantity}\n") // Format product details
        }

        // Set the order details and total price in the TextViews
        holder.orderDetailsTextView.text = orderDetails.toString().trim()
        holder.totalPriceTextView.text = "Total Price: R$totalPrice" // Display total price
        holder.orderIdTextView.text = "Order ID: ${order.orderId}" // Display order ID
        holder.statusTextView.text = "Status: ${order.status}" // Display order status
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}
