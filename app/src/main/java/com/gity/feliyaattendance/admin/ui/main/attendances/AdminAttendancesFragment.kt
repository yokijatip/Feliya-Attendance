package com.gity.feliyaattendance.admin.ui.main.attendances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.adapter.FragmentStateAdapterAdminAttendance
import com.gity.feliyaattendance.databinding.FragmentAdminAttendancesBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AdminAttendancesFragment : Fragment() {

    private var _binding: FragmentAdminAttendancesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminAttendanceViewModel
    private lateinit var pagerAdapter: FragmentStateAdapterAdminAttendance


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAttendancesBinding.inflate(inflater, container, false)
//        Start Your Code
        setupViewModel()
        setupViewPager()
        setupSwipeRefresh()
//        End Your Code
        return binding.root
    }

    private fun setupViewPager() {
        pagerAdapter = FragmentStateAdapterAdminAttendance(requireActivity())
        binding.apply {
            pager.adapter = pagerAdapter

            TabLayoutMediator(tabLayout, pager) { tab: TabLayout.Tab, position: Int ->
                when (position) {
                    0 -> tab.text = getString(R.string.status_pending)
                    1 -> tab.text = getString(R.string.status_approved)
                    2 -> tab.text = getString(R.string.status_rejected)
                }
            }.attach()
            pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    swipeRefreshLayout.isEnabled = positionOffset == 0f && positionOffsetPixels == 0
                }
            })
        }
    }

    private fun setupViewModel() {
        val repository = Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val factory = ViewModelFactory(repository)
        // Menggunakan requireActivity() untuk membuat ViewModel di scope Activity
        viewModel = ViewModelProvider(
            requireActivity(),
            factory
        )[AdminAttendanceViewModel::class.java]
    }

    private fun setupSwipeRefresh() {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                refreshCurrentFragment()
            }
        }
    }

    private fun refreshCurrentFragment() {
        val currentPosition = binding.pager.currentItem
        when (currentPosition) {
            0 -> viewModel.fetchAttendancesAdminStatusPendingList()
            1 -> viewModel.fetchAttendancesAdminStatusApprovedList()
            2 -> viewModel.fetchAttendancesAdminStatusRejectedList()
        }
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding ke null untuk menghindari memory leak
        _binding = null
    }


}