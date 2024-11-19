package com.example.barberbuzz

import java.io.Serializable

data class Product(
    val name: String? = null,
    val price: String? = null,
    val imageUrl: String? = null,
    var quantity: Int = 0
) : Serializable
