package com.gity.feliyaattendance.admin.ui.main.attendances.pending

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.admin.adapter.AdminAttendanceStatusAdapter
import com.gity.feliyaattendance.admin.ui.main.attendances.AdminAttendanceViewModel
import com.gity.feliyaattendance.admin.ui.main.detail.attendance.AdminAttendanceDetailActivity
import com.gity.feliyaattendance.databinding.FragmentAdminAttendancePendingBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AdminAttendancePendingFragment : Fragment() {

    private var _binding: FragmentAdminAttendancePendingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminAttendanceViewModel
    private lateinit var adapter: AdminAttendanceStatusAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAttendancePendingBinding.inflate(inflater, container, false)
        setupViewModel()
        setupRecyclerView()
        observeData()
        return binding.root
    }

    private fun observeData() {
        viewModel.attendanceList.observe(viewLifecycleOwner) { result ->
            result.onSuccess { attendance ->
                adapter.submitList(attendance)
                Log.i("ATTENDANCE_DATA", "Data: ${attendance.size}")
            }.onFailure { exception ->
                Log.e("ATTENDANCE_DATA", "Error fetching active projects", exception)
            }
        }
    }

    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(
            requireActivity(),
            factory
        )[AdminAttendanceViewModel::class.java]
    }


    private fun setupRecyclerView() {
        adapter = AdminAttendanceStatusAdapter { attendance ->
            navigateToDetail(attendance.attendanceId)
        }

        binding.rvAttendancePending.apply {
            adapter = this@AdminAttendancePendingFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun navigateToDetail(attendanceId: String) {
        val startActivity = Intent(requireContext(), AdminAttendanceDetailActivity::class.java)
        startActivity.putExtra("ATTENDANCE_ID", attendanceId)
        startActivity(startActivity)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchAttendancesAdminStatusPendingList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}