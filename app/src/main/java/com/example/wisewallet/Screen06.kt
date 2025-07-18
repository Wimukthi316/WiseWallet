package com.example.wisewallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.wisewallet.databinding.ActivityScreen06Binding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class Screen06 : AppCompatActivity() {

    private lateinit var binding: ActivityScreen06Binding
    private lateinit var dataManager: DataManager
    private val currencyFormatter = NumberFormat.getCurrencyInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityScreen06Binding.inflate(layoutInflater)
        setContentView(binding.root)
        dataManager = DataManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupGreeting()
        setupObservers()
        setupClickListeners()
        updatePeriodText()
    }

    private fun setupGreeting() {
        // Set dynamic greeting based on time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }

        binding.greetingText.text = greeting

        // Get the real username from DataManager
        val username = dataManager.getCurrentUsername()
        binding.usernameText.text = username
    }

    private fun setupObservers() {
        // Observe budget data changes
        dataManager.budgetLiveData.observe(this, Observer { budgetData ->
            updateBudgetSummary(budgetData)
        })
    }

    private fun updateBudgetSummary(budgetData: DataManager.BudgetData) {
        val monthlyBudget = budgetData.monthlyBudget
        val totalSpent = budgetData.categorySpending.values.sum()

        // Update budget amount
        if (monthlyBudget > 0) {
            binding.budgetAmount.text = currencyFormatter.format(monthlyBudget)
        } else {
            binding.budgetAmount.text = "No budget set"
        }

        // Update spent amount
        binding.spentAmount.text = "Spent: ${currencyFormatter.format(totalSpent)}"

        // Calculate and display month-over-month change
        calculateMonthlyChange(totalSpent)
    }

    private fun calculateMonthlyChange(currentMonthSpent: Double) {
        val expenses = dataManager.loadExpenses()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Calculate last month's spending
        calendar.add(Calendar.MONTH, -1)
        val lastMonth = calendar.get(Calendar.MONTH)
        val lastMonthYear = calendar.get(Calendar.YEAR)

        val lastMonthSpent = expenses
            .filter {
                val expenseCalendar = Calendar.getInstance().apply { time = it.date }
                expenseCalendar.get(Calendar.MONTH) == lastMonth &&
                        expenseCalendar.get(Calendar.YEAR) == lastMonthYear
            }
            .sumOf { it.amount }

        // Calculate percentage change
        if (lastMonthSpent > 0) {
            val percentageChange = ((currentMonthSpent - lastMonthSpent) / lastMonthSpent) * 100
            val changeText = if (percentageChange >= 0) {
                "+${String.format("%.1f", percentageChange)}% from last month"
            } else {
                "${String.format("%.1f", percentageChange)}% from last month"
            }

            binding.changeText.text = changeText

            // Change icon based on increase/decrease
            if (percentageChange >= 0) {
                binding.changeIcon.setImageResource(R.drawable.goal) // Use your up arrow icon
            } else {
                binding.changeIcon.setImageResource(R.drawable.goal) // Use your down arrow icon
            }
        } else {
            binding.changeText.text = "No data from last month"
        }
    }

    private fun updatePeriodText() {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.periodText.text = monthFormat.format(calendar.time)
    }

    private fun setupClickListeners() {
        // Profile icon navigation - direct to Profile activity
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

        // Add transaction button
        binding.addTransactionButton.setOnClickListener {
            val intent = Intent(this, Screen07::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh all data when returning to this screen
        dataManager.refreshUserData()
        setupGreeting()
    }
}