package com.example.yuntushaomiaojia.data.notebook

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotebookRepository(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCurrentNote(): String {
        return preferences.getString(KEY_NOTE, "").orEmpty()
    }

    fun saveNote(content: String) {
        val oldHistory = preferences.getString(KEY_NOTE_HISTORY, "").orEmpty()
        val newRecord = "${noteTimeText()}\n$content\n\n"
        preferences.edit()
            .putString(KEY_NOTE, content)
            .putString(KEY_NOTE_HISTORY, newRecord + oldHistory)
            .apply()
    }

    fun getNoteHistory(): String {
        return preferences.getString(KEY_NOTE_HISTORY, "").orEmpty()
    }

    private fun noteTimeText(): String {
        return SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date())
    }

    companion object {
        private const val PREFS_NAME = "toolbox_prefs"
        private const val KEY_NOTE = "saved_notebook"
        private const val KEY_NOTE_HISTORY = "saved_notebook_history"
    }
}
