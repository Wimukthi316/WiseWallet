package com.example.wisewallet

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.*

class DataManager(private val context: Context) {
    private val userPreferences: SharedPreferences =
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Dynamic SharedPreferences that always use current user
    private fun getSharedPreferences(): SharedPreferences {
        val username = getCurrentUsername()
        return context.getSharedPreferences("${username}_ExpensePrefs", Context.MODE_PRIVATE)
    }

    private fun getBudgetPrefs(): SharedPreferences {
        val username = getCurrentUsername()
        return context.getSharedPreferences("${username}_BudgetPrefs", Context.MODE_PRIVATE)
    }

    private fun getNotificationPrefs(): SharedPreferences {
        val username = getCurrentUsername()
        return context.getSharedPreferences("${username}_NotificationPrefs", Context.MODE_PRIVATE)
    }

    private val EXPENSES_KEY = "expenses"
    private val _expensesLiveData = MutableLiveData<List<Expense>>()

    // Expose expenses LiveData
    val expensesLiveData: LiveData<List<Expense>> = _expensesLiveData

    // Budget Preferences
    private val MONTHLY_BUDGET_KEY = "monthly_budget"
    private val CATEGORY_BUDGETS_KEY = "category_budgets"

    // Notification tracking
    private val MONTHLY_NOTIFIED_KEY = "monthly_notified"
    private val CATEGORY_NOTIFIED_KEY = "category_notified"

    // Notification constants
    private val CHANNEL_ID = "budget_alerts_channel"
    private val MONTHLY_NOTIFICATION_ID = 1001
    private val CATEGORY_NOTIFICATION_BASE_ID = 2000

    private val _budgetLiveData = MutableLiveData<BudgetData>()
    val budgetLiveData: LiveData<BudgetData> = _budgetLiveData
    private val currencyFormatter = NumberFormat.getCurrencyInstance()

    data class BudgetData(
        val monthlyBudget: Double = 0.0,
        val categoryBudgets: Map<String, Double> = emptyMap(),
        val categorySpending: Map<String, Double> = emptyMap()
    )

    init {
        // Initialize with current data
        updateExpensesData()
        updateBudgetData()
    }

    // User management methods
    fun getCurrentUser(): User? {
        val userJson = userPreferences.getString("CURRENT_USER", null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun getCurrentUsername(): String {
        return getCurrentUser()?.username ?: "DefaultUser"
    }

    // Method to clear user data when logging out
    fun clearCurrentUserData() {
        // Get current username before clearing
        val currentUsername = getCurrentUsername()

        // Clear all user-specific preferences
        context.getSharedPreferences("${currentUsername}_ExpensePrefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
        context.getSharedPreferences("${currentUsername}_BudgetPrefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
        context.getSharedPreferences("${currentUsername}_NotificationPrefs", Context.MODE_PRIVATE)
            .edit().clear().apply()

        // Clear current user from UserPrefs
        userPreferences.edit().remove("CURRENT_USER").apply()
    }

    // Method to refresh data when user changes
    fun refreshUserData() {
        updateExpensesData()
        updateBudgetData()
    }

    // Expense methods
    private fun updateExpensesData() {
        _expensesLiveData.postValue(loadExpenses())
    }

    fun saveExpenses(expenses: List<Expense>) {
        val json = gson.toJson(expenses)
        getSharedPreferences().edit().putString(EXPENSES_KEY, json).apply()
        updateExpensesData()
        updateBudgetData()
        checkBudgetExceedances()
    }

    fun loadExpenses(): List<Expense> {
        val json = getSharedPreferences().getString(EXPENSES_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<Expense>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun addExpense(expense: Expense) {
        val currentExpenses = loadExpenses().toMutableList()
        currentExpenses.add(expense)
        saveExpenses(currentExpenses)
    }

    fun updateExpense(updatedExpense: Expense) {
        val currentExpenses = loadExpenses().toMutableList()
        val index = currentExpenses.indexOfFirst { it.id == updatedExpense.id }
        if (index != -1) {
            currentExpenses[index] = updatedExpense
            saveExpenses(currentExpenses)
        }
    }

    fun deleteExpense(expenseId: Long) {
        val currentExpenses = loadExpenses().toMutableList()
        currentExpenses.removeAll { it.id == expenseId }
        saveExpenses(currentExpenses)
    }

    fun getNewExpenseId(): Long {
        return (loadExpenses().maxOfOrNull { it.id } ?: 0) + 1
    }

    // Budget methods
    fun updateBudgetData() {
        _budgetLiveData.postValue(BudgetData(
            monthlyBudget = getMonthlyBudget(),
            categoryBudgets = getCategoryBudgets(),
            categorySpending = getCategorySpending()
        ))
    }

    fun saveMonthlyBudget(amount: Double) {
        getBudgetPrefs().edit().putFloat(MONTHLY_BUDGET_KEY, amount.toFloat()).apply()
        // When budget is changed, reset notification status
        resetMonthlyNotificationStatus()
        updateBudgetData()
        checkBudgetExceedances()
    }

    fun getMonthlyBudget(): Double {
        return getBudgetPrefs().getFloat(MONTHLY_BUDGET_KEY, 0f).toDouble()
    }

    fun saveCategoryBudget(category: String, amount: Double) {
        val budgets = getCategoryBudgets().toMutableMap()
        budgets[category] = amount
        getBudgetPrefs().edit().putString(CATEGORY_BUDGETS_KEY, gson.toJson(budgets)).apply()
        // When budget is changed, reset notification status for this category
        resetCategoryNotificationStatus(category)
        updateBudgetData()
        checkBudgetExceedances()
    }

    fun getCategoryBudgets(): Map<String, Double> {
        val json = getBudgetPrefs().getString(CATEGORY_BUDGETS_KEY, null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<Map<String, Double>>() {}.type)
        } else {
            emptyMap()
        }
    }

    fun getCategorySpending(): Map<String, Double> {
        val expenses = loadExpenses()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return expenses
            .filter {
                val expenseCalendar = Calendar.getInstance().apply { time = it.date }
                expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                        expenseCalendar.get(Calendar.YEAR) == currentYear
            }
            .groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    fun onExpensesUpdated() {
        updateExpensesData()
        updateBudgetData()
        checkBudgetExceedances()
    }

    // New methods for notification management
    private fun resetAllNotificationStatus() {
        getNotificationPrefs().edit().clear().apply()
    }

    private fun resetMonthlyNotificationStatus() {
        getNotificationPrefs().edit().remove(MONTHLY_NOTIFIED_KEY).apply()
    }

    private fun resetCategoryNotificationStatus(category: String) {
        val notifiedCategories = getCategoryNotificationStatus()
        notifiedCategories.remove(category)
        saveCategoryNotificationStatus(notifiedCategories)
    }

    private fun markMonthlyBudgetNotified() {
        getNotificationPrefs().edit().putBoolean(MONTHLY_NOTIFIED_KEY, true).apply()
    }

    private fun isMonthlyBudgetNotified(): Boolean {
        return getNotificationPrefs().getBoolean(MONTHLY_NOTIFIED_KEY, false)
    }

    private fun getCategoryNotificationStatus(): MutableMap<String, Boolean> {
        val json = getNotificationPrefs().getString(CATEGORY_NOTIFIED_KEY, null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<MutableMap<String, Boolean>>() {}.type)
        } else {
            mutableMapOf()
        }
    }

    private fun saveCategoryNotificationStatus(statusMap: Map<String, Boolean>) {
        getNotificationPrefs().edit().putString(CATEGORY_NOTIFIED_KEY, gson.toJson(statusMap)).apply()
    }

    private fun markCategoryBudgetNotified(category: String) {
        val notifiedCategories = getCategoryNotificationStatus().toMutableMap()
        notifiedCategories[category] = true
        saveCategoryNotificationStatus(notifiedCategories)
    }

    private fun isCategoryBudgetNotified(category: String): Boolean {
        return getCategoryNotificationStatus()[category] ?: false
    }

    // Check if any budget has been exceeded and show notifications if needed
    fun checkBudgetExceedances() {
        val budgetData = BudgetData(
            monthlyBudget = getMonthlyBudget(),
            categoryBudgets = getCategoryBudgets(),
            categorySpending = getCategorySpending()
        )

        // Check monthly budget
        val monthlyBudget = budgetData.monthlyBudget
        val totalSpent = budgetData.categorySpending.values.sum()

        if (monthlyBudget > 0 && totalSpent > monthlyBudget && !isMonthlyBudgetNotified()) {
            showBudgetExceededNotification(MONTHLY_NOTIFICATION_ID,
                "Monthly Budget Exceeded",
                "You've exceeded your monthly budget by ${currencyFormatter.format(totalSpent - monthlyBudget)}")
            markMonthlyBudgetNotified()
        }

        // Check category budgets
        budgetData.categoryBudgets.forEach { (category, budget) ->
            val spent = budgetData.categorySpending[category] ?: 0.0
            if (budget > 0 && spent > budget && !isCategoryBudgetNotified(category)) {
                // Create unique ID for each category notification
                val notificationId = CATEGORY_NOTIFICATION_BASE_ID + category.hashCode()
                showBudgetExceededNotification(notificationId,
                    "$category Budget Exceeded",
                    "You've exceeded your $category budget by ${currencyFormatter.format(spent - budget)}")
                markCategoryBudgetNotified(category)
            }
        }
    }

    private fun showBudgetExceededNotification(notificationId: Int, title: String, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.Blue))
            .build()

        notificationManager.notify(notificationId, notification)
    }

    // Reset notifications for a new month
    fun resetMonthlyNotifications() {
        // This should be called on the first day of each month
        resetAllNotificationStatus()
    }
}