package com.gity.feliyaattendance.ui.main.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.Attendance
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class WorkerReportViewModel(private val repository: Repository) : ViewModel() {

    private var _getAllAttendance = MutableLiveData<Result<List<Attendance>>>()
    val getAllAttendance: LiveData<Result<List<Attendance>>> get() = _getAllAttendance

    fun getAllAttendance(workerId: String, year: Int, month: Int) {
        viewModelScope.launch {
            val result = repository.getAllAttendanceByMonthAndYear(workerId, year, month)
            _getAllAttendance.postValue(result)
        }
    }

}