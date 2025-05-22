package com.example.simple_farm_transit_lpu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
//import com.example.simple_farm_transit_lpu.MainActivity
import com.example.simple_farm_transit_lpu.R
import com.example.simple_farm_transit_lpu.RegisterActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AuthActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    companion object {
        const val KEY1 = "com.farmtransit.AuthActivity.mail"
        const val KEY2 = "com.farmtransit.AuthActivity.name"
        const val KEY3 = "com.farmtransit.AuthActivity.phone"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        val signButton = findViewById<Button>(R.id.btn_login)
        val etMail = findViewById<EditText>(R.id.et_email)
        val userPass = findViewById<EditText>(R.id.et_password)

        val registerTextView = findViewById<TextView>(R.id.tv_register)

        // 🔽 Add the click listener for register text
        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        signButton.setOnClickListener {
            val mail = etMail.text.toString().trim()
            val password = userPass.text.toString().trim()

            if (mail.isNotEmpty() && password.isNotEmpty()) {
                readData(mail, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun readData(email: String, password: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.get().addOnSuccessListener { snapshot ->
            var userFound = false
            for (user in snapshot.children) {
                val userEmail = user.child("email").value.toString()
                val userPassword = user.child("password").value.toString()

                if (userEmail == email && userPassword == password) {
                    val name = user.child("name").value
                    val phone = user.child("phone").value
                    val userType = user.child("userType").value

                    val intentWelcome = Intent(this, AuthActivity::class.java)
                    intentWelcome.putExtra(KEY1, userEmail)
                    intentWelcome.putExtra(KEY2, name.toString())
                    intentWelcome.putExtra(KEY3, phone.toString())
                    startActivity(intentWelcome)
                    userFound = true
                    break
                }
            }

            if (!userFound) {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Login failed. Please try again!", Toast.LENGTH_SHORT).show()
        }
    }
}
























//class AuthActivity : AppCompatActivity() {
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var emailEditText: EditText
//    private lateinit var passwordEditText: EditText
//    private lateinit var loginButton: Button
//    private lateinit var registerTextView: TextView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_auth)
//
//        // Initialize Firebase Auth
//        auth = FirebaseAuth.getInstance()
//
//        // Initialize views
//        emailEditText = findViewById(R.id.et_email)
//        passwordEditText = findViewById(R.id.et_password)
//        loginButton = findViewById(R.id.btn_login)
//        registerTextView = findViewById(R.id.tv_register)
//
//        // Set click listeners
//        loginButton.setOnClickListener {
//            loginUser()
//        }
//
//        registerTextView.setOnClickListener {
//            val intent = Intent(this, RegisterActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//    private fun loginUser() {
//        val email = emailEditText.text.toString().trim()
//        val password = passwordEditText.text.toString().trim()
//
//        if (email.isEmpty() || password.isEmpty()) {
//            CustomToast.showToast(this, "Please fill all fields", Toast.LENGTH_SHORT)
//            return
//        }
//
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success
//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                } else {
//                    // If sign in fails
//                    CustomToast.showToast(this, "Authentication failed: ${task.exception?.message}",
//                        Toast.LENGTH_LONG)
//                }
//            }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        // Check if user is signed in
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }
//}
