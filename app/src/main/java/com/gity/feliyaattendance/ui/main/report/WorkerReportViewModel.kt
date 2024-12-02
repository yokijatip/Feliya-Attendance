package com.gity.feliyaattendance.ui.main.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.Attendance
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch
import java.util.Calendar

class WorkerReportViewModel(private val repository: Repository) : ViewModel() {

    private var _getAllAttendance = MutableLiveData<Result<List<Attendance>>>()
    val getAllAttendance: LiveData<Result<List<Attendance>>> get() = _getAllAttendance

    // Live data for monthly work hours
    private var _monthlyWorkHours = MutableLiveData<String>()
    val monthlyWorkHours: LiveData<String> = _monthlyWorkHours

    // Live data for monthly overtime
    private var _monthlyOvertime = MutableLiveData<String>()
    val monthlyOvertime: LiveData<String> = _monthlyOvertime

    fun getAllAttendance(workerId: String, year: Int, month: Int) {
        viewModelScope.launch {
            val result = repository.getAllAttendanceByMonthAndYear(workerId, year, month)
            _getAllAttendance.postValue(result)
        }
    }

    // Method to calculate monthly work hours
    fun calculateMonthlyWorkHours(workerId: String) {
        viewModelScope.launch {
            // Get current year and month
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Note: Calendar.MONTH is 0-indexed

            // Call repository method to calculate work hours
            val result = repository.calculateMonthlyWorkHours(workerId, currentYear, currentMonth)

            result.onSuccess { (workHours, overtimeHours) ->
                _monthlyWorkHours.postValue(workHours)
                _monthlyOvertime.postValue(overtimeHours)
            }.onFailure {
                // Optionally handle failure, e.g., set default values
                _monthlyWorkHours.postValue("0:00")
                _monthlyOvertime.postValue("0:00")
            }
        }
    }


}