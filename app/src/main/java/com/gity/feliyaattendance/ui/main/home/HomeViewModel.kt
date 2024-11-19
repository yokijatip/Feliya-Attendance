package com.gity.feliyaattendance.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.local.AttendanceDataStoreManager
import com.gity.feliyaattendance.data.model.Attendance
import com.gity.feliyaattendance.repository.Repository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: Repository) : ViewModel() {
    private val _nameResult = MutableLiveData<Result<String>>()
    val nameResult: LiveData<Result<String>> = _nameResult

    private val _attendanceList = MutableLiveData<Result<List<Attendance>>>()
    val attendanceList: LiveData<Result<List<Attendance>>> get() = _attendanceList

    private var nameCache: String? = null

    private val _clockInTime = MutableLiveData<Timestamp?>()
    val clockInTime: LiveData<Timestamp?> get() = _clockInTime

    private val _clockOutTime = MutableLiveData<Timestamp?>()
    val clockOutTime: LiveData<Timestamp?> get() = _clockOutTime

    fun fetchClockInAndClockOutTimes(dataStoreManager: AttendanceDataStoreManager) {
        viewModelScope.launch {
            _clockInTime.value = dataStoreManager.clockIn.firstOrNull()
            _clockOutTime.value = dataStoreManager.clockOut.firstOrNull()
        }
    }

    //    Menggunakan sistem cache untuk menyimpan data
    fun fetchName() {
        if (nameCache != null) {
            _nameResult.value = Result.success(nameCache!!)
        } else {
            viewModelScope.launch {
                val result = repository.fetchName()
                if (result.isSuccess) {
                    nameCache = result.getOrNull()
                    _nameResult.value = result
                }
                _nameResult.value = result
            }
        }
    }

    fun fetchAttendanceList(userId: String) {
        viewModelScope.launch {
            val result = repository.getAttendanceByUserId(userId)
            _attendanceList.value = result
        }
    }

}