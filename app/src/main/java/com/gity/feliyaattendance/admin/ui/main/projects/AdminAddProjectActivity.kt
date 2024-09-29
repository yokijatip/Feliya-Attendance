package com.gity.feliyaattendance.admin.ui.main.projects

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAdminAddProjectBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminAddProjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAddProjectBinding
    private val calendar = Calendar.getInstance()

    private lateinit var startDate: String
    private lateinit var endDate: String

    private lateinit var statusProjectAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminAddProjectBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        statusProjectAdapter = ArrayAdapter(this, R.layout.list_item_roles, mutableListOf())
        statusProjectAdapter.clear()
        statusProjectAdapter.addAll(setupStatusProject())
        statusProjectAdapter.notifyDataSetChanged()

        binding.apply {
            edtStatusProject.setAdapter(statusProjectAdapter)

            btnStartCalendar.setOnClickListener {
                showStartDatePicker()
            }
            btnEndCalendar.setOnClickListener {
                showEndDatePicker()
            }
        }
    }

    private fun setupStatusProject(): List<String> {
        val statusListProject = listOf(
            getString(R.string.status_project_active),
            getString(R.string.status_project_inactive),
            getString(R.string.status_project_completed),
        )

        return statusListProject
    }

    private fun showStartDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this, { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd - MMMM - yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.tvStartDate.text = formattedDate
                startDate = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showEndDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this, { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd - MMMM - yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.tvEndDate.text = formattedDate
                endDate = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

}