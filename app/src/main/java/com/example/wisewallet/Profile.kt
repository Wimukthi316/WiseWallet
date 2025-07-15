package com.example.wisewallet

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wisewallet.databinding.ActivityProfileBinding
import com.google.gson.Gson

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        loadUserData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.saveProfileButton.setOnClickListener {
            if (validateProfileInputs()) {
                saveUpdatedProfile()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("CURRENT_USER", null)
        currentUser = if (userJson != null) {
            Gson().fromJson(userJson, User::class.java).also { user ->
                binding.usernameEditText.setText(user.username)
                binding.emailEditText.setText(user.email)
                binding.phoneEditText.setText(user.phone)
            }
        } else {
            User("", "", "", "").also {
                Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateProfileInputs(): Boolean {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()

        if (username.isEmpty()) {
            binding.usernameEditText.error = "Username required"
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Valid email required"
            return false
        }

        if (phone.isNotEmpty() && !android.util.Patterns.PHONE.matcher(phone).matches()) {
            binding.phoneEditText.error = "Valid phone number required"
            return false
        }

        return true
    }

    private fun saveUpdatedProfile() {
        currentUser = User(
            username = binding.usernameEditText.text.toString().trim(),
            email = binding.emailEditText.text.toString().trim(),
            password = currentUser.password, // Keep the same password
            phone = binding.phoneEditText.text.toString().trim()
        )

        sharedPreferences.edit()
            .putString("CURRENT_USER", Gson().toJson(currentUser))
            .apply()
    }

    private fun logoutUser() {
        val intent = Intent(this, Screen05::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}