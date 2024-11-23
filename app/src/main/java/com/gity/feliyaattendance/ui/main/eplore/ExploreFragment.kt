package com.gity.feliyaattendance.ui.main.eplore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.adapter.AdminAnnouncementAdapter
import com.gity.feliyaattendance.admin.ui.main.announcement.AdminAnnouncementViewModel
import com.gity.feliyaattendance.databinding.FragmentExploreBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date


class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminAnnouncementViewModel
    private lateinit var adapter: AdminAnnouncementAdapter
    private var selectedDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)

        setupViewModel()
        setupRecyclerView()
        setupSwipeRefreshLayout()
        observeAnnouncements()
        setupFilterButton()

        viewModel.fetchAnnouncements()
        return binding.root
    }

    private fun setupSwipeRefreshLayout() {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.fetchAnnouncements()
            }
        }
    }

    //    Setup RecyclerView
    private fun setupRecyclerView() {
        adapter = AdminAnnouncementAdapter { announcement ->
            // Handle announcement click if needed
            val intent = Intent(requireActivity(), AnnouncementDetailActivity::class.java)
            intent.putExtra("announcementId", announcement.id)
            startActivity(intent)
        }
        binding.rvAnnouncement.adapter = adapter
        binding.rvAnnouncement.layoutManager = LinearLayoutManager(requireContext())

        val dividerItemDecoration =
            DividerItemDecoration(binding.rvAnnouncement.context, LinearLayoutManager.VERTICAL)
        binding.rvAnnouncement.addItemDecoration(dividerItemDecoration)
    }


    //    Setup Observer Announcement
    private fun observeAnnouncements() {
        viewModel.announcementList.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                adapter.submitList(result.getOrNull())
                binding.tvNoAnnouncement.visibility =
                    if (result.getOrNull()?.isEmpty() == true) View.VISIBLE
                    else View.GONE
                // Stop refreshing animation
                binding.swipeRefreshLayout.isRefreshing = false
            }.onFailure {
                CommonHelper.showInformationFailedDialog(
                    requireContext(),
                    getString(R.string.failed),
                    getString(R.string.no_announcement_description)
                )
                // Stop refreshing animation
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    //    Setup ViewModel
    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AdminAnnouncementViewModel::class.java]
    }


    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_month))
                .build()

            datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
                selectedDate = Date(selectedDateInMillis)
                viewModel.fetchAnnouncements(selectedDate)
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}