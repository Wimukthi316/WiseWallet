package com.example.wisewallet

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wisewallet.databinding.ActivityProfileBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dataManager: DataManager
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        dataManager = DataManager(this)
        loadUserData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.saveProfileButton.setOnClickListener {
            if (validateProfileInputs()) {
                saveUpdatedProfile()
            }
        }

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun getAllUsers(): List<User> {
        val usersJson = sharedPreferences.getString("ALL_USERS", null)
        return if (usersJson != null) {
            val type = object : TypeToken<List<User>>() {}.type
            Gson().fromJson(usersJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun saveAllUsers(users: List<User>) {
        val editor = sharedPreferences.edit()
        editor.putString("ALL_USERS", Gson().toJson(users))
        editor.apply()
    }

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("CURRENT_USER", null)
        currentUser = if (userJson != null) {
            Gson().fromJson(userJson, User::class.java).also { user ->
                binding.usernameEditText.setText(user.username)
                binding.emailEditText.setText(user.email)
                binding.phoneEditText.setText(user.phone)

                // Display current user info
                displayUserInfo(user)
            }
        } else {
            User("", "", "", "").also {
                Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show()
                // If no user data, redirect to login
                redirectToLogin()
            }
        }
    }

    private fun displayUserInfo(user: User) {
        // You can add TextViews to show current user info
        // For now, just ensure the EditTexts are populated
        binding.usernameEditText.setText(user.username)
        binding.emailEditText.setText(user.email)
        binding.phoneEditText.setText(user.phone)
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

        // Check if username or email is taken by another user
        val users = getAllUsers()
        val existingUser = users.find {
            (it.username == username || it.email == email) &&
                    it.username != currentUser.username &&
                    it.email != currentUser.email
        }

        if (existingUser != null) {
            if (existingUser.username == username) {
                binding.usernameEditText.error = "Username already taken"
            }
            if (existingUser.email == email) {
                binding.emailEditText.error = "Email already in use"
            }
            return false
        }

        return true
    }

    private fun saveUpdatedProfile() {
        val newUsername = binding.usernameEditText.text.toString().trim()
        val newEmail = binding.emailEditText.text.toString().trim()
        val oldUsername = currentUser.username
        val oldEmail = currentUser.email

        // Check if username has changed
        val usernameChanged = oldUsername != newUsername
        val emailChanged = oldEmail != newEmail

        if (usernameChanged) {
            // Show warning about username change - NO TOAST here
            AlertDialog.Builder(this)
                .setTitle("Username Change Warning")
                .setMessage("Changing your username will migrate all your data (expenses, budgets) to the new username. You'll need to log in again with your new username. Continue?")
                .setPositiveButton("Yes, Change Username") { _, _ ->
                    updateUserProfile(newUsername, newEmail, true, emailChanged)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    // Revert username field
                    binding.usernameEditText.setText(oldUsername)
                }
                .setCancelable(false) // Prevent dismissing by touching outside
                .show()
        } else {
            updateUserProfile(newUsername, newEmail, false, emailChanged)
        }
    }

    private fun updateUserProfile(newUsername: String, newEmail: String, usernameChanged: Boolean, emailChanged: Boolean) {
        val oldUsername = currentUser.username

        // Create updated user object
        val updatedUser = User(
            username = newUsername,
            email = newEmail,
            password = currentUser.password, // Keep the same password
            phone = binding.phoneEditText.text.toString().trim()
        )

        // Update user in the users list
        val users = getAllUsers().toMutableList()
        val userIndex = users.indexOfFirst { it.username == currentUser.username && it.email == currentUser.email }

        if (userIndex != -1) {
            users[userIndex] = updatedUser
            saveAllUsers(users)
        }

        // Save updated current user
        sharedPreferences.edit()
            .putString("CURRENT_USER", Gson().toJson(updatedUser))
            .apply()

        // If username changed, migrate all user data
        if (usernameChanged) {
            dataManager.migrateUserData(oldUsername, newUsername)
        }

        // Update currentUser reference
        currentUser = updatedUser

        if (usernameChanged || emailChanged) {
            // If username or email changed, logout and redirect to login
            val message = when {
                usernameChanged && emailChanged -> "Username and email updated. Your data has been migrated. Please log in again with your new credentials."
                usernameChanged -> "Username updated. Your data has been migrated. Please log in again with your new username."
                emailChanged -> "Email updated. Please log in again with your new email."
                else -> "Profile updated successfully"
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            // Wait a moment before logout to show the toast
            binding.root.postDelayed({
                performLogout()
            }, 3000) // Longer delay to read migration message
        } else {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        // Only clear current user session, not user data
        dataManager.clearSession()

        // Redirect to login screen
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, Screen05::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to profile
        loadUserData()
    }
}