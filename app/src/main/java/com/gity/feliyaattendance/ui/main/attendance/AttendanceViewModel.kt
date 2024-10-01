package com.gity.feliyaattendance.ui.main.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.Project
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AttendanceViewModel(private val repository: Repository) : ViewModel() {
    private val _activeProjects = MutableLiveData<Result<List<Project>>>()
    val activeProjects: LiveData<Result<List<Project>>> get() = _activeProjects

    // Fetch active projects from repository
    fun fetchActiveProjects() {
        viewModelScope.launch {
            val result = repository.getActiveProjects()
            _activeProjects.value = result
        }
    }
}