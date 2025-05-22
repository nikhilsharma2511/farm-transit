package com.example.simple_farm_transit_lpu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize Views
        profileImage = findViewById(R.id.profileImage)
        nameInput = findViewById(R.id.nameTextView)
        emailInput = findViewById(R.id.emailTextView)
        phoneInput = findViewById(R.id.phoneTextView)
        saveButton = findViewById(R.id.editProfileButton)
        progressBar = findViewById(R.id.profileProgressBar)
        
        // Set up action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"

        // Load Existing Profile Data
        loadProfileData()

        // Profile Image Click Listener
        profileImage.setOnClickListener {
            openImageChooser()
        }

        // Save Profile Button
        saveButton.setOnClickListener {
            saveProfileData()
        }
    }

    private fun loadProfileData() {
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }
        
        // Show progress bar while loading
        progressBar.visibility = View.VISIBLE
        
        // Set email from Firebase Auth
        emailInput.setText(currentUser.email)
        
        // Get additional user data from Firebase Database
        val userRef = database.reference.child("users").child(currentUser.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.visibility = View.GONE
                
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val phone = snapshot.child("phone").getValue(String::class.java)
                    val profileImageUri = snapshot.child("profileImageUri").getValue(String::class.java)
                    
                    nameInput.setText(name)
                    phoneInput.setText(phone)
                    
                    // Load profile image if exists
                    profileImageUri?.let {
                        profileImage.setImageURI(Uri.parse(it))
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ProfileActivity, "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfileData() {
        val name = nameInput.text.toString()
        val email = emailInput.text.toString()
        val phone = phoneInput.text.toString()

        // Validate Input
        if (validateInput(name, email, phone)) {
            val currentUser = auth.currentUser ?: return
            
            // Show progress bar
            progressBar.visibility = View.VISIBLE
            
            // Update user data in Firebase Database
            val userRef = database.reference.child("users").child(currentUser.uid)
            
            val userData = HashMap<String, Any>()
            userData["name"] = name
            userData["email"] = email
            userData["phone"] = phone
            
            userRef.updateChildren(userData)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun validateInput(name: String, email: String, phone: String): Boolean {
        return when {
            name.isEmpty() -> {
                nameInput.error = "Name cannot be empty"
                false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInput.error = "Invalid email address"
                false
            }
            phone.isEmpty() || phone.length != 10 -> {
                phoneInput.error = "Invalid phone number"
                false
            }
            else -> true
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                profileImage.setImageURI(uri)

                // Save Image URI to Firebase
                val currentUser = auth.currentUser ?: return
                val userRef = database.reference.child("users").child(currentUser.uid)
                
                userRef.child("profileImageUri").setValue(uri.toString())
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}