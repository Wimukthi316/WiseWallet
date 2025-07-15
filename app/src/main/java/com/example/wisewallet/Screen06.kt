package com.example.wisewallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wisewallet.databinding.ActivityScreen06Binding

class Screen06 : AppCompatActivity() {

    private lateinit var binding: ActivityScreen06Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityScreen06Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Add this block for profile icon navigation
        binding.profileIconCard.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        // Existing navigation handlers
        binding.addExpensesCard.setOnClickListener {
            val intent = Intent(this, Screen07::class.java)
            startActivity(intent)
        }

        binding.spendingSummaryCard.setOnClickListener {
            val intent = Intent(this, Screen08::class.java)
            startActivity(intent)
        }

        binding.budgetPlanCard.setOnClickListener {
            val intent = Intent(this, Screen09::class.java)
            startActivity(intent)
        }
    }
}