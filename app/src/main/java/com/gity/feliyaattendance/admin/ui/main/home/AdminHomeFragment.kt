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
import androidx.recyclerview.widget.RecyclerView
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.adapter.MenuAdminAdapter
import com.gity.feliyaattendance.admin.data.model.AdminMenu
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

    private lateinit var recyclerView: RecyclerView
    private lateinit var adminMenuAdapter: MenuAdminAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminHomeViewModel::class.java]

        recyclerView = binding.rvMenu
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        dashboard()

        val adminMenuList = listOf(
            AdminMenu(name = getString(R.string.admin_menu_see_worker_list), R.drawable.ic_users),
            AdminMenu(name = getString(R.string.admin_menu_add_worker), R.drawable.ic_user_plus),
            AdminMenu(name = getString(R.string.admin_menu_add_project), R.drawable.ic_folder_plus),
            AdminMenu(
                name = getString(R.string.admin_menu_attendance_approval),
                R.drawable.ic_check_square_custom_admin
            ),
            AdminMenu(
                name = getString(R.string.admin_menu_generate_excel),
                R.drawable.ic_table
            ),
            AdminMenu(
                name = getString(R.string.admin_menu_generate_pdf),
                R.drawable.ic_file_text
            )


        )

        adminMenuAdapter = MenuAdminAdapter(adminMenuList) { menu ->
            when (menu.name) {
                getString(R.string.admin_menu_see_worker_list) -> Toast.makeText(
                    requireContext(),
                    "See Worker List",
                    Toast.LENGTH_SHORT
                ).show()

                getString(R.string.admin_menu_add_worker) -> Toast.makeText(
                    requireContext(),
                    "Add Worker",
                    Toast.LENGTH_SHORT
                ).show()

                getString(R.string.admin_menu_add_project) -> navigateToAddProject()

                getString(R.string.admin_menu_attendance_approval) -> Toast.makeText(
                    requireContext(),
                    "Attendance Approval",
                    Toast.LENGTH_SHORT
                ).show()

                getString(R.string.admin_menu_generate_excel) -> Toast.makeText(
                    requireContext(),
                    "Generate Report Excel",
                    Toast.LENGTH_SHORT
                ).show()

                getString(R.string.admin_menu_generate_pdf) -> Toast.makeText(
                    requireContext(),
                    "Generate PDF",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        recyclerView.addItemDecoration(HorizontalSpaceItemDecoration(24))
        recyclerView.adapter = adminMenuAdapter

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
        return binding.root
    }

    private fun dashboard() {
        binding.apply {
            viewModel.workersCount.observe(viewLifecycleOwner) { result ->
                result.onSuccess { count ->
                    tvWorker.text = count.toString()
                }.onFailure { exception ->
                    Log.e("HomeFragment", "Error Message = $exception")
                }
            }

            viewModel.projectCount.observe(viewLifecycleOwner) { result ->
                result.onSuccess { count ->
                    tvProject.text = count.toString()
                }.onFailure { exception ->
                    Log.e("HomeFragment", "Error Message = $exception")
                }
            }

            viewModel.attendancePending.observe(viewLifecycleOwner) { result ->
                result.onSuccess { count ->
                    tvPending.text = count.toString()
                }.onFailure { exception ->
                    Log.e("HomeFragment", "Error Message = $exception")
                }
            }
        }
    }

    private fun navigateToAddProject() {
        val intent = Intent(requireActivity(), AdminAddProjectActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}