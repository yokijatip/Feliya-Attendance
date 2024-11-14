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
import com.gity.feliyaattendance.adapter.ProjectAdapter
import com.gity.feliyaattendance.databinding.FragmentAdminProjectsBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminProjectsFragment : Fragment() {

    private var _binding: FragmentAdminProjectsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var repository: Repository
    private lateinit var viewModel: AdminProjectViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdminProjectsBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminProjectViewModel::class.java]

        val adapter = ProjectAdapter { selectedProject ->
            Toast.makeText(
                requireContext(),
                "Selected Project: ${selectedProject.projectName}",
                Toast.LENGTH_SHORT
            ).show()
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

        viewModel.projects.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                val projectList = result.getOrNull()
                projectList?.let {
                    adapter.submitList(it)
                }
            }.onFailure {
                Log.e("PROJECTS_DATA", "Failed to Load projects | Error : ${it.message}")
            }
        }

        viewModel.fetchProjects("desc")

        return binding.root
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