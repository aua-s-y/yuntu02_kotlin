package com.example.yuntushaomiaojia.data.travel

import android.content.Context

class TravelListRepository(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun addItem(item: String) {
        val oldItems = preferences.getString(KEY_TRAVEL_ITEMS, "").orEmpty()
        preferences.edit()
            .putString(KEY_TRAVEL_ITEMS, "$oldItems$ITEM_PREFIX$item\n")
            .apply()
    }

    fun clearItems() {
        preferences.edit().remove(KEY_TRAVEL_ITEMS).apply()
    }

    fun getItemsText(emptyText: String): String {
        val items = preferences.getString(KEY_TRAVEL_ITEMS, "").orEmpty()
        return items.ifEmpty { emptyText }
    }

    companion object {
        private const val PREFS_NAME = "tool_activity_prefs"
        private const val KEY_TRAVEL_ITEMS = "travel_items"
        private const val ITEM_PREFIX = "• "
    }
}
