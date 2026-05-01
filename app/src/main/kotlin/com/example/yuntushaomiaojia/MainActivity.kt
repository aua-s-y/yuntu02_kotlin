package com.example.yuntushaomiaojia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yuntushaomiaojia.databinding.ActivityMainBinding
import com.example.yuntushaomiaojia.model.MainPage
import com.example.yuntushaomiaojia.ui.common.CommonToolsFragment
import com.example.yuntushaomiaojia.ui.home.HomeFragment
import com.example.yuntushaomiaojia.ui.quick.QuickToolsFragment
import com.example.yuntushaomiaojia.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        applySystemInsets()
        observePage()
        setupBottomNavigation()
        openDefaultPage(savedInstanceState)
    }

    private fun applySystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun observePage() {
        viewModel.selectedPage.observe(this) { page ->
            replaceContentFragment(createFragment(page))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            viewModel.selectPage(item.itemId)
        }
    }

    private fun openDefaultPage(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.menu_home
        }
    }

    private fun createFragment(page: MainPage): Fragment {
        return when (page) {
            MainPage.HOME -> HomeFragment()
            MainPage.COMMON_TOOLS -> CommonToolsFragment()
            MainPage.QUICK_TOOLS -> QuickToolsFragment()
        }
    }

    private fun replaceContentFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
