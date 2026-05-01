package com.example.yuntushaomiaojia.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yuntushaomiaojia.model.MainPage
class MainViewModel : ViewModel() {

    private val _selectedPage = MutableLiveData(MainPage.HOME)
    val selectedPage: LiveData<MainPage> = _selectedPage

    fun selectPage(@IdRes menuItemId: Int): Boolean {
        val page = MainPage.fromMenuItemId(menuItemId) ?: return false
        if (_selectedPage.value != page) {
            _selectedPage.value = page
        }
        return true
    }
}
