package com.gity.feliyaattendance.admin.ui.main.attendances

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.Attendance
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AdminAttendanceViewModel(private val repository: Repository) : ViewModel() {

    private var _attendanceAdminStatusPendingList = MutableLiveData<Result<List<Attendance>>>()
    val attendanceList: LiveData<Result<List<Attendance>>> get() = _attendanceAdminStatusPendingList

    private var _attendanceAdminStatusApprovedList = MutableLiveData<Result<List<Attendance>>>()
    val attendanceAdminStatusApprovedList: LiveData<Result<List<Attendance>>> get() = _attendanceAdminStatusApprovedList

    private var _attendanceAdminStatusRejectedList = MutableLiveData<Result<List<Attendance>>>()
    val attendanceAdminStatusRejectedList: LiveData<Result<List<Attendance>>> get() = _attendanceAdminStatusRejectedList

    fun fetchAttendancesAdminStatusPendingList() {
        viewModelScope.launch {
            val result = repository.getAllAttendancePending()
            _attendanceAdminStatusPendingList.value = result
        }
    }

    fun fetchAttendancesAdminStatusApprovedList() {
        viewModelScope.launch {
            val result = repository.getAllAttendanceApproved()
            _attendanceAdminStatusApprovedList.value = result
        }
    }

    fun fetchAttendancesAdminStatusRejectedList() {
        viewModelScope.launch {
            val result = repository.getAllAttendanceRejected()
            _attendanceAdminStatusRejectedList.value = result
        }
    }


}