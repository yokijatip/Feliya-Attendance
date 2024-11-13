package com.gity.feliyaattendance.admin.ui.main.detail.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.AttendanceDetail
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AdminAttendanceDetailViewModel(private val repository: Repository) : ViewModel() {
    private val _attendanceDetail = MutableLiveData<AttendanceDetail?>()
    val attendanceDetail: LiveData<AttendanceDetail?> get() = _attendanceDetail

    private val _updateStatusSuccess = MutableLiveData<Boolean>()
    val updateStatusSuccess: LiveData<Boolean> get() = _updateStatusSuccess

    private val _deleteAttendance = MutableLiveData<Unit>()
    val deleteAttendance: LiveData<Unit> get() = _deleteAttendance

    fun fetchAttendanceDetail(attendanceId: String) {
        viewModelScope.launch {
            try {
                val detail = repository.getAttendanceDetail(attendanceId).getOrNull()
                _attendanceDetail.postValue(detail)
            } catch (e: Exception) {
                _attendanceDetail.postValue(null)
            }
        }
    }

    fun updateAttendanceStatus(attendanceId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                val result = repository.updateAttendanceStatus(attendanceId, newStatus).isSuccess
                _updateStatusSuccess.postValue(result)
            } catch (e: Exception) {
                _updateStatusSuccess.postValue(false)
            }
        }
    }

    fun deleteAttendance(attendanceId: String) {
        viewModelScope.launch {
            repository.deleteAttendance(attendanceId)
        }
    }
}