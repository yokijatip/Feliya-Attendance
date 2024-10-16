package com.gity.feliyaattendance.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.admin.ui.main.attendances.AdminAttendanceViewModel
import com.gity.feliyaattendance.admin.ui.main.home.AdminHomeViewModel
import com.gity.feliyaattendance.admin.ui.main.projects.AdminProjectViewModel
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.auth.AuthViewModel
import com.gity.feliyaattendance.ui.main.attendance.AttendanceViewModel
import com.gity.feliyaattendance.ui.main.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AdminHomeViewModel::class.java)) {
            return AdminHomeViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AdminProjectViewModel::class.java)) {
            return AdminProjectViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            return AttendanceViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AdminAttendanceViewModel::class.java)) {
            return AdminAttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}