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

    private val _workersCount = MutableLiveData<Result<Int>>()
    val workersCount: LiveData<Result<Int>> = _workersCount

    private val _projectCount = MutableLiveData<Result<Int>>()
    val projectCount: LiveData<Result<Int>> = _projectCount

    private var nameCache: String? = null

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

    // Mengambil jumlah pekerja
    fun fetchWorkersCount() {
        viewModelScope.launch {
            val result = repository.getWorkersCount()
            _workersCount.value = result
        }
    }

    fun fetchProjectCount() {
        viewModelScope.launch {
            viewModelScope.launch {
                val result = repository.getProjectCount()
                _projectCount.value = result
            }
        }
    }

}