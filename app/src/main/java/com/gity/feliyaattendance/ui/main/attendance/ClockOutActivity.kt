package com.gity.feliyaattendance.ui.main.attendance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityClockOutBinding
import com.gity.feliyaattendance.ui.main.MainActivity
import com.gity.feliyaattendance.utils.ProjectDataStoreManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ClockOutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClockOutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityClockOutBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        displayProjectData()


        binding.apply {
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnClockOut.setOnClickListener {
                onClockOutSuccess()
            }
        }

    }

    private fun displayProjectData() {
        lifecycleScope.launch {
            val dataStoreManager = ProjectDataStoreManager(this@ClockOutActivity)

            val projectName = dataStoreManager.projectName.firstOrNull() ?: "No Project Name"
            val projectLocation = dataStoreManager.projectLocation.firstOrNull() ?: "No Location"

            binding.tvProjectName.text = projectName
            binding.tvLocation.text = projectLocation

            Toast.makeText(
                this@ClockOutActivity,
                "Project Data: name = $projectName, location = $projectLocation",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun onClockOutSuccess() {
        lifecycleScope.launch {
            val dataStoreManager = ProjectDataStoreManager(this@ClockOutActivity)
            dataStoreManager.clearProjectData()
            startActivity(Intent(this@ClockOutActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }
    }
}