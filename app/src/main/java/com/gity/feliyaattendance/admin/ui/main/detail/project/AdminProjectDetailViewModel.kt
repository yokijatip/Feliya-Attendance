package com.gity.feliyaattendance.admin.ui.main.detail.project

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.Project
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AdminProjectDetailViewModel(private val repository: Repository) : ViewModel() {
    private val _projectDetail = MutableLiveData<Project?>()
    val projectDetail: MutableLiveData<Project?> get() = _projectDetail

    fun fetchProjectDetail(projectId: String) {
        viewModelScope.launch {
            try {
                val detail = repository.getDetailProject(projectId).getOrNull()
                _projectDetail.postValue(detail)
            } catch (e: Exception) {
                _projectDetail.postValue(null)
            }
        }
    }

    fun updateProjectStatus(projectId: String, newStatus: String) {
        viewModelScope.launch {
            val result = repository.updateProjectStatus(projectId, newStatus)
            // Handle the result if needed (e.g., show a message)
            if (result.isFailure) {
                Log.e("AdminProjectDetailViewModel", "Failed to update project status: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}