package com.gity.feliyaattendance.admin.ui.main.detail.worker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.adapter.WorkerDetailAdapter
import com.gity.feliyaattendance.admin.data.model.MonthlyDashboard
import com.gity.feliyaattendance.admin.data.model.Worker
import com.gity.feliyaattendance.data.model.DetailWorkerMenu
import com.gity.feliyaattendance.databinding.ActivityAdminWorkerDetailBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.Date

class AdminWorkerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminWorkerDetailBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var viewModel: AdminWorkerDetailViewModel
    private lateinit var repository: Repository

    private lateinit var workerName: String
    private lateinit var workerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminWorkerDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        handleBackButton()

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminWorkerDetailViewModel::class.java]

//        Get Worker ID
        workerId = intent.getStringExtra("WORKER_ID")!!

//        Observer ViewModel
        observerDetailViewModel()
        workerDetailMenuSetup()

        if (workerId != null) {
            viewModel.fetchWorkerDetail(workerId)
            viewModel.fetchMonthlyDashboard(workerId)
        } else {
            Toast.makeText(this@AdminWorkerDetailActivity, "Invalid Worker ID", Toast.LENGTH_SHORT)
                .show()
            finish()
        }

        observeExcelGeneration()


    }

    private fun workerDetailMenuSetup() {
        val workerDetailMenuList = listOf(
            DetailWorkerMenu(getString(R.string.admin_menu_generate_excel), R.drawable.ic_table),
            DetailWorkerMenu(getString(R.string.admin_menu_generate_pdf), R.drawable.ic_file_text)
        )

        val detailWorkerMenuAdapter = WorkerDetailAdapter(workerDetailMenuList) { menu ->
            when (menu.tvDetailWorkerMenu) {
                getString(R.string.admin_menu_generate_excel) -> {
                    generateExcel(workerId)
                }

                getString(R.string.admin_menu_generate_pdf) -> {
                    Toast.makeText(
                        this@AdminWorkerDetailActivity,
                        "Generated PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.rvWorkerDetailMenu.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailWorkerMenuAdapter
        }
    }

    private fun generateExcel(workerId: String) {
        val datePicker = MaterialDatePicker.Builder
            .dateRangePicker()
            .setTitleText("Pilih Rentang Tanggal")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val startTimestamp = Timestamp(Date(selection.first))
            val endTimestamp = Timestamp(Date(selection.second))

            viewModel.generateAttendanceReport(
                context = this,
                userId = workerId,
                username = workerName,
                startTimestamp = startTimestamp,
                endTimestamp = endTimestamp
            )
        }

        datePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun observeExcelGeneration() {
        viewModel.excelGenerationResult.observe(this) { result ->
            result.onSuccess { file ->
//                Buka file atau share
                openExcelFile(file)
            }.onFailure {
                Toast.makeText(
                    this@AdminWorkerDetailActivity,
                    "Error generating Excel file: $it",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observerDetailViewModel() {
        viewModel.workerDetailResult.observe(this) { result ->
            result.onSuccess { worker ->
                displayWorkerDetails(worker)
            }.onFailure { exception ->
                Toast.makeText(
                    this@AdminWorkerDetailActivity,
                    "Error fetching Worker Detail : $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.monthlyDashboardResult.observe(this) { result ->
            result.onSuccess { worker ->
                displayMonthlyDashboard(worker)
            }.onFailure { exception ->
                Log.e("ADMIN_DETAIL_ACTIVITY", "Error fetching Monthly Dashboard : $exception")
            }
        }

        viewModel.monthlyDashboardResult.observe(this) { result ->
            result.onSuccess { dashboard ->
                Log.d("Activity", "Received dashboard data:")
                Log.d("Activity", "Total Attendance: ${dashboard.totalAttendance}")
                Log.d("Activity", "Total Overtime: ${dashboard.totalOvertimeHours}")

                // Update UI
                binding.tvAttendance.text = dashboard.totalAttendance.toString()
                binding.tvOvertime.text = dashboard.totalOvertimeHours.toString()
            }.onFailure { error ->
                Log.e("Activity", "Error displaying dashboard", error)
                // Show error message to user
            }
        }
    }

    private fun displayWorkerDetails(worker: Worker) {
        binding.apply {
            tvUserName.text = worker.name
            tvUserEmail.text = worker.email
            tvUserRole.text = worker.role
            workerName = worker.name!!

            Glide.with(this@AdminWorkerDetailActivity)
                .load(worker.profileImageUrl)
                .apply(
                    RequestOptions.placeholderOf(R.drawable.worker_profile_placeholder)
                        .error(R.drawable.worker_profile_placeholder)
                )
                .into(ivUserImage)
        }
    }

    private fun displayMonthlyDashboard(monthlyDashboard: MonthlyDashboard) {
        binding.apply {
            tvAttendance.text = monthlyDashboard.totalAttendance.toString()
            tvOvertime.text = monthlyDashboard.totalOvertimeHours.toString()
        }
    }

    private fun handleBackButton() {
        onBackPressedDispatcher.addCallback {
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    //    Fungsi untuk membuka file excel
    private fun openExcelFile(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(intent)
    }

    fun Date.toTimestamp(): Timestamp {
        return Timestamp(this)
    }
}