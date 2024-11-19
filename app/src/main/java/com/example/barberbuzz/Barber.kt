package com.example.barberbuzz

data class Barber(
    val barberName: String = "",
    val email: String = "",
    val status: String = "pending approval",
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val profileImageUrl: String = ""

) {
    // No-argument constructor
    constructor() : this("", "", "pending approval", 0f, 0)
}
