package com.gity.feliyaattendance.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.admin.ui.main.announcement.AdminAnnouncementViewModel
import com.gity.feliyaattendance.admin.ui.main.attendances.AdminAttendanceViewModel
import com.gity.feliyaattendance.admin.ui.main.detail.announcement.AdminAnnouncementDetailViewModel
import com.gity.feliyaattendance.admin.ui.main.detail.attendance.AdminAttendanceDetailViewModel
import com.gity.feliyaattendance.admin.ui.main.detail.project.AdminProjectDetailViewModel
import com.gity.feliyaattendance.admin.ui.main.detail.worker.AdminWorkerDetailViewModel
import com.gity.feliyaattendance.admin.ui.main.history.attendance.HistoryAttendanceViewModel
import com.gity.feliyaattendance.admin.ui.main.home.AdminHomeViewModel
import com.gity.feliyaattendance.admin.ui.main.home.workers.AdminWorkersViewModel
import com.gity.feliyaattendance.admin.ui.main.projects.AdminProjectViewModel
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.ui.auth.AuthViewModel
import com.gity.feliyaattendance.ui.main.attendance.AttendanceViewModel
import com.gity.feliyaattendance.ui.main.detail.AttendanceDetailViewModel
import com.gity.feliyaattendance.ui.main.home.HomeViewModel
import com.gity.feliyaattendance.ui.main.settings.account.AccountViewModel

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
        } else if (modelClass.isAssignableFrom(AdminAttendanceDetailViewModel::class.java)) {
            return AdminAttendanceDetailViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AttendanceDetailViewModel::class.java)) {
            return AttendanceDetailViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AdminWorkersViewModel::class.java)) {
            return AdminWorkersViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AdminWorkerDetailViewModel::class.java)) {
            return AdminWorkerDetailViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AdminAnnouncementViewModel::class.java)) {
            return AdminAnnouncementViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AdminAnnouncementDetailViewModel::class.java)) {
            return AdminAnnouncementDetailViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(AdminProjectDetailViewModel::class.java)) {
            return AdminProjectDetailViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(HistoryAttendanceViewModel::class.java)) {
            return HistoryAttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}