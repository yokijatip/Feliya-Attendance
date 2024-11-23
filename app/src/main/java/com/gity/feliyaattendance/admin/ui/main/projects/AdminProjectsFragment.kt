package com.gity.feliyaattendance.admin.ui.main.projects

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.adapter.AdminProjectAdapter
import com.gity.feliyaattendance.admin.ui.main.detail.project.AdminProjectDetailActivity
import com.gity.feliyaattendance.databinding.FragmentAdminProjectsBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminProjectsFragment : Fragment() {

    private var _binding: FragmentAdminProjectsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminProjectAdapter
    private lateinit var viewModel: AdminProjectViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdminProjectsBinding.inflate(inflater, container, false)
        setupViewModel()
        setupUI()

        adapter = AdminProjectAdapter { selectedProject ->
            Intent(requireActivity(), AdminProjectDetailActivity::class.java).apply {
                putExtra("PROJECT_ID", selectedProject.projectId)
                startActivity(this)
            }
        }

        binding.apply {
            rvProjects.layoutManager = LinearLayoutManager(requireContext())
            rvProjects.adapter = adapter
        }

        setupObserver()
        swipeToRefresh()
        viewModel.fetchProjects("asc") // Ambil proyek awal
        return binding.root
    }

    private fun setupUI() {
        navigateToAddProject()
        setupButtonFilter()
    }

    private fun setupButtonFilter() {
        binding.apply {
            btnFilter.setOnClickListener {
                showDialogImage()
            }
        }
    }

    private fun showDialogImage() {
        val dialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.custom_dialog_menu_admin_project_filter)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_active).setOnClickListener {
            // Panggil fetchProjects dengan filter "Active"
            viewModel.fetchProjects("asc", "Active")
            dialog.dismiss() // Tutup dialog
        }
        dialog.findViewById<LinearLayout>(R.id.linear_layout_inactive).setOnClickListener {
            // Panggil fetchProjects dengan filter "Inactive"
            viewModel.fetchProjects("asc", "Inactive")
            dialog.dismiss() // Tutup dialog
        }
        dialog.findViewById<LinearLayout>(R.id.linear_layout_completed).setOnClickListener {
            // Panggil fetchProjects dengan filter "Completed"
            viewModel.fetchProjects("asc", "Completed")
            dialog.dismiss() // Tutup dialog
        }
        dialog.show()
    }

    private fun setupObserver() {
        viewModel.projects.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                val projectList = result.getOrNull()
                projectList?.let {
                    adapter.submitList(it)
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }.onFailure {
                Log.e("PROJECTS_DATA", "Failed to Load projects | Error : ${it.message}")
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun swipeToRefresh() {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.fetchProjects("asc") // Ambil proyek tanpa filter saat refresh
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminProjectViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToAddProject() {
        binding.apply {
            btnAddProject.setOnClickListener {
                val intent = Intent(requireActivity(), AdminAddProjectActivity::class.java)
                startActivity(intent)
            }
        }
    }
}