package com.example.yuntushaomiaojia.model

import androidx.annotation.IdRes
import com.example.yuntushaomiaojia.R
enum class MainPage(@param:IdRes val menuItemId: Int) {
    HOME(R.id.menu_home),
    COMMON_TOOLS(R.id.menu_common_tools),
    QUICK_TOOLS(R.id.menu_quick_tools);

    companion object {
        fun fromMenuItemId(@IdRes menuItemId: Int): MainPage? {
            return values().firstOrNull { page -> page.menuItemId == menuItemId }
        }
    }
}
