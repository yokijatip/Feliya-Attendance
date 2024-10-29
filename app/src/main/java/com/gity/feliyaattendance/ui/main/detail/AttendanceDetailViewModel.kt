package com.gity.feliyaattendance.ui.main.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.AttendanceDetail
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AttendanceDetailViewModel(private val repository: Repository): ViewModel() {
    private val _attendanceDetail = MutableLiveData<AttendanceDetail?>()
    val attendanceDetail: LiveData<AttendanceDetail?> get() = _attendanceDetail

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
}