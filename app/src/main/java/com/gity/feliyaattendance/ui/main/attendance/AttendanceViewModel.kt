package com.gity.feliyaattendance.ui.main.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.data.model.Project
import com.gity.feliyaattendance.repository.Repository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Date

class AttendanceViewModel(private val repository: Repository) : ViewModel() {
    private val _activeProjects = MutableLiveData<Result<List<Project>>>()
    val activeProjects: LiveData<Result<List<Project>>> get() = _activeProjects

    private val _attendanceResult = MutableLiveData<Result<Unit>>()
    val attendanceResult: LiveData<Result<Unit>> get() = _attendanceResult

    // Fetch active projects from repository
    fun fetchActiveProjects() {
        viewModelScope.launch {
            val result = repository.getActiveProjects()
            _activeProjects.value = result
        }
    }

    fun clockIn(
        userId: String,
        projectId: String,
        date: Date,
        clockInTime: Date,
        clockOutTime: Date?,
        imageUrlIn: String,
        imageUrlOut: String?,
        description: String,
        status: String = "pending",
        workHours: Int = 0,
        workHoursOvertime: Int = 0
    ) {
        viewModelScope.launch {
            try {
                val result = repository.attendance(
                    userId,
                    projectId,
                    date,
                    clockInTime,
                    clockOutTime,
                    imageUrlIn,
                    imageUrlOut,
                    description,
                    status,
                    workHours,
                    workHoursOvertime
                )
            } catch (e: Exception) {
                _attendanceResult.postValue(Result.failure(e))
            }
        }
    }


}