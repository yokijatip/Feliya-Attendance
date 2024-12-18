package com.gity.feliyaattendance.admin.ui.main.projects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.Project
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch
import java.util.Date

class AdminProjectViewModel(private val repository: Repository) : ViewModel() {

    private val _saveProjectResult = MutableLiveData<Result<Unit>>()
    val saveProjectResult: LiveData<Result<Unit>> = _saveProjectResult

    private val _projects = MutableLiveData<Result<List<Project>>>()
    val projects: LiveData<Result<List<Project>>> = _projects

    fun addProject(
        projectName: String,
        location: String,
        startDate: Date,
        endDate: Date,
        status: String,
        description: String,
        projectImage: String
    ) {
        viewModelScope.launch {
            val result = repository.addProject(
                projectName, location, startDate, endDate, status, description, projectImage
            )
            _saveProjectResult.postValue(result)
        }
    }

    // Modifikasi fungsi fetchProjects untuk menerima filterStatus
    fun fetchProjects(orderBy: String = "asc", filterStatus: String? = null) {
        viewModelScope.launch {
            try {
                // Panggil repository dengan filterStatus
                val result = repository.getAllProject(orderBy, filterStatus)
                _projects.value = result
            } catch (e: Exception) {
                _projects.postValue(Result.failure(e))
            }
        }
    }
}
