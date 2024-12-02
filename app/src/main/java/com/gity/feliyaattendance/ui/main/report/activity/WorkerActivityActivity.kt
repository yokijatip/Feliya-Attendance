package com.gity.feliyaattendance.ui.main.report.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.adapter.AttendanceAdapter
import com.gity.feliyaattendance.databinding.ActivityWorkerActivityBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.main.detail.AttendanceDetailActivity
import com.gity.feliyaattendance.ui.main.report.WorkerReportViewModel
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WorkerActivityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerActivityBinding
    private lateinit var viewModel: WorkerReportViewModel

    private lateinit var adapter: AttendanceAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var currentYear: Int = 0
    private var currentMonth: Int = 0

    private lateinit var workerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWorkerActivityBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI()
        setupViewModel()

        workerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Use Firebase Timestamp to get current date
        val currentTimestamp = Timestamp.now()
        val currentDate = currentTimestamp.toDate()
        val calendar = Calendar.getInstance().apply { time = currentDate }

        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1

        updateDateDisplay(currentYear, currentMonth)
        fetchAttendanceByMonthAndYear(workerId, currentYear, currentMonth)
        observerData()
        setupRecyclerView()
        setupSwipeRefreshLayout(workerId)
        setupEditDate(workerId)
    }

    private fun setupSwipeRefreshLayout(userId: String) {
        swipeRefreshLayout = binding.swipeRefreshLayout.apply {
            setColorSchemeResources(
                R.color.primary_color,
                R.color.secondary_color,
                R.color.accent_color
            )
            isMotionEventSplittingEnabled = false
            setOnRefreshListener {
                fetchAttendanceByMonthAndYear(userId, currentYear, currentMonth)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupEditDate(userId: String) {
        binding.moreMenu.setOnClickListener {
            val currentTimestamp = Timestamp.now()
            val currentDate = currentTimestamp.toDate()
            val calendar = Calendar.getInstance().apply { time = currentDate }

            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            val datePickerDialog = DatePickerDialog(
                this@WorkerActivityActivity,
                { _, selectedYear, selectedMonth, _ ->
                    // Update current year and month
                    this.currentYear = selectedYear
                    this.currentMonth = selectedMonth + 1

                    // Update date display with full month name and year
                    updateDateDisplay(selectedYear, selectedMonth + 1)

                    // Fetch attendance for the selected month and year
                    fetchAttendanceByMonthAndYear(userId, selectedYear, selectedMonth + 1)
                },
                currentYear,
                currentMonth,
                1
            )
            datePickerDialog.show()
        }
    }

    private fun fetchAttendanceByMonthAndYear(userId: String, year: Int, month: Int) {
        viewModel.getAllAttendance(userId, year, month)
    }

    private fun observerData() {
        viewModel.getAllAttendance.observe(this) { result ->
            result.onSuccess { attendanceList ->
                Log.d("ATTENDANCE_DATA", "Attendance List Size: ${attendanceList.size}")
                adapter.submitList(attendanceList)
                swipeRefreshLayout.isRefreshing = false
            }.onFailure { exception ->
                Log.e("ATTENDANCE_DATA", "Error Fetching approved attendance", exception)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun updateDateDisplay(year: Int, month: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
        }
        val dateFormat = SimpleDateFormat("MMMM, yyyy", Locale("id", "ID"))
        binding.tvDate.text = dateFormat.format(calendar.time)
    }

    private fun setupRecyclerView() {
        adapter = AttendanceAdapter { attendance ->
            navigateToDetail(attendance.attendanceId)
        }
        binding.rvAttendanceApproved.adapter = adapter
        binding.rvAttendanceApproved.layoutManager =
            LinearLayoutManager(this@WorkerActivityActivity)
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

    private fun navigateToDetail(attendanceId: String) {
        try {
            val intent = Intent(this, AttendanceDetailActivity::class.java).apply {
                putExtra("ATTENDANCE_ID", attendanceId)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("WORKER_REPORT", "Error ${e.message}")
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