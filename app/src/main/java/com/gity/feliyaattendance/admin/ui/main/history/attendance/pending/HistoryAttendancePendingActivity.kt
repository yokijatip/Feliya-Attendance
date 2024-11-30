package com.gity.feliyaattendance.admin.ui.main.history.attendance.pending

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
import com.gity.feliyaattendance.admin.adapter.AdminAttendanceStatusAdapter
import com.gity.feliyaattendance.admin.ui.main.detail.attendance.AdminAttendanceDetailActivity
import com.gity.feliyaattendance.admin.ui.main.history.attendance.HistoryAttendanceViewModel
import com.gity.feliyaattendance.databinding.ActivityHistoryAttendancePendingBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryAttendancePendingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryAttendancePendingBinding
    private lateinit var viewModel: HistoryAttendanceViewModel
    private lateinit var adapter: AdminAttendanceStatusAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var currentYear: Int = 0
    private var currentMonth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryAttendancePendingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSwipeRefreshLayout()
        setupUI()
        setupViewModel()
        setupRecyclerView()
        setupEditDate()

        // Use Firebase Timestamp to get current date
        val currentTimestamp = Timestamp.now()
        val currentDate = currentTimestamp.toDate()
        val calendar = Calendar.getInstance().apply { time = currentDate }

        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1

        updateDateDisplay(currentYear, currentMonth)
        fetchAttendanceByMonthAndYear(currentYear, currentMonth)
        observerData()

    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout = binding.swipeRefreshLayout.apply {
            setColorSchemeResources(
                R.color.primary_color,
                R.color.secondary_color,
                R.color.accent_color
            )
            isMotionEventSplittingEnabled = false
            setOnRefreshListener {
                fetchAttendanceByMonthAndYear(currentYear, currentMonth)
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

    private fun setupUI() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        handleBackButton()
    }

    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HistoryAttendanceViewModel::class.java]
    }

    private fun handleBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback {
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupEditDate() {
        binding.moreMenu.setOnClickListener {
            val currentTimestamp = Timestamp.now()
            val currentDate = currentTimestamp.toDate()
            val calendar = Calendar.getInstance().apply { time = currentDate }

            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            val datePickerDialog = DatePickerDialog(
                this@HistoryAttendancePendingActivity,
                { _, selectedYear, selectedMonth, _ ->
                    // Update current year and month
                    this.currentYear = selectedYear
                    this.currentMonth = selectedMonth + 1

                    // Update date display with full month name and year
                    updateDateDisplay(selectedYear, selectedMonth + 1)

                    // Fetch attendance for the selected month and year
                    fetchAttendanceByMonthAndYear(selectedYear, selectedMonth + 1)
                },
                currentYear,
                currentMonth,
                1
            )
            datePickerDialog.show()
        }
    }

    private fun fetchAttendanceByMonthAndYear(year: Int, month: Int) {
        viewModel.fetchPendingAttendanceByMonthAndYear(year, month)
    }

    private fun observerData() {
        viewModel.historyAttendancePending.observe(this) { result ->
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

    private fun setupRecyclerView() {
        adapter = AdminAttendanceStatusAdapter { attendance ->
            navigateToDetail(attendance.attendanceId)
        }
        binding.rvAttendancePending.adapter = adapter
        binding.rvAttendancePending.layoutManager =
            LinearLayoutManager(this@HistoryAttendancePendingActivity)
    }

    private fun navigateToDetail(attendanceId: String) {
        val intent = Intent(this, AdminAttendanceDetailActivity::class.java).apply {
            putExtra("ATTENDANCE_ID", attendanceId)
        }
        startActivity(intent)
    }
}