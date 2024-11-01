package com.gity.feliyaattendance.admin.ui.main.detail.worker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.admin.data.model.MonthlyDashboard
import com.gity.feliyaattendance.admin.data.model.Worker
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AdminWorkerDetailViewModel(private val repository: Repository) : ViewModel() {
    private val _workerDetailResult = MutableLiveData<Result<Worker>>()
    val workerDetailResult: LiveData<Result<Worker>> = _workerDetailResult

    private val _monthlyDashboardResult = MutableLiveData<Result<MonthlyDashboard>>()
    val monthlyDashboardResult: LiveData<Result<MonthlyDashboard>> = _monthlyDashboardResult

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
}