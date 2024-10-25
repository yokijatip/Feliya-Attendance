package com.gity.feliyaattendance.admin.ui.main.detail.attendance

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAdminAttendanceDetailBinding

class AdminAttendanceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAttendanceDetailBinding

    //    Status Attendance Adapter
    private lateinit var statusAttendanceAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminAttendanceDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupWindowInsets()
        setupUi()
        setupListeners()
        setupStatusAdapter()
    }

    private fun setupUi() {
        updateStatusUI()
    }

    private fun updateStatusUI(status: String? = null) {
        val (backgroundColor, textColor) = when (status) {
            "pending" -> Pair(
                R.color.status_pending_background,
                R.color.status_pending
            )

            "rejected" -> Pair(
                R.color.status_rejected_background,
                R.color.status_rejected
            )

            "approved" -> Pair(
                R.color.status_approved_background,
                R.color.status_approved
            )

            else -> Pair(
                R.color.status_pending_background,
                R.color.status_pending
            )
        }

        binding.apply {
            // Update CardView background
            cardStatus.setBackgroundTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(this@AdminAttendanceDetailActivity, backgroundColor)
                )
            )

            // Update TextView color and text
            tvStatus.apply {
                setTextColor(
                    ContextCompat.getColor(
                        this@AdminAttendanceDetailActivity,
                        textColor
                    )
                )
                text = status ?: "pending"
            }
        }
    }

    private fun setupStatusAdapter() {
        statusAttendanceAdapter = ArrayAdapter(
            this,
            R.layout.list_item_roles,
            resources.getStringArray(R.array.status_attendance).toMutableList()
        )

        binding.edtStatus.apply {
            setAdapter(statusAttendanceAdapter)
            // Menambahkan listener untuk perubahan status
            setOnItemClickListener { _, _, _, _ ->
                val selectedStatus = text.toString()
                updateStatusUI(selectedStatus)
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnBack.setOnClickListener { finish() }

            btnSave.setOnClickListener {
                val statusText = edtStatus.text.toString()
                updateStatusUI(statusText)
                showToast("Status updated to: $statusText")
            }
        }
    }


    private fun setupWindowInsets() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}