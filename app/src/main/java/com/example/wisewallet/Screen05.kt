package com.example.wisewallet

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wisewallet.databinding.ActivityScreen05Binding
import com.google.gson.Gson

class Screen05 : AppCompatActivity() {

    private lateinit var binding: ActivityScreen05Binding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScreen05Binding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginButton.setOnClickListener {
            if (validateLogin()) {
                navigateToScreen06()
            }
        }

        binding.signsUpText.setOnClickListener {
            startActivity(Intent(this, Screen04::class.java))
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, Screen04::class.java))
        }
    }

    private fun validateLogin(): Boolean {
        val email = binding.usernameEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.usernameEditText.error = "Valid email required"
            return false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password required"
            return false
        }

        val userJson = sharedPreferences.getString("CURRENT_USER", null)
        if (userJson == null) {
            Toast.makeText(this, "No user found. Please sign up first", Toast.LENGTH_SHORT).show()
            return false
        }

        val user = Gson().fromJson(userJson, User::class.java)
        if (user.email != email || user.password != password) {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun navigateToScreen06() {
        startActivity(Intent(this, Screen06::class.java))
        finish()
    }
}