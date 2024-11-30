package com.gity.feliyaattendance.admin.ui.main.history.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.Attendance
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class HistoryAttendanceViewModel(private val repository: Repository) : ViewModel() {

    private var _historyAttendanceApproved = MutableLiveData<Result<List<Attendance>>>()
    val historyAttendanceApproved: LiveData<Result<List<Attendance>>> get() = _historyAttendanceApproved

    private var _historyAttendanceRejected = MutableLiveData<Result<List<Attendance>>>()
    val historyAttendanceRejected: LiveData<Result<List<Attendance>>> get() = _historyAttendanceRejected

    private var _historyAttendancePending = MutableLiveData<Result<List<Attendance>>>()
    val historyAttendancePending: LiveData<Result<List<Attendance>>> get() = _historyAttendancePending

    fun fetchApprovedAttendanceByMonthAndYear(year: Int, month: Int) {
        viewModelScope.launch {
            val result = repository.getApprovedAttendanceByMonthAndYear(year, month)
            _historyAttendanceApproved.postValue(result)
        }
    }

    fun fetchRejectedAttendanceByMonthAndYear(year: Int, month: Int) {
        viewModelScope.launch {
            val result = repository.getRejectedAttendanceByMonthAndYear(year, month)
            _historyAttendanceRejected.postValue(result)
        }
    }

    fun fetchPendingAttendanceByMonthAndYear(year: Int, month: Int) {
        viewModelScope.launch {
            val result = repository.getPendingAttendanceByMonthAndYear(year, month)
            _historyAttendancePending.postValue(result)
        }
    }

}