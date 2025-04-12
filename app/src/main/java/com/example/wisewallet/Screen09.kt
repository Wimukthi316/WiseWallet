package com.example.wisewallet

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.wisewallet.databinding.ActivityScreen09Binding
import com.example.wisewallet.databinding.DialogAddCategoryBudgetBinding
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class Screen09 : AppCompatActivity() {

    private lateinit var binding: ActivityScreen09Binding
    private lateinit var dataManager: DataManager
    private val currencyFormatter = NumberFormat.getCurrencyInstance()
    private val currentMonthYear by lazy {
        val calendar = Calendar.getInstance()
        "${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} ${calendar.get(Calendar.YEAR)}"
    }

    // Notification constants
    private val CHANNEL_ID = "budget_alerts_channel"
    private val NOTIFICATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityScreen09Binding.inflate(layoutInflater)
        setContentView(binding.root)
        dataManager = DataManager(this)

        createNotificationChannel()
        checkNotificationPermission()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        setupClickListeners()
        setupObservers()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Alerts"
            val description = "Notifications for budget warnings"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    showPermissionRationale()
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_CODE
                    )
                }
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("This app needs notification permission to alert you when you exceed your budget.")
            .setPositiveButton("Allow") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(
                    binding.root,
                    "Notifications disabled - budget alerts won't work",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupUI() {
        binding.monthLabel.text = currentMonthYear

        val currentBudget = dataManager.getMonthlyBudget()
        if (currentBudget > 0) {
            binding.budgetInput.setText(currentBudget.toString())
        }

        binding.budgetAlertCard.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener { finish() }

        binding.saveBudgetButton.setOnClickListener {
            val budgetInput = binding.budgetInput.text.toString()
            if (budgetInput.isNotEmpty()) {
                try {
                    val budget = budgetInput.toDouble()
                    if (budget > 0) {
                        dataManager.saveMonthlyBudget(budget)
                        Snackbar.make(binding.root, "Monthly budget saved", Snackbar.LENGTH_SHORT).show()
                    } else {
                        binding.budgetInputLayout.error = "Budget must be greater than 0"
                    }
                } catch (e: NumberFormatException) {
                    binding.budgetInputLayout.error = "Invalid amount"
                }
            } else {
                binding.budgetInputLayout.error = "Please enter a budget amount"
            }
        }

        binding.addCategoryBudgetButton.setOnClickListener {
            showAddCategoryBudgetDialog()
        }
    }

    private fun setupObservers() {
        dataManager.budgetLiveData.observe(this, Observer { budgetData ->
            updateBudgetViews(budgetData)
            updateBudgetAlertCard(budgetData)
        })

        dataManager.expensesLiveData.observe(this, Observer {
            dataManager.onExpensesUpdated()
        })
    }

    private fun updateBudgetViews(budgetData: DataManager.BudgetData) {
        val monthlyBudget = budgetData.monthlyBudget
        val totalSpent = budgetData.categorySpending.values.sum()
        val remaining = monthlyBudget - totalSpent

        binding.spentAmount.text = currencyFormatter.format(totalSpent)

        if (monthlyBudget > 0) {
            binding.progressLayout.findViewById<TextView>(R.id.ofAmountText).text =
                "of ${currencyFormatter.format(monthlyBudget)}"
        } else {
            binding.progressLayout.findViewById<TextView>(R.id.ofAmountText).text = ""
        }

        binding.budgetProgressBar.progress = if (monthlyBudget > 0) {
            (totalSpent / monthlyBudget * 100).toInt().coerceAtMost(100)
        } else {
            0
        }

        binding.remainingLabel.text = if (monthlyBudget > 0) {
            "Remaining: ${currencyFormatter.format(remaining)}"
        } else {
            "Set a monthly budget to track your spending"
        }

        updateCategoryBudgetView(
            "Food",
            binding.foodCategoryCard,
            binding.foodAmountText,
            binding.foodProgressBar,
            budgetData
        )

        updateCategoryBudgetView(
            "Transport",
            binding.transportCategoryCard,
            binding.transportAmountText,
            binding.transportProgressBar,
            budgetData
        )

        updateCategoryBudgetView(
            "Entertainment",
            binding.entertainmentCategoryCard,
            binding.entertainmentAmountText,
            binding.entertainmentProgressBar,
            budgetData
        )
    }

    private fun updateCategoryBudgetView(
        category: String,
        cardView: View,
        amountText: TextView,
        progressBar: com.google.android.material.progressindicator.LinearProgressIndicator,
        budgetData: DataManager.BudgetData
    ) {
        val spent = budgetData.categorySpending[category] ?: 0.0
        val budget = budgetData.categoryBudgets[category] ?: 0.0

        if (budget > 0) {
            cardView.visibility = View.VISIBLE
            amountText.text = "${currencyFormatter.format(spent)} / ${currencyFormatter.format(budget)}"

            val progress = (spent / budget * 100).toInt()
            progressBar.progress = progress.coerceAtMost(100)

            if (spent > budget) {
                amountText.setTextColor(ContextCompat.getColor(this, R.color.red))
                progressBar.setIndicatorColor(ContextCompat.getColor(this, R.color.red))
            } else {
                amountText.setTextColor(ContextCompat.getColor(this, R.color.Blue))
                progressBar.setIndicatorColor(ContextCompat.getColor(this, R.color.Blue))
            }
        } else {
            cardView.visibility = View.GONE
        }
    }

    private fun updateBudgetAlertCard(budgetData: DataManager.BudgetData) {
        val monthlyBudget = budgetData.monthlyBudget
        val totalSpent = budgetData.categorySpending.values.sum()

        if (monthlyBudget > 0) {
            val percentSpent = (totalSpent / monthlyBudget) * 100

            if (percentSpent >= 80 && percentSpent < 100) {
                binding.budgetAlertCard.visibility = View.VISIBLE
                binding.alertText.text = "You've spent ${percentSpent.toInt()}% of your monthly budget. Be careful with your spending."
            } else if (percentSpent >= 100) {
                binding.budgetAlertCard.visibility = View.VISIBLE
                binding.alertText.text = "You've exceeded your monthly budget by ${currencyFormatter.format(totalSpent - monthlyBudget)}!"
            } else {
                binding.budgetAlertCard.visibility = View.GONE
            }
        } else {
            binding.budgetAlertCard.visibility = View.GONE
        }
    }

    private fun showAddCategoryBudgetDialog() {
        val dialogBinding = DialogAddCategoryBudgetBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Category Budget")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialogBinding.categoryDropdown.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayOf("Food", "Transport", "Bills", "Entertainment", "Rent")
            )
        )

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val category = dialogBinding.categoryDropdown.text.toString()
                val amountText = dialogBinding.amountInput.text.toString()

                if (category.isEmpty()) {
                    dialogBinding.categoryInputLayout.error = "Please select a category"
                    return@setOnClickListener
                }

                if (amountText.isEmpty()) {
                    dialogBinding.amountInputLayout.error = "Please enter an amount"
                    return@setOnClickListener
                }

                try {
                    val amount = amountText.toDouble()
                    if (amount > 0) {
                        dataManager.saveCategoryBudget(category, amount)
                        dialog.dismiss()
                    } else {
                        dialogBinding.amountInputLayout.error = "Amount must be greater than 0"
                    }
                } catch (e: NumberFormatException) {
                    dialogBinding.amountInputLayout.error = "Invalid amount"
                }
            }
        }

        dialog.show()
    }
}