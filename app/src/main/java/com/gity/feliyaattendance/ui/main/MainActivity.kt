package com.gity.feliyaattendance.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityMainBinding
import com.gity.feliyaattendance.ui.main.eplore.ExploreFragment
import com.gity.feliyaattendance.ui.main.home.HomeFragment
import com.gity.feliyaattendance.ui.main.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        replaceFragment(HomeFragment())

        binding.apply {
            chipNavigationBar.setItemSelected(R.id.home, true)
            chipNavigationBar.setOnItemSelectedListener {
                when (it) {
                    R.id.home -> replaceFragment(HomeFragment())
                    R.id.explores -> replaceFragment(ExploreFragment())
                    R.id.settings -> replaceFragment(SettingsFragment())

                    else -> {
                        Toast.makeText(this@MainActivity, "Error Navigation Button", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}