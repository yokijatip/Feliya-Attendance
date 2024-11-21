package com.gity.feliyaattendance.admin.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AdminHomeViewModel(private val repository: Repository) : ViewModel() {
    private val _nameResult = MutableLiveData<Result<String>>()
    val nameResult: LiveData<Result<String>> = _nameResult

    private val _emailResult = MutableLiveData<Result<String>>()
    val emailResult: LiveData<Result<String>> = _emailResult

    private val _workersCount = MutableLiveData<Result<Int>>()
    val workersCount: LiveData<Result<Int>> = _workersCount

    private val _projectCount = MutableLiveData<Result<Int>>()
    val projectCount: LiveData<Result<Int>> = _projectCount

    private val _attendancePending = MutableLiveData<Result<Int>>()
    val attendancePending: LiveData<Result<Int>> = _attendancePending

    init {
        refreshAllData()
    }

    fun refreshAllData() {
        fetchName()
        fetchEmail()
        refreshWorkersCount()
        refreshProjectCount()
        refreshAttendancePending()
    }

    //    Menggunakan sistem cache untuk menyimpan data
    fun fetchName() {
        viewModelScope.launch {
            try {
                val name = repository.fetchName()
                _nameResult.postValue(name)
            } catch (e: Exception) {
                _nameResult.postValue(e.message?.let { Result.failure(Exception(it)) })
            }
        }
    }

    fun fetchEmail() {
        viewModelScope.launch {
            try {
                val email = repository.fetchEmail()
                _emailResult.postValue(email)
            } catch (e: Exception) {
                _emailResult.postValue(e.message?.let { Result.failure(Exception(it)) })
            }
        }
    }

    fun refreshWorkersCount() {
        viewModelScope.launch {
            try {
                val count = repository.getWorkersCount()
                _workersCount.postValue(count)
            } catch (e: Exception) {
                _workersCount.value = Result.failure(e)
            }
        }
    }

    fun refreshProjectCount() {
        viewModelScope.launch {
            try {
                val count = repository.getProjectCount()
                _projectCount.postValue(count)
            } catch (e: Exception) {
                _projectCount.value = Result.failure(e)
            }
        }
    }

    fun refreshAttendancePending() {
        viewModelScope.launch {
            try {
                val count = repository.getAttendancePending()
                _attendancePending.postValue(count)
            } catch (e: Exception) {
                _attendancePending.value = Result.failure(e)
            }
        }
    }

}