package com.example.wisewallet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Screen04 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen04)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var createAccountButton = findViewById<Button>(R.id.createAccountButton)
        createAccountButton.setOnClickListener{
            val intent = Intent(this, Screen05::class.java)
            startActivity(intent)
            finish()

        }

        //backButton For Navigate Screen04 To Screen 03
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { navigateToScreen03() }

    }
    //Function to Navigate Screen04 To Screen 03
    private fun navigateToScreen03() {
        val intent = Intent(this, Screen03::class.java)
        startActivity(intent)
    }
}