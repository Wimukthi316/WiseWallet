package com.example.wisewallet

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wisewallet.databinding.ActivityScreen04Binding
import com.google.gson.Gson

class Screen04 : AppCompatActivity() {

    private lateinit var binding: ActivityScreen04Binding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScreen04Binding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.createAccountButton.setOnClickListener {
            if (validateInputs()) {
                saveUserData()
                navigateToScreen05()
            }
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, Screen03::class.java))
        }
        binding.alreadyHaveAccountText.setOnClickListener {
            startActivity(Intent(this, Screen05::class.java))
        }
    }

    private fun validateInputs(): Boolean {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val contact = binding.contactEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        if (username.isEmpty()) {
            binding.usernameEditText.error = "Username required"
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Valid email required"
            return false
        }

        if (contact.isNotEmpty() && !android.util.Patterns.PHONE.matcher(contact).matches()) {
            binding.contactEditText.error = "Valid phone number required"
            return false
        }

        if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            return false
        }

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords don't match"
            return false
        }

        return true
    }

    private fun saveUserData() {
        val user = User(
            username = binding.usernameEditText.text.toString().trim(),
            email = binding.emailEditText.text.toString().trim(),
            password = binding.passwordEditText.text.toString(),
            phone = binding.contactEditText.text.toString().trim()
        )

        val editor = sharedPreferences.edit()
        editor.putString("CURRENT_USER", Gson().toJson(user))
        editor.apply()
    }

    private fun navigateToScreen05() {
        startActivity(Intent(this, Screen05::class.java))
        finish()
    }
}

data class User(
    val username: String,
    val email: String,
    val password: String,
    var phone: String
)