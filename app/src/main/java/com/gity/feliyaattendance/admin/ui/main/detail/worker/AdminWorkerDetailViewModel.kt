package com.gity.feliyaattendance.admin.ui.main.detail.worker

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.admin.data.model.MonthlyDashboard
import com.gity.feliyaattendance.admin.data.model.Worker
import com.gity.feliyaattendance.repository.Repository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.io.File

class AdminWorkerDetailViewModel(private val repository: Repository) : ViewModel() {
    private val _workerDetailResult = MutableLiveData<Result<Worker>>()
    val workerDetailResult: LiveData<Result<Worker>> = _workerDetailResult

    private val _monthlyDashboardResult = MutableLiveData<Result<MonthlyDashboard>>()
    val monthlyDashboardResult: LiveData<Result<MonthlyDashboard>> = _monthlyDashboardResult

    private val _excelGenerationResult = MutableLiveData<Result<File>>()
    val excelGenerationResult: LiveData<Result<File>> = _excelGenerationResult

    // Fungsi untuk mengambil detail pekerja berdasarkan workerId
    fun fetchWorkerDetail(workerId: String) {
        viewModelScope.launch {
            val result = repository.getWorkerDetail(workerId)
            _workerDetailResult.postValue(result)
        }
    }

    // Function to fetch monthly dashboard
    fun fetchMonthlyDashboard(userId: String) {
        viewModelScope.launch {
            val result = repository.getMonthlyDashboard(userId)
            _monthlyDashboardResult.postValue(result)
        }
    }

//    Generate Attendance Monthly Report
    fun generateAttendanceReport(
        context: Context,
        userId: String,
        username: String,
        startTimestamp: Timestamp,
        endTimestamp: Timestamp
    ) {
        viewModelScope.launch {
            val attendanceResult = repository.generateExcelReport(userId, startTimestamp, endTimestamp)

            attendanceResult.onSuccess { attendanceReports ->
                val excelResult = repository.generateExcelFile(
                    context,
                    userId,
                    username,
                    startTimestamp,
                    endTimestamp,
                    attendanceReports
                )

                _excelGenerationResult.postValue(excelResult)
            }.onFailure { error ->
                _excelGenerationResult.postValue(Result.failure(error))
                Log.e("AdminWorkerDetailViewModel", "Error generating Excel file: $error")
            }
        }
    }


}