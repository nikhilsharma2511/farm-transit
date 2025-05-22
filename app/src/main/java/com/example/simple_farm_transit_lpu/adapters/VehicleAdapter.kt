package com.example.simple_farm_transit_lpu.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_farm_transit_lpu.R
import com.example.simple_farm_transit_lpu.models.Vehicle

class VehicleAdapter(
    private val vehicles: List<Vehicle>,
    private val onBookClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vehicleType: TextView = view.findViewById(R.id.vehicleTypeText)
        val vehicleCapacity: TextView = view.findViewById(R.id.vehicleCapacityText)
        val vehicleRate: TextView = view.findViewById(R.id.vehicleRateText)
        val vehicleLicensePlate: TextView = view.findViewById(R.id.vehicleLicensePlateText)
        val bookButton: Button = view.findViewById(R.id.bookVehicleButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicles[position]
        holder.vehicleType.text = vehicle.type
        holder.vehicleCapacity.text = "Capacity: ${vehicle.capacity} tons"
        holder.vehicleRate.text = "Rate: ₹${vehicle.ratePerTon} per ton"
        holder.vehicleLicensePlate.text = "License: ${vehicle.licensePlate}"
        
        holder.bookButton.setOnClickListener {
            onBookClick(vehicle)
        }
    }

    override fun getItemCount() = vehicles.size
}
