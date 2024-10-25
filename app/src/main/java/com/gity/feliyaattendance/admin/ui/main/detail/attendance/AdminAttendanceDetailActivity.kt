package com.gity.feliyaattendance.admin.ui.main.detail.attendance

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAdminAttendanceDetailBinding

class AdminAttendanceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAttendanceDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminAttendanceDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        handleBackButton()

    }

    private fun handleBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}