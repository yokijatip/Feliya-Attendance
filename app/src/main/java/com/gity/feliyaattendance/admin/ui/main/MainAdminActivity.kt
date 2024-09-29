package com.gity.feliyaattendance.admin.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.ui.main.attendances.AdminAttendancesFragment
import com.gity.feliyaattendance.admin.ui.main.home.AdminHomeFragment
import com.gity.feliyaattendance.admin.ui.main.projects.AdminProjectsFragment
import com.gity.feliyaattendance.admin.ui.main.workers.AdminWorkersFragment
import com.gity.feliyaattendance.databinding.ActivityMainAdminBinding
import com.gity.feliyaattendance.ui.main.settings.SettingsFragment

class MainAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        replaceFragment(AdminHomeFragment())

        binding.apply {
            chipNavigationBar.setItemSelected(R.id.home, true)
            chipNavigationBar.setOnItemSelectedListener {
                when (it) {
                    R.id.home -> replaceFragment(AdminHomeFragment())
                    R.id.attendances -> replaceFragment(AdminAttendancesFragment())
                    R.id.projects -> replaceFragment(AdminProjectsFragment())
                    R.id.settings -> replaceFragment(SettingsFragment())
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