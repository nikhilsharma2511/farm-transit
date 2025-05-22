package com.example.simple_farm_transit_lpu

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_farm_transit_lpu.adapters.BookingAdapter
import com.example.simple_farm_transit_lpu.adapters.VehicleAdapter
import com.example.simple_farm_transit_lpu.models.Booking
import com.example.simple_farm_transit_lpu.models.Vehicle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class DashboardActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var noDataTextView: TextView
    
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    private val vehicles = mutableListOf<Vehicle>()
    private val bookings = mutableListOf<Booking>()
    
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var bookingAdapter: BookingAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        // Initialize views
        recyclerView = findViewById(R.id.transportListRecyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        noDataTextView = findViewById(R.id.noDataTextView)
        

        
        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Setup adapters
        vehicleAdapter = VehicleAdapter(vehicles) { vehicle ->
            showBookingDialog(vehicle)
        }
        
        bookingAdapter = BookingAdapter(bookings)
        
        // Set default adapter
        recyclerView.adapter = vehicleAdapter
        
        // Setup tab selection listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        recyclerView.adapter = vehicleAdapter
                        checkForEmptyState(vehicles.isEmpty())
                    }
                    1 -> {
                        recyclerView.adapter = bookingAdapter
                        checkForEmptyState(bookings.isEmpty())
                    }
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        
        // Load data
        loadVehicles()
        loadBookings()
        
        // Bottom Navigation Setup
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_add_vehicle -> {
                    startActivity(Intent(this, VehicleAddActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadVehicles() {
        val vehiclesRef = database.reference.child("vehicles")
        
        vehiclesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                vehicles.clear()
                
                for (vehicleSnapshot in snapshot.children) {
                    // Make sure to get the ID from the snapshot key
                    val vehicleId = vehicleSnapshot.key
                    val vehicle = vehicleSnapshot.getValue(Vehicle::class.java)
                    
                    vehicle?.let {
                        // Ensure the vehicle has the correct ID from the snapshot
                        val updatedVehicle = it.copy(id = vehicleId ?: "")
                        
                        // Only add available vehicles that aren't owned by the current user
                        if (updatedVehicle.isAvailable && updatedVehicle.ownerId != auth.currentUser?.uid) {
                            vehicles.add(updatedVehicle)
                        }
                    }
                }
                
                vehicleAdapter.notifyDataSetChanged()
                
                // Check if we're on the vehicles tab and update empty state
                if (tabLayout.selectedTabPosition == 0) {
                    checkForEmptyState(vehicles.isEmpty())
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Failed to load vehicles: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun loadBookings() {
        val currentUserId = auth.currentUser?.uid ?: return
        val bookingsRef = database.reference.child("bookings")
            .orderByChild("userId")
            .equalTo(currentUserId)
        
        bookingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookings.clear()
                
                for (bookingSnapshot in snapshot.children) {
                    val booking = bookingSnapshot.getValue(Booking::class.java)
                    booking?.let {
                        bookings.add(it)
                    }
                }
                
                // Sort bookings by date (newest first)
                bookings.sortByDescending { it.bookingDate }
                
                bookingAdapter.notifyDataSetChanged()
                
                // Check if we're on the bookings tab and update empty state
                if (tabLayout.selectedTabPosition == 1) {
                    checkForEmptyState(bookings.isEmpty())
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Failed to load bookings: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun checkForEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            noDataTextView.visibility = View.VISIBLE
            
            // Set appropriate message based on selected tab
            if (tabLayout.selectedTabPosition == 0) {
                noDataTextView.text = "No vehicles available for booking"
            } else {
                noDataTextView.text = "You don't have any bookings yet"
            }
        } else {
            recyclerView.visibility = View.VISIBLE
            noDataTextView.visibility = View.GONE
        }
    }
    
    private fun showBookingDialog(vehicle: Vehicle) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_book_vehicle, null)
        val fromLocationInput = dialogView.findViewById<EditText>(R.id.fromLocationInput)
        val toLocationInput = dialogView.findViewById<EditText>(R.id.toLocationInput)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmBookingButton)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        confirmButton.setOnClickListener {
            val fromLocation = fromLocationInput.text.toString().trim()
            val toLocation = toLocationInput.text.toString().trim()
            
            if (fromLocation.isEmpty() || toLocation.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Create booking
            createBooking(vehicle, fromLocation, toLocation)
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun createBooking(vehicle: Vehicle, fromLocation: String, toLocation: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Generate a unique booking ID
        val bookingsRef = database.reference.child("bookings")
        val newBookingId = bookingsRef.push().key ?: return
        
        // Create booking object
        val booking = Booking(
            id = newBookingId,
            userId = currentUserId,
            vehicleId = vehicle.id,
            vehicleType = vehicle.type,
            licensePlate = vehicle.licensePlate,
            fromLocation = fromLocation,
            toLocation = toLocation,
            bookingDate = Date().time,
            status = "Pending"
        )
        
        // Save booking to database
        bookingsRef.child(newBookingId).setValue(booking)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking created successfully!", Toast.LENGTH_SHORT).show()
                
                // Switch to bookings tab
                tabLayout.getTabAt(1)?.select()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to the activity
        loadVehicles()
        loadBookings()
    }
}