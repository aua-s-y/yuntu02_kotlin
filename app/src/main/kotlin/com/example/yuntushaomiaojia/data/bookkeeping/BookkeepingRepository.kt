package com.example.yuntushaomiaojia.data.bookkeeping

import android.content.Context
import java.util.Locale

class BookkeepingRepository(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun addExpense(amount: Double, category: String, note: String, timeText: String) {
        val line = "$category  $note  ¥${String.format(Locale.CHINA, "%.2f", amount)}  $timeText"
        val oldRecords = preferences.getString(KEY_EXPENSE_RECORDS, "").orEmpty()
        val total = preferences.getFloat(KEY_EXPENSE_TOTAL, 0f) + amount.toFloat()
        preferences.edit()
            .putString(KEY_EXPENSE_RECORDS, "$line\n$oldRecords")
            .putFloat(KEY_EXPENSE_TOTAL, total)
            .apply()
    }

    fun clearExpenses() {
        preferences.edit()
            .remove(KEY_EXPENSE_RECORDS)
            .remove(KEY_EXPENSE_TOTAL)
            .apply()
    }

    fun getSummaryText(totalPrefix: String, emptyRecordsText: String): String {
        val total = preferences.getFloat(KEY_EXPENSE_TOTAL, 0f)
        val records = preferences.getString(KEY_EXPENSE_RECORDS, "").orEmpty()
        val displayRecords = records.ifEmpty { emptyRecordsText }
        return "$totalPrefix¥${String.format(Locale.CHINA, "%.2f", total)}\n\n$displayRecords"
    }

    companion object {
        private const val PREFS_NAME = "tool_activity_prefs"
        private const val KEY_EXPENSE_RECORDS = "expense_records"
        private const val KEY_EXPENSE_TOTAL = "expense_total"
    }
}
