package com.gity.feliyaattendance.admin.ui.main.attendances.rejected

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.admin.adapter.AdminAttendanceStatusAdapter
import com.gity.feliyaattendance.admin.ui.main.attendances.AdminAttendanceViewModel
import com.gity.feliyaattendance.admin.ui.main.detail.attendance.AdminAttendanceDetailActivity
import com.gity.feliyaattendance.databinding.FragmentAdminAttendanceRejectedBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminAttendanceRejectedFragment : Fragment() {

    private var _binding: FragmentAdminAttendanceRejectedBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var repository: Repository
    private lateinit var viewModel: AdminAttendanceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdminAttendanceRejectedBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminAttendanceViewModel::class.java]

        val adapter = AdminAttendanceStatusAdapter { attendance ->
            Toast.makeText(requireContext(), attendance.attendanceId, Toast.LENGTH_SHORT).show()
            navigateToDetail()
        }

        binding.apply {
            rvAttendanceRejected.adapter = adapter
            rvAttendanceRejected.layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.attendanceAdminStatusRejectedList.observe(viewLifecycleOwner) { result ->
            result.onSuccess { attendance ->
                adapter.submitList(attendance)
                Log.i("ATTENDANCE_DATA", "Data: ${attendance.size}")
            }.onFailure { exception ->
                Log.e("ATTENDANCE_DATA", "Error fetching active projects", exception)
            }
        }

        viewModel.fetchAttendancesAdminStatusRejectedList()

        return binding.root
    }

    private fun navigateToDetail() {
        val startActivity = Intent(requireContext(), AdminAttendanceDetailActivity::class.java)
        startActivity(startActivity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}