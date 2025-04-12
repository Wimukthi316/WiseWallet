package com.example.wisewallet

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wisewallet.databinding.ActivityScreen08Binding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Screen08 : AppCompatActivity() {

    private lateinit var binding: ActivityScreen08Binding
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var dataManager: DataManager
    private val expensesList = mutableListOf<Expense>()
    private var filteredList = mutableListOf<Expense>()
    private var currentFilterCategory: String? = null

    private val addExpenseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityScreen08Binding.inflate(layoutInflater)
        setContentView(binding.root)
        dataManager = DataManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupChipGroup()
        setupClickListeners()
        setupObservers()
        refreshData()
    }

    private fun setupObservers() {
        // Replace getExpensesLiveData() with expensesLiveData
        dataManager.expensesLiveData.observe(this, Observer { expenses ->
            expensesList.clear()
            expensesList.addAll(expenses)
            filterExpenses(currentFilterCategory)
            updateSummary()
            updateEmptyState()
        })
    }

    private fun refreshData() {
        loadExpenses()
        updateSummary()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(
            filteredList,
            onEditClick = { position -> editExpense(position) },
            onDeleteClick = { position -> confirmDeleteExpense(position) }
        )

        binding.expenseRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Screen08)
            adapter = expenseAdapter
        }
    }

    private fun setupChipGroup() {
        binding.categoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                binding.allChip.isChecked = true
                filterExpenses(null)
            } else {
                when (checkedIds[0]) {
                    R.id.allChip -> filterExpenses(null)
                    R.id.foodChip -> filterExpenses("Food")
                    R.id.transportChip -> filterExpenses("Transport")
                    R.id.billsChip -> filterExpenses("Bills")
                    R.id.entertainmentChip -> filterExpenses("Entertainment")
                    R.id.rentChip -> filterExpenses("Rent")
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addButton.setOnClickListener {
            val intent = Intent(this, Screen07::class.java)
            addExpenseLauncher.launch(intent)
        }
    }

    private fun loadExpenses() {
        expensesList.clear()
        expensesList.addAll(dataManager.loadExpenses())

        filteredList.clear()
        filteredList.addAll(expensesList)
        expenseAdapter.notifyDataSetChanged()

        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredList.isEmpty()) {
            binding.expenseRecyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            binding.expenseRecyclerView.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
        }
    }

    private fun filterExpenses(category: String?) {
        currentFilterCategory = category

        filteredList.clear()
        if (category == null) {
            filteredList.addAll(expensesList)
        } else {
            filteredList.addAll(expensesList.filter { it.category == category })
        }

        expenseAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateSummary() {
        val totalAmount = expensesList.sumOf { it.amount }

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val thisMonthAmount = expensesList.filter {
            val expenseCalendar = Calendar.getInstance().apply { time = it.date }
            expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                    expenseCalendar.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }

        val currencyFormatter = NumberFormat.getCurrencyInstance()
        binding.totalExpenseAmount.text = currencyFormatter.format(totalAmount)
        binding.thisMonthAmount.text = currencyFormatter.format(thisMonthAmount)
        binding.totalItems.text = expensesList.size.toString()
    }

    private fun editExpense(position: Int) {
        val expense = filteredList[position]
        val intent = Intent(this, Screen07::class.java)
        intent.putExtra("EDIT_EXPENSE", expense)
        addExpenseLauncher.launch(intent)
    }

    private fun confirmDeleteExpense(position: Int) {
        val expense = filteredList[position]

        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete '${expense.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteExpense(expense, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteExpense(expense: Expense, position: Int) {
        dataManager.deleteExpense(expense.id)
        // No need to manually update UI here - the LiveData observer will handle it
        Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
    }
}