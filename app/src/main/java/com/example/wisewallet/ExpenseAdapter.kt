package com.example.wisewallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wisewallet.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseAdapter(
    private val expenses: List<Expense>,
    private val onEditClick: (position: Int) -> Unit,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.editButton.setOnClickListener {
                onEditClick(adapterPosition)
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(adapterPosition)
            }
        }

        fun bind(expense: Expense) {
            // Format the date
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val formattedDate = dateFormat.format(expense.date)

            // Format the amount
            val currencyFormatter = NumberFormat.getCurrencyInstance()
            val formattedAmount = currencyFormatter.format(expense.amount)

            // Set data to views
            binding.expenseTitle.text = expense.title
            binding.expenseCategory.text = expense.category
            binding.expenseDate.text = formattedDate
            binding.expenseAmount.text = formattedAmount
        }
    }
}