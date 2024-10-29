package com.gity.feliyaattendance.ui.main.home

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
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.adapter.AttendanceAdapter
import com.gity.feliyaattendance.databinding.FragmentHomeBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.main.attendance.ClockOutActivity
import com.gity.feliyaattendance.ui.main.attendance.ShowProjectActivity
import com.gity.feliyaattendance.ui.main.detail.AttendanceDetailActivity
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var repository: Repository
    private lateinit var viewModel: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        //        Start Of Your Code

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        val adapter = AttendanceAdapter { attendance ->
            navigateToDetailAttendance(attendance.attendanceId)
        }

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.fetchAttendanceList(userId)

        binding.rvYourActivity.layoutManager = LinearLayoutManager(requireContext())
        binding.rvYourActivity.adapter = adapter

        viewModel.attendanceList.observe(viewLifecycleOwner) { result ->
            result.onSuccess { attendance ->
                adapter.submitList(attendance)
                Log.i("ATTENDANCE_DATA", "Data : ${attendance.size}")
            }.onFailure { exception ->
                Log.e("ATTENDANCE_DATA", "Error fetching active projects", exception)
            }
        }
        viewModel.fetchAttendanceList(userId)

        binding.apply {
            btnNotification.setOnClickListener {
                Toast.makeText(requireContext(), "Clicked", Toast.LENGTH_SHORT).show()
            }

            tvTopDay.text = CommonHelper.getCurrentDayOnly()
            tvTopDate.text = CommonHelper.getCurrentDateOnly()
            tvGreetings.text = CommonHelper.getGreetingsMessage(
                getString(R.string.greetings_good_morning),
                getString(R.string.greetings_good_afternoon),
                getString(R.string.greetings_good_evening)
            )

            btnClockIn.setOnClickListener {
                navigateToShowProject()
            }

            btnClockOut.setOnClickListener {
                navigateToClockOut()
            }
        }

        viewModel.fetchName()
        viewModel.nameResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { name ->
                binding.tvName.text = name
            }
            result.onFailure { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load name: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        swipeRefreshLayout(adapter)
        return binding.root
    }

    private fun navigateToClockOut() {
        val intent = Intent(requireActivity(), ClockOutActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToShowProject() {
        val intent = Intent(requireActivity(), ShowProjectActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDetailAttendance(attendanceId: String) {
        val intent = Intent(requireActivity(), AttendanceDetailActivity::class.java)
        intent.putExtra("ATTENDANCE_ID", attendanceId)
        startActivity(intent)
    }

    private fun swipeRefreshLayout(adapter: AttendanceAdapter) {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.fetchAttendanceList(firebaseAuth.currentUser?.uid ?: "")
                viewModel.attendanceList.observe(viewLifecycleOwner) { result ->
                    result.onSuccess { attendance ->
                        adapter.submitList(attendance)
                        Log.i("ATTENDANCE_DATA", "Data : ${attendance.size}")
                    }.onFailure { exception ->
                        Log.e("ATTENDANCE_DATA", "Error fetching active projects", exception)
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
                Toast.makeText(requireContext(), "Refreshed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}