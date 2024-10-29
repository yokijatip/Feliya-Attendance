package com.gity.feliyaattendance.admin.ui.main.projects

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAdminAddProjectBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AdminAddProjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAddProjectBinding
    private lateinit var statusProjectAdapter: ArrayAdapter<String>
    private var startDate: Date? = null
    private var endDate: Date? = null
    private val calendar = Calendar.getInstance()

    private lateinit var viewModel: AdminProjectViewModel
    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminProjectViewModel::class.java]

        setupUI()
        setupStatusProjectDropdown()
        setupListeners()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.saveProjectResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Project added successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity after successful save
            }
            result.onFailure { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        handleBackButton()
    }

    private fun setupStatusProjectDropdown() {
        statusProjectAdapter =
            ArrayAdapter(this, R.layout.list_item_roles, setupStatusProject())
        binding.edtStatusProject.setAdapter(statusProjectAdapter)
    }

    private fun setupStatusProject(): List<String> {
        return listOf(
            getString(R.string.status_project_active),
            getString(R.string.status_project_inactive),
            getString(R.string.status_project_completed)
        )
    }

    private fun setupListeners() {
        binding.apply {
            btnStartCalendar.setOnClickListener {
                showDatePicker { date ->
                    binding.tvStartDate.text =
                        SimpleDateFormat("dd - MMMM - yyyy", Locale.getDefault()).format(date)
                    startDate = date
                }
            }
            btnEndCalendar.setOnClickListener {
                showDatePicker { date ->
                    binding.tvEndDate.text =
                        SimpleDateFormat("dd - MMMM - yyyy", Locale.getDefault()).format(date)
                    endDate = date
                }
            }

            btnSave.setOnClickListener {
                if (validateInputs()) {
                    viewModel.addProject(
                        binding.edtNameProject.text.toString(),
                        binding.edtLocationProject.text.toString(),
                        startDate ?: Date(),
                        endDate ?: Date(),
                        binding.edtStatusProject.text.toString(),
                        binding.edtDescriptionProject.text.toString()
                    )
                }
            }


        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validasi Project Name
        if (binding.edtNameProject.text.isNullOrEmpty()) {
            binding.edtNameProjectLayout.error = getString(R.string.error_empty_project_name)
            isValid = false
        } else {
            binding.edtNameProjectLayout.error = null
        }

        // Validasi Location
        if (binding.edtLocationProject.text.isNullOrEmpty()) {
            binding.edtLocationProjectLayout.error = getString(R.string.error_empty_location)
            isValid = false
        } else {
            binding.edtLocationProjectLayout.error = null
        }

        // Validasi Status Project
        if (binding.edtStatusProject.text.isNullOrEmpty()) {
            binding.edtStatusProjectLayout.error = getString(R.string.error_empty_status)
            isValid = false
        } else {
            binding.edtStatusProjectLayout.error = null
        }

        return isValid
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePickerDialog = DatePickerDialog(
            this, { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }
                onDateSelected(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun handleBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        onBackPressedDispatcher.addCallback {
            finish()
        }
    }
}

