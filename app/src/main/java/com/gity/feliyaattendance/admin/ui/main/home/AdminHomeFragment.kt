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
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.adapter.MenuAdminAdapter
import com.gity.feliyaattendance.admin.data.model.AdminMenu
import com.gity.feliyaattendance.admin.ui.main.home.workers.AdminListWorkerActivity
import com.gity.feliyaattendance.admin.ui.main.projects.AdminAddProjectActivity
import com.gity.feliyaattendance.databinding.FragmentAdminHomeBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.helper.HorizontalSpaceItemDecoration
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
    private lateinit var adminMenuAdapter: MenuAdminAdapter


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
        setupRecyclerView()
        setupTopBar()
        setupSwipeRefresh()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.rvMenu.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(HorizontalSpaceItemDecoration(24))
            adapter = createMenuAdapter()
        }
    }


    private fun createMenuAdapter(): MenuAdminAdapter {
        val adminMenuList = listOf(
            AdminMenu(name = getString(R.string.admin_menu_see_worker_list), R.drawable.ic_users),
            AdminMenu(name = getString(R.string.admin_menu_add_worker), R.drawable.ic_user_plus),
            AdminMenu(name = getString(R.string.admin_menu_add_project), R.drawable.ic_folder_plus),
            AdminMenu(
                name = getString(R.string.admin_menu_attendance_approval),
                R.drawable.ic_check_square_custom_admin
            ),
            AdminMenu(name = getString(R.string.admin_menu_generate_excel), R.drawable.ic_table),
            AdminMenu(name = getString(R.string.admin_menu_generate_pdf), R.drawable.ic_file_text)
        )

        return MenuAdminAdapter(adminMenuList) { menu ->
            handleMenuClick(menu)
        }.also { adminMenuAdapter = it }
    }

    private fun handleMenuClick(menu: AdminMenu) {
        when (menu.name) {
            getString(R.string.admin_menu_see_worker_list) -> navigateToWorkerList()
            getString(R.string.admin_menu_add_worker) -> showToast("Add Worker")
            getString(R.string.admin_menu_add_project) -> navigateToAddProject()
            getString(R.string.admin_menu_attendance_approval) -> showToast("Attendance Approval")
            getString(R.string.admin_menu_generate_excel) -> showToast("Generate Report Excel")
            getString(R.string.admin_menu_generate_pdf) -> showToast("Generate PDF")
        }
    }

    private fun setupTopBar() {
        binding.apply {
            tvTopDay.text = CommonHelper.getCurrentDayOnly()
            tvTopDate.text = CommonHelper.getCurrentDateOnly()
            tvGreetings.text = CommonHelper.getGreetingsMessage(
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

    private fun setupClickListeners() {
        binding.btnNotification.setOnClickListener {
            showToast("Notifications Clicked")
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
                binding.tvName.text = name
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