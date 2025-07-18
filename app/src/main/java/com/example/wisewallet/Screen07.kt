package com.example.wisewallet

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wisewallet.databinding.ActivityScreen07Binding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Screen07 : AppCompatActivity() {

    private lateinit var binding: ActivityScreen07Binding
    private lateinit var dataManager: DataManager
    private val calendar = Calendar.getInstance()
    private var editingExpense: Expense? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityScreen07Binding.inflate(layoutInflater)
        setContentView(binding.root)
        dataManager = DataManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editingExpense = intent.getParcelableExtra("EDIT_EXPENSE")
        if (editingExpense != null) {
            populateExpenseData(editingExpense!!)
            binding.saveButton.text = "Update Expense"
        }

        setupCategoryDropdown()
        setupDatePicker()
        setupClickListeners()
    }

    private fun populateExpenseData(expense: Expense) {
        binding.titleInput.setText(expense.title)
        binding.categoryDropdown.setText(expense.category)
        binding.amountInput.setText(expense.amount.toString())
        calendar.time = expense.date
        updateDateInView()
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Rent")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        (binding.categoryDropdown as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        updateDateInView()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        binding.dateInput.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.dateInputLayout.setEndIconOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateInView() {
        val format = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(format, Locale.US)
        binding.dateInput.setText(sdf.format(calendar.time))
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                saveExpense()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.titleInput.text.toString().trim().isEmpty()) {
            binding.titleInputLayout.error = "Title cannot be empty"
            isValid = false
        } else {
            binding.titleInputLayout.error = null
        }

        if (binding.categoryDropdown.text.toString().trim().isEmpty()) {
            binding.categoryInputLayout.error = "Please select a category"
            isValid = false
        } else {
            binding.categoryInputLayout.error = null
        }

        val amountText = binding.amountInput.text.toString().trim()
        if (amountText.isEmpty()) {
            binding.amountInputLayout.error = "Amount cannot be empty"
            isValid = false
        } else {
            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    binding.amountInputLayout.error = "Amount must be greater than 0"
                    isValid = false
                } else {
                    binding.amountInputLayout.error = null
                }
            } catch (e: NumberFormatException) {
                binding.amountInputLayout.error = "Invalid amount"
                isValid = false
            }
        }

        return isValid
    }

    private fun saveExpense() {
        val title = binding.titleInput.text.toString().trim()
        val category = binding.categoryDropdown.text.toString().trim()
        val amount = binding.amountInput.text.toString().trim().toDouble()
        val date = calendar.time
        val currentUsername = dataManager.getCurrentUsername()

        if (editingExpense != null) {
            val updatedExpense = editingExpense!!.copy(
                title = title,
                category = category,
                amount = amount,
                date = date,
                username = currentUsername
            )
            dataManager.updateExpense(updatedExpense)
            Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
        } else {
            val newExpense = Expense(
                id = dataManager.getNewExpenseId(),
                title = title,
                category = category,
                amount = amount,
                date = date,
                username = currentUsername
            )
            dataManager.addExpense(newExpense)
            Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show()
        }
    }
}