package com.gity.feliyaattendance.admin.ui.main.projects

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

        adapter = AdminProjectAdapter { selectedProject ->
            Intent(requireActivity(), AdminProjectDetailActivity::class.java).apply {
                putExtra("PROJECT_ID", selectedProject.projectId)
                startActivity(this)
            }
        }

        binding.apply {
            rvProjects.layoutManager = LinearLayoutManager(requireContext())
            rvProjects.adapter = adapter

            btnAddProject.setOnClickListener {
                navigateToAddProject()
            }

            btnFilter.setOnClickListener {
                // Implement filter functionality here
                Toast.makeText(requireContext(), "Filter button clicked", Toast.LENGTH_SHORT).show()
            }
        }


        setupObserver()
        swipeToRefresh()
        viewModel.fetchProjects("asc")
        return binding.root
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
                viewModel.fetchProjects("asc")
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
        val intent = Intent(requireActivity(), AdminAddProjectActivity::class.java)
        startActivity(intent)
    }
}