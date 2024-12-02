package com.gity.feliyaattendance.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.adapter.AttendanceAdapter
import com.gity.feliyaattendance.data.local.AttendanceDataStoreManager
import com.gity.feliyaattendance.databinding.FragmentHomeBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.main.attendance.ClockOutActivity
import com.gity.feliyaattendance.ui.main.attendance.ShowProjectActivity
import com.gity.feliyaattendance.ui.main.detail.AttendanceDetailActivity
import com.gity.feliyaattendance.ui.main.report.WorkerReportActivity
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var attendanceAdapter: AttendanceAdapter
    private lateinit var attendanceDataStoreManager: AttendanceDataStoreManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupDataStoreManager()
        setupRecyclerView()
        setupUI()
        observeClockTimes()
        observeData()
        setupSwipeRefresh()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchClockInAndClockOutTimes(attendanceDataStoreManager)
    }

    private fun observeClockTimes() {
        viewModel.clockInTime.observe(viewLifecycleOwner) { clockIn ->
            if (clockIn != null) {
                // Tampilkan waktu clock in
                val clockInFormattedTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(clockIn.toDate())
                binding.tvClockIn.text = clockInFormattedTime

                // Hitung waktu clock out yang diharapkan
                val expectedClockOutTime = calculateClockOutTime(clockIn)
                val clockOutFormattedTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(expectedClockOutTime)
                binding.tvClockOut.text = clockOutFormattedTime
            } else {
                // Reset kedua TextView jika tidak ada clock in
                binding.tvClockIn.text = getString(R.string.hours_default)
                binding.tvClockOut.text = getString(R.string.hours_default)
            }
        }

        viewModel.clockOutTime.observe(viewLifecycleOwner) { clockOut ->
            if (clockOut != null) {
                // Jika sudah clock out, reset kedua TextView
                binding.tvClockIn.text = getString(R.string.hours_default)
                binding.tvClockOut.text = getString(R.string.hours_default)
            }
        }
    }

    private fun calculateClockOutTime(clockIn: Timestamp): Date {
        val calendar = Calendar.getInstance()
        calendar.time = clockIn.toDate()
        calendar.add(Calendar.HOUR_OF_DAY, 9) // Add 8 hours for standard work day
        return calendar.time
    }

    private fun setupDataStoreManager() {
        attendanceDataStoreManager = AttendanceDataStoreManager(requireContext())
    }

    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[HomeViewModel::class.java]
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter { attendance ->
            navigateToDetailAttendance(attendance.attendanceId)
        }

        binding.rvYourActivity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceAdapter
        }
    }

    private fun setupUI() {
        binding.apply {
            tvTopDay.text = CommonHelper.getCurrentDayOnly()
            tvTopDate.text = CommonHelper.getCurrentDateOnly()
            tvGreeting.text = CommonHelper.getGreetingsMessage(
                getString(R.string.greetings_good_morning),
                getString(R.string.greetings_good_afternoon),
                getString(R.string.greetings_good_evening)
            )

            btnClockIn.setOnClickListener { navigateToShowProject() }
            btnClockOut.setOnClickListener { navigateToClockOut() }
            btnLeaveApplication.setOnClickListener { navigateToLeaveApplication() }
            btnReports.setOnClickListener { navigateToReportActivity() }
            viewAllActivity.setOnClickListener {
                navigateToViewAllActivity()
            }
        }
    }

    private fun observeData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        // Fetch Attendance List
        viewModel.fetchAttendanceList(userId)
        viewModel.attendanceList.observe(viewLifecycleOwner) { result ->
            result.onSuccess { attendance ->
                attendanceAdapter.submitList(attendance)
                Log.i("ATTENDANCE_DATA", "Data : ${attendance.size}")
            }.onFailure { exception ->
                Log.e("ATTENDANCE_DATA", "Error fetching attendance", exception)
            }
        }

        // Fetch Name
        viewModel.fetchName()
        viewModel.nameResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { name ->
                binding.tvWorkerName.text = name
            }.onFailure { exception ->
                Log.e("HomeFragment", "Error fetching name", exception)
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                viewModel.fetchAttendanceList(userId)

                viewModel.attendanceList.observe(viewLifecycleOwner) { result ->
                    result.onSuccess { attendance ->
                        attendanceAdapter.submitList(attendance)
                        Log.i("ATTENDANCE_DATA", "Data : ${attendance.size}")
                    }.onFailure { exception ->
                        Log.e("ATTENDANCE_DATA", "Error fetching attendance", exception)
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun navigateToClockOut() {
        startActivity(Intent(requireActivity(), ClockOutActivity::class.java))
    }

    private fun navigateToShowProject() {
        startActivity(Intent(requireActivity(), ShowProjectActivity::class.java))
    }

    private fun navigateToLeaveApplication() {
        CommonHelper.showToast(requireContext(), "Under Development")
    }

    private fun navigateToReportActivity() {
        startActivity(Intent(requireActivity(), WorkerReportActivity::class.java))
    }

    private fun navigateToDetailAttendance(attendanceId: String) {
        val intent = Intent(requireActivity(), AttendanceDetailActivity::class.java).apply {
            putExtra("ATTENDANCE_ID", attendanceId)
        }
        startActivity(intent)
    }

    private fun navigateToViewAllActivity() {
        startActivity(Intent(requireActivity(), WorkerReportActivity::class.java))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}