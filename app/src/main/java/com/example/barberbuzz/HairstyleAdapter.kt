// HairstyleAdapter.kt
package com.example.barberbuzz

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HairstyleAdapter(
    private val hairstyles: List<Hairstyle>,
    private val onItemClick: (Hairstyle) -> Unit // Callback for item click
) : RecyclerView.Adapter<HairstyleAdapter.HairstyleViewHolder>() {

    class HairstyleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hairstyleName: TextView = itemView.findViewById(R.id.hairstyleNameTextView)
        val duration: TextView = itemView.findViewById(R.id.hairstyleDurationTextView)
        val price: TextView = itemView.findViewById(R.id.hairstylePriceTextView)
        val imageView: ImageView = itemView.findViewById(R.id.hairstyleImageView)

        // Bind hairstyle data and set click listener
        fun bind(hairstyle: Hairstyle, onItemClick: (Hairstyle) -> Unit) {
            hairstyleName.text = hairstyle.name
            duration.text = hairstyle.duration
            price.text = hairstyle.price

            // Load the image using Glide
            Glide.with(itemView.context)
                .load(hairstyle.imageUrl)
                .into(imageView)

            // Set click listener for the entire item
            itemView.setOnClickListener {
                onItemClick(hairstyle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HairstyleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hairstyle_item, parent, false)
        return HairstyleViewHolder(view)
    }

    override fun onBindViewHolder(holder: HairstyleViewHolder, position: Int) {
        holder.bind(hairstyles[position], onItemClick)
    }

    override fun getItemCount(): Int {
        return hairstyles.size
    }
}
