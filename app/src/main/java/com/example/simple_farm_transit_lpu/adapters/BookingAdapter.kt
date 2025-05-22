package com.example.simple_farm_transit_lpu.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_farm_transit_lpu.R
import com.example.simple_farm_transit_lpu.models.Booking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingAdapter(
    private val bookings: List<Booking>
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vehicleType: TextView = view.findViewById(R.id.bookingVehicleTypeText)
        val route: TextView = view.findViewById(R.id.bookingRouteText)
        val date: TextView = view.findViewById(R.id.bookingDateText)
        val status: TextView = view.findViewById(R.id.bookingStatusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.vehicleType.text = booking.vehicleType
        holder.route.text = "From ${booking.fromLocation} to ${booking.toLocation}"
        
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(booking.bookingDate))
        holder.date.text = formattedDate
        
        holder.status.text = booking.status
        
        // Set status color based on booking status
        val context = holder.status.context
        val statusColor = when (booking.status) {
            "Confirmed" -> context.getColor(R.color.colorConfirmed)
            "Completed" -> context.getColor(R.color.colorCompleted)
            "Cancelled" -> context.getColor(R.color.colorCancelled)
            else -> context.getColor(R.color.colorPending)
        }
        holder.status.setTextColor(statusColor)
    }

    override fun getItemCount() = bookings.size
}
