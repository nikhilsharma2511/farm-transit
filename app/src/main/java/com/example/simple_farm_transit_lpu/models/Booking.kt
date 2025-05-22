package com.example.simple_farm_transit_lpu.models

import java.util.Date

data class Booking(
    val id: String = "",
    val userId: String = "",
    val vehicleId: String = "",
    val vehicleType: String = "",
    val licensePlate: String = "",
    val fromLocation: String = "",
    val toLocation: String = "",
    val bookingDate: Long = Date().time,
    val status: String = "Pending" // Pending, Confirmed, Completed, Cancelled
)
