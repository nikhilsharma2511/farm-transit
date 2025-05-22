package com.example.simple_farm_transit_lpu

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simple_farm_transit_lpu.models.Vehicle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class VehicleAddActivity : AppCompatActivity() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_add)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        // Set up action bar with back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add New Vehicle"
        
        // Set up the spinner with vehicle types
        val vehicleTypeSpinner = findViewById<Spinner>(R.id.vehicleTypeSpinner)
        val vehicleTypes = arrayOf("Tractor", "Truck", "Pickup", "Van", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vehicleTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        vehicleTypeSpinner.adapter = adapter
        
        // Set up the save button
        findViewById<Button>(R.id.saveVehicleButton).setOnClickListener {
            val vehicleType = vehicleTypeSpinner.selectedItem.toString()
            val capacity = findViewById<EditText>(R.id.capacityInput).text.toString()
            val ratePerTon = findViewById<EditText>(R.id.ratePerTonInput).text.toString()
            val licensePlate = findViewById<EditText>(R.id.licensePlateInput).text.toString()
            
            if (validateInputs(capacity, ratePerTon, licensePlate)) {
                // Show loading message
                Toast.makeText(this, "Adding vehicle...", Toast.LENGTH_SHORT).show()
                
                // Save vehicle to Firebase
                saveVehicleToFirebase(vehicleType, capacity, ratePerTon, licensePlate)
            }
        }
    }
    
    private fun validateInputs(capacity: String, ratePerTon: String, licensePlate: String): Boolean {
        if (capacity.isEmpty()) {
            Toast.makeText(this, "Please enter vehicle capacity", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (ratePerTon.isEmpty()) {
            Toast.makeText(this, "Please enter rate per ton", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (licensePlate.isEmpty()) {
            Toast.makeText(this, "Please enter license plate number", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun saveVehicleToFirebase(vehicleType: String, capacity: String, ratePerTon: String, licensePlate: String) {
        val currentUserId = auth.currentUser?.uid
        
        if (currentUserId == null) {
            Toast.makeText(this, "You must be logged in to add a vehicle", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Generate a unique vehicle ID
        val vehiclesRef = database.reference.child("vehicles")
        val newVehicleId = vehiclesRef.push().key
        
        if (newVehicleId == null) {
            Toast.makeText(this, "Error creating vehicle ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create vehicle object
        val vehicle = Vehicle(
            id = newVehicleId,
            ownerId = currentUserId,
            type = vehicleType,
            capacity = capacity,
            ratePerTon = ratePerTon,
            licensePlate = licensePlate,
            isAvailable = true
        )
        
        // Save vehicle to database
        vehiclesRef.child(newVehicleId).setValue(vehicle)
            .addOnSuccessListener {
                Toast.makeText(this, "Vehicle added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add vehicle: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}