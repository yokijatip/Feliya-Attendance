package com.gity.feliyaattendance.admin.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.ui.main.announcement.AdminAnnouncementActivity
import com.gity.feliyaattendance.admin.ui.main.home.workers.AdminListWorkerActivity
import com.gity.feliyaattendance.admin.ui.main.projects.AdminAddProjectActivity
import com.gity.feliyaattendance.databinding.FragmentAdminHomeBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var repository: Repository
    private lateinit var viewModel: AdminHomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)

        initializeFirebase()
        setupViewModel()
        setupUI()
        setupObservers()

        return binding.root
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        repository = Repository(firebaseAuth, firebaseFirestore)
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminHomeViewModel::class.java]
    }

    private fun setupUI() {
        setupTopBar()
        setupSwipeRefresh()
        handleMenuClick()
    }

    private fun handleMenuClick() {
        binding.apply {
            btnListWorker.setOnClickListener {
                navigateToWorkerList()
            }
            btnAddProject.setOnClickListener {
                navigateToAddProject()
            }
            btnLeaveApplication.setOnClickListener {
                navigateToLeaveApplication()
            }
            btnAnnouncement.setOnClickListener {
                navigateToAnnouncement()
            }
            btnHistoryAttendance.setOnClickListener {
                navigateToHistoryAttendance()
            }
        }
    }

    private fun setupTopBar() {
        binding.apply {
            tvTopDay.text = CommonHelper.getCurrentDayOnly()
            tvTopDate.text = CommonHelper.getCurrentDateOnly()
            tvGreeting.text = CommonHelper.getGreetingsMessage(
                getString(R.string.greetings_good_morning),
                getString(R.string.greetings_good_afternoon),
                getString(R.string.greetings_good_evening)
            )
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                refreshData()
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.apply {
            observeNameResult()
            observeDashboardData()
        }
    }

    private fun observeNameResult() {
        viewModel.nameResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { name ->
                binding.tvWorkerName.text = name
            }.onFailure { exception ->
                showToast("Failed to load name: ${exception.message}")
            }
        }
    }

    private fun observeDashboardData() {
        binding.apply {
            viewModel.workersCount.observe(viewLifecycleOwner) { result ->
                handleResult(result) { count ->
                    tvWorker.text = count.toString()
                }
            }

            viewModel.projectCount.observe(viewLifecycleOwner) { result ->
                handleResult(result) { count ->
                    tvProject.text = count.toString()
                }
            }

            viewModel.attendancePending.observe(viewLifecycleOwner) { result ->
                handleResult(result) { count ->
                    tvPending.text = count.toString()
                    // Hide refresh indicator after all data is loaded
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun <T> handleResult(result: Result<T>, onSuccess: (T) -> Unit) {
        result.onSuccess(onSuccess).onFailure { exception ->
            Log.e("HomeFragment", "Error Message = $exception")
        }
    }


    private fun navigateToWorkerList() {
        Intent(requireActivity(), AdminListWorkerActivity::class.java).also {
            startActivity(it)
        }
    }

    private fun navigateToAddProject() {
        Intent(requireActivity(), AdminAddProjectActivity::class.java).also {
            startActivity(it)
        }
    }

    private fun navigateToLeaveApplication() {
        CommonHelper.showToast(requireContext(), "Under Maintenance")
    }

    private fun navigateToAnnouncement() {
        startActivity(Intent(requireActivity(), AdminAnnouncementActivity::class.java))
    }

    private fun navigateToHistoryAttendance() {
        CommonHelper.showToast(requireContext(), "Under Maintenance")
    }

    private fun refreshData() {
        viewModel.apply {
            fetchName()
            refreshWorkersCount()
            refreshProjectCount()
            refreshAttendancePending()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}