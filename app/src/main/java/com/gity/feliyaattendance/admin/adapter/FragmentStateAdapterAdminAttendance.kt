package com.gity.feliyaattendance.admin.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gity.feliyaattendance.admin.ui.main.attendances.approved.AdminAttendanceApprovedFragment
import com.gity.feliyaattendance.admin.ui.main.attendances.pending.AdminAttendancePendingFragment
import com.gity.feliyaattendance.admin.ui.main.attendances.rejected.AdminAttendanceRejectedFragment

class FragmentStateAdapterAdminAttendance(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminAttendancePendingFragment()
            1 -> AdminAttendanceApprovedFragment()
            2 -> AdminAttendanceRejectedFragment()
            else -> AdminAttendancePendingFragment()
        }
    }

}