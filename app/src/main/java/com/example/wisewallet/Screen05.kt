package com.example.wisewallet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Screen05 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen05)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

        //Click Creeate Acc button Navigate To Screen 06
        var loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener{
            val intent = Intent(this, Screen06::class.java)
            startActivity(intent)

        }

        //backButton For Navigate Screen05 To Screen 04
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { navigateToScreen04() }
    }

    //Function to Navigate Screen05 To Screen 04
    private fun navigateToScreen04() {
        val intent = Intent(this, Screen04::class.java)
        startActivity(intent)
    }
}