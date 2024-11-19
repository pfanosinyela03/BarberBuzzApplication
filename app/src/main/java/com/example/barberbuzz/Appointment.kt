package com.example.barberbuzz

data class Appointment(
    var id: String? = null,
    val hairstyleName: String = "",
    val price: String = "",
    val barberName: String = "",
    var date: String = "",
    var time: String = "",
    val username: String = ""
)

