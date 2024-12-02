package com.gity.feliyaattendance.ui.main.report

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.adapter.WorkerDetailAdapter
import com.gity.feliyaattendance.data.model.DetailWorkerMenu
import com.gity.feliyaattendance.databinding.ActivityWorkerReportBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.main.report.activity.WorkerActivityActivity
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkerReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerReportBinding
    private lateinit var viewModel: WorkerReportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWorkerReportBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI()
        setupViewModel()
        val workerId = FirebaseAuth.getInstance().currentUser?.uid
        CommonHelper.showToast(this, workerId!!)
        viewModel.calculateMonthlyWorkHours(FirebaseAuth.getInstance().currentUser?.uid.toString())
        observerMonthlyTotalHoursAndOvertime()
        workerReportMenuSetup()
    }

    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WorkerReportViewModel::class.java]
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

    private fun workerReportMenuSetup() {
        val workerReportMenuList = listOf(
            DetailWorkerMenu(getString(R.string.history_activity), R.drawable.ic_stats_report)
        )

        val workerReportMenuAdapter = WorkerDetailAdapter(workerReportMenuList) { menu ->
            when (menu.tvDetailWorkerMenu) {
                getString(R.string.history_activity) -> {
                    navigateToHistoryActivity()
                }
            }
        }

        binding.rvWorkerReportMenu.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workerReportMenuAdapter
        }
    }

    private fun navigateToHistoryActivity() {
        try {
            startActivity(Intent(this@WorkerReportActivity, WorkerActivityActivity::class.java))
        } catch (e: Exception) {
            Log.e("WORKER_REPORT", "Error ${e.message}")
        }
    }

    private fun observerMonthlyTotalHoursAndOvertime() {
        viewModel.monthlyWorkHours.observe(this@WorkerReportActivity) {
            binding.tvTotalHours.text = it
        }

        viewModel.monthlyOvertime.observe(this@WorkerReportActivity) {
            binding.tvTotalOvertime.text = it
        }
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