package com.example.barberbuzz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class BarberAdapter(
    private val context: Context,
    private var barberList: MutableList<Barber>,
    private val barberReference: DatabaseReference
) : RecyclerView.Adapter<BarberAdapter.BarberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.barber_item, parent, false)
        return BarberViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarberViewHolder, position: Int) {
        val barber = barberList[position]
        holder.bind(barber)

        // Handle card click for rating
        holder.itemView.setOnClickListener {
            showRatingDialog(barber)
        }
    }

    override fun getItemCount() = barberList.size

    fun updateBarberList(newBarbers: List<Barber>) {
        barberList.clear()
        barberList.addAll(newBarbers)
        notifyDataSetChanged()
    }

    private fun showRatingDialog(barber: Barber) {
        // Get the username from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        if (username == null) {
            Toast.makeText(context, "You must be logged in to rate.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a reference to the ratings for this barber and username
        val userRatingRef = barberReference.child(barber.barberName).child("ratings").child(username)

        userRatingRef.get().addOnSuccessListener { snapshot ->
            val lastRatingTime = snapshot.getValue<Long>() ?: 0L
            val currentTime = System.currentTimeMillis()

            // Check if 24 hours have passed
            if (currentTime - lastRatingTime < 24 * 60 * 60 * 1000) { // 24 hours in milliseconds
                Toast.makeText(context, "You have already rated this barber", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Show rating dialog
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rate_barber, null)
            val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

            AlertDialog.Builder(context)
                .setTitle("Rate ${barber.barberName}")
                .setView(dialogView)
                .setPositiveButton("Submit") { _, _ ->
                    val rating = ratingBar.rating
                    updateBarberRating(barber, rating)
                    // Save the current timestamp
                    userRatingRef.setValue(currentTime)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }.addOnFailureListener {
            Toast.makeText(context, "Error checking rating time", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateBarberRating(barber: Barber, rating: Float) {
        val barberKey = barber.barberName

        // Fetch current rating and count
        barberReference.child(barberKey).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentRating = snapshot.child("rating").getValue(Float::class.java) ?: 0f
                val currentRatingCount = snapshot.child("ratingCount").getValue(Int::class.java) ?: 0

                // Calculate the new average rating
                val newRatingCount = currentRatingCount + 1
                val newRating = ((currentRating * currentRatingCount) + rating) / newRatingCount

                // Update the barber's rating in Firebase
                barberReference.child(barberKey).child("rating").setValue(newRating)
                barberReference.child(barberKey).child("ratingCount").setValue(newRatingCount)

                Toast.makeText(context, "Thank you for rating!", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error submitting rating", Toast.LENGTH_SHORT).show()
            }
        })
    }

    class BarberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val barberNameTextView: TextView = itemView.findViewById(R.id.barberNameTextView)
        private val barberEmailTextView: TextView = itemView.findViewById(R.id.barberEmailTextView)
        private val barberRatingBar: RatingBar = itemView.findViewById(R.id.barberRatingBar)
        private val barberImageView: ImageView = itemView.findViewById(R.id.barberImageView)


        fun bind(barber: Barber) {
            barberNameTextView.text = barber.barberName
            barberEmailTextView.text = barber.email
            barberRatingBar.rating = barber.rating

            // Load the image using Glide
            if (barber.profileImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(barber.profileImageUrl)
                    .placeholder(R.drawable.admin_users) // Fallback image if no profile image
                    .into(barberImageView)
            } else {
                barberImageView.setImageResource(R.drawable.admin_users) // Default image
            }
        }
    }
}
