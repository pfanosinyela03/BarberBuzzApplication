package com.example.barberbuzz

data class Order(
    val items: List<Product> = listOf(),
    val orderId: String = "",
    val status: String = ""
)
