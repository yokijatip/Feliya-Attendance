package com.gity.feliyaattendance.admin.ui.main.attendances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.admin.adapter.FragmentStateAdapterAdminAttendance
import com.gity.feliyaattendance.databinding.FragmentAdminAttendancesBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class AdminAttendancesFragment : Fragment() {

    private var _binding: FragmentAdminAttendancesBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAttendancesBinding.inflate(inflater, container, false)
//        Start Your Code
        binding.apply {
            val tabLayout = tabLayout
            val viewPager2 = pager
            val adapter = FragmentStateAdapterAdminAttendance(requireActivity())
            viewPager2.adapter = adapter

            TabLayoutMediator(tabLayout, pager) { tab: TabLayout.Tab, position: Int ->
                when (position) {
                    0 -> tab.text = getString(R.string.status_pending)
                    1 -> tab.text = getString(R.string.status_approved)
                    2 -> tab.text = getString(R.string.status_rejected)
                }
            }.attach()

        }
//        End Your Code
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding ke null untuk menghindari memory leak
        _binding = null
    }


}