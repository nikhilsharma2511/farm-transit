package com.example.simple_farm_transit_lpu.models

data class Vehicle(
    val id: String = "",
    val ownerId: String = "",
    val type: String = "",
    val capacity: String = "",
    val ratePerTon: String = "",
    val licensePlate: String = "",
    val isAvailable: Boolean = true
)
