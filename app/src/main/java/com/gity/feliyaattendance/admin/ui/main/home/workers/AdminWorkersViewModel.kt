package com.gity.feliyaattendance.admin.ui.main.home.workers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.admin.data.model.Worker
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AdminWorkersViewModel(private val repository: Repository) : ViewModel() {
    private var _workerlist = MutableLiveData<Result<List<Worker>>>()
    val workerlist: LiveData<Result<List<Worker>>> get() = _workerlist

    fun getWorkerList() {
        viewModelScope.launch {
            val result = repository.getWorkerList()
            _workerlist.postValue(result)
        }
    }


}