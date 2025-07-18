package com.gity.feliyaattendance.admin.ui.main.analytics.clustering

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.admin.data.model.DateRange
import com.gity.feliyaattendance.admin.data.model.PerformanceSummary
import com.gity.feliyaattendance.admin.data.model.WorkerPerformanceData
import com.gity.feliyaattendance.databinding.ActivityWorkerAnalysisBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.TensorFlowLiteHelper
import com.gity.feliyaattendance.utils.WorkerAnalysisViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Calendar

class WorkerAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerAnalysisBinding
    private lateinit var viewModel: WorkerAnalysisViewModel
    private lateinit var workerAdapter: WorkerPerformanceAdapter

    private val TAG = "WorkerAnalysisActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Activity created")

        // Initialize dependencies manually
        initializeDependencies()

        setupUI()
        setupObservers()

        // Show date range dialog after a short delay to ensure UI is ready
        binding.root.post {
            showDateRangeDialog()
        }
    }

    private fun initializeDependencies() {
        // Initialize Firebase instances
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseFirestore = FirebaseFirestore.getInstance()

        // Initialize Repository
        val repository = Repository(firebaseAuth, firebaseFirestore)

        // Initialize TensorFlow Lite Helper
        val tensorFlowHelper = TensorFlowLiteHelper(this)

        // Initialize ViewModel with custom factory
        val viewModelFactory = WorkerAnalysisViewModelFactory(repository, tensorFlowHelper)
        viewModel = ViewModelProvider(this, viewModelFactory)[WorkerAnalysisViewModel::class.java]
    }

    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Worker Performance Analysis"

        // Setup RecyclerView
        workerAdapter = WorkerPerformanceAdapter { worker ->
            showWorkerDetails(worker)
        }

        binding.recyclerViewWorkers.apply {
            layoutManager = LinearLayoutManager(this@WorkerAnalysisActivity)
            adapter = workerAdapter
        }

        // Setup refresh button
        binding.btnRefresh.setOnClickListener {
            showDateRangeDialog()
        }

        // Setup filter buttons
        binding.btnFilterAll.setOnClickListener {
            filterWorkers("All")
        }

        binding.btnFilterHigh.setOnClickListener {
            filterWorkers("High Performer")
        }

        binding.btnFilterMedium.setOnClickListener {
            filterWorkers("Medium Performer")
        }

        binding.btnFilterLow.setOnClickListener {
            filterWorkers("Low Performer")
        }

        // Set initial filter state
        updateFilterButtonStates("All")
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(state: WorkerAnalysisUiState) {
        Log.d(
            TAG,
            "Updating UI - Loading: ${state.isLoading}, Error: ${state.error}, Results: ${state.analysisResult != null}"
        )

        // Show/hide loading
        binding.progressBar.visibility = if (state.isLoading) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }

        // Show error
        state.error?.let { error ->
            Log.e(TAG, "Showing error: $error")
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // Update analysis results
        state.analysisResult?.let { result ->
            Log.d(TAG, "Updating UI with ${result.workers.size} workers")
            updateSummaryCards(result.summary)
            workerAdapter.submitList(result.workers)

            // Update date range display
            state.dateRange?.let { dateRange ->
                binding.tvDateRange.text =
                    "Analysis Period: ${dateRange.startDate} to ${dateRange.endDate}"
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateSummaryCards(summary: PerformanceSummary) {
        Log.d(TAG, "Updating summary cards: ${summary.totalWorkers} total workers")

        binding.apply {
            tvTotalWorkers.text = summary.totalWorkers.toString()
            tvHighPerformers.text = summary.highPerformers.toString()
            tvMediumPerformers.text = summary.mediumPerformers.toString()
            tvLowPerformers.text = summary.lowPerformers.toString()
            tvAvgAttendance.text = String.format("%.1f%%", summary.averageAttendanceRate)
            tvAvgWorkHours.text = String.format("%.1f hrs", summary.averageWorkHours)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showDateRangeDialog() {
        Log.d(TAG, "Showing date range dialog")

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Set default to last 30 days
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        val defaultStartYear = calendar.get(Calendar.YEAR)
        val defaultStartMonth = calendar.get(Calendar.MONTH)
        val defaultStartDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Start date picker
        val startDatePicker = DatePickerDialog(this, { _, year, month, day ->
            val startDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            Log.d(TAG, "Start date selected: $startDate")

            // End date picker
            val endDatePicker = DatePickerDialog(this, { _, endYear, endMonth, endDay ->
                val endDate = String.format("%04d-%02d-%02d", endYear, endMonth + 1, endDay)
                Log.d(TAG, "End date selected: $endDate")

                val dateRange = DateRange(startDate, endDate)
                viewModel.analyzeWorkerPerformance(dateRange)

            }, currentYear, currentMonth, currentDay)

            endDatePicker.show()

        }, defaultStartYear, defaultStartMonth, defaultStartDay)

        startDatePicker.show()
    }

    private fun filterWorkers(performanceLevel: String) {
        Log.d(TAG, "Filtering workers by: $performanceLevel")

        val currentResult = viewModel.uiState.value.analysisResult
        if (currentResult != null) {
            val filteredWorkers = if (performanceLevel == "All") {
                currentResult.workers
            } else {
                viewModel.getWorkersByPerformance(performanceLevel)
            }

            Log.d(TAG, "Filtered to ${filteredWorkers.size} workers")
            workerAdapter.submitList(filteredWorkers)

            // Update filter button states
            updateFilterButtonStates(performanceLevel)
        }
    }

    private fun updateFilterButtonStates(selectedFilter: String) {
        binding.apply {
            btnFilterAll.isSelected = selectedFilter == "All"
            btnFilterHigh.isSelected = selectedFilter == "High Performer"
            btnFilterMedium.isSelected = selectedFilter == "Medium Performer"
            btnFilterLow.isSelected = selectedFilter == "Low Performer"
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showWorkerDetails(worker: WorkerPerformanceData) {
        Log.d(TAG, "Showing details for worker: ${worker.name}")

        val details = """
            Name: ${worker.name}
            Worker ID: ${worker.workerId}
            Performance: ${worker.performanceLabel}
            Confidence: ${String.format("%.1f%%", worker.confidence * 100)}
            
            Metrics:
            • Attendance Rate: ${String.format("%.1f%%", worker.attendanceRate)}
            • Avg Work Hours: ${String.format("%.1f hrs", worker.avgWorkHours)}
            • Punctuality Score: ${String.format("%.1f%%", worker.punctualityScore)}
            • Consistency Score: ${String.format("%.1f%%", worker.consistencyScore)}
            • Total Records: ${worker.totalRecords}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Worker Performance Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return true
    }
}