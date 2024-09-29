package com.gity.feliyaattendance.admin.ui.main.projects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch
import java.util.Date

class AdminProjectViewModel(private val repository: Repository) : ViewModel() {

    private val _saveProjectResult = MutableLiveData<Result<Unit>>()
    val saveProjectResult: LiveData<Result<Unit>> = _saveProjectResult

    fun addProject(
        projectName: String,
        location: String,
        startDate: Date,
        endDate: Date,
        status: String,
        description: String
    ) {
        viewModelScope.launch {
            val result = repository.addProject(
                projectName, location, startDate, endDate, status, description
            )
            _saveProjectResult.postValue(result)
        }
    }
}
