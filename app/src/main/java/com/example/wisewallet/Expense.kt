package com.example.wisewallet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Expense(
    val id: Long,
    val title: String,
    val category: String,
    val amount: Double,
    val date: Date,
    val username: String = "" // Add username field for additional safety
) : Parcelable {
    // For proper JSON serialization of Date objects
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "category" to category,
            "amount" to amount,
            "date" to date.time,
            "username" to username
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Expense {
            return Expense(
                id = (map["id"] as Double).toLong(),
                title = map["title"] as String,
                category = map["category"] as String,
                amount = map["amount"] as Double,
                date = Date((map["date"] as Double).toLong()),
                username = map["username"] as? String ?: ""
            )
        }
    }
}