package com.gity.feliyaattendance.admin.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.adapter.FeatureAdminAdapter
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

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminHomeViewModel::class.java]

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

        val featureNames = resources.getStringArray(R.array.features)
        val featureIcons = arrayOf(
            R.drawable.ic_user_plus,
            R.drawable.ic_folder_plus,
            R.drawable.ic_check_square_custom_admin,
            R.drawable.ic_printer,
        )

//        Setup Recycler View and Feature Adapter
        val featureAdapter = FeatureAdminAdapter(featureNames, featureIcons) { position ->
            handleFeatureClick(position)
        }

        binding.rvFeatures.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = featureAdapter
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

    private fun handleFeatureClick(position: Int) {
        // Handle clicks for each feature based on position
        when (position) {
            0 -> Toast.makeText(requireContext(), "Add Worker clicked", Toast.LENGTH_SHORT).show()
            1 -> navigateToAddProject()
            2 -> Toast.makeText(requireContext(), "Attendance Approval clicked", Toast.LENGTH_SHORT)
                .show()

            3 -> Toast.makeText(requireContext(), "Generate Report clicked", Toast.LENGTH_SHORT)
                .show()
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