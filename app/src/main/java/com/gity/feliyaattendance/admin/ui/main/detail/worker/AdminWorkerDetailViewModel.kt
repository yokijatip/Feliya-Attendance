package com.gity.feliyaattendance.admin.ui.main.detail.worker

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.admin.data.model.Worker
import com.gity.feliyaattendance.repository.Repository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.io.File

class AdminWorkerDetailViewModel(private val repository: Repository) : ViewModel() {
    private val _workerDetailResult = MutableLiveData<Result<Worker>>()
    val workerDetailResult: LiveData<Result<Worker>> = _workerDetailResult

    private val _excelGenerationResult = MutableLiveData<Result<File>>()
    val excelGenerationResult: LiveData<Result<File>> = _excelGenerationResult

    private val _deleteAccountStatus = MutableLiveData<Result<Unit>>()
    val deleteAccountStatus: LiveData<Result<Unit>> get() = _deleteAccountStatus

    private val _updateStatusAccount = MutableLiveData<Result<Unit>>()
    val updateStatusAccount: LiveData<Result<Unit>> get() = _updateStatusAccount

    // Fungsi untuk mengambil detail pekerja berdasarkan workerId
    fun fetchWorkerDetail(workerId: String) {
        viewModelScope.launch {
            val result = repository.getWorkerDetail(workerId)
            _workerDetailResult.postValue(result)
        }
    }

    //    mengubah Status Account
    fun updateStatusAccount(userId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                val result = repository.updateStatusAccount(userId, newStatus)
                _updateStatusAccount.postValue(result)
            } catch (e: Exception) {
                _updateStatusAccount.postValue(Result.failure(e))
            }
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
            val attendanceResult =
                repository.generateExcelReport(userId, startTimestamp, endTimestamp)

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

    fun deleteWorkerAccount(workerId: String) {
        viewModelScope.launch {
            val result = repository.deleteWorker(workerId)
            _deleteAccountStatus.postValue(result)
        }
    }


}