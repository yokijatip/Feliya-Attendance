package com.gity.feliyaattendance.ui.main.attendance.edit

import android.util.Log
import androidx.lifecycle.ViewModel
import com.gity.feliyaattendance.helper.AttendanceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditAttendanceViewModel(private val attendanceManager: AttendanceManager) : ViewModel() {

    private val _workMinutes = MutableStateFlow(0)
    val workMinutes: StateFlow<Int> = _workMinutes.asStateFlow()

    private val _overtimeMinutes = MutableStateFlow(0)
    val overtimeMinutes: StateFlow<Int> = _overtimeMinutes.asStateFlow()

    private val _workDescription = MutableStateFlow("")
    val workDescription: StateFlow<String> = _workDescription.asStateFlow()

    // Update work minutes directly with total minutes
    fun updateWorkMinutes(minutes: Int) {
        _workMinutes.value = minutes
    }

    // Update overtime minutes directly with total minutes
    fun updateOvertimeMinutes(minutes: Int) {
        _overtimeMinutes.value = minutes
    }

    fun workDescription(workDescription: String) {
        _workDescription.value = workDescription
    }

    // Function to update attendance hours
    suspend fun updateAttendanceHours(attendanceId: String?) {
        if (attendanceId.isNullOrBlank()) {
            Log.e("AttendanceUpdate", "Attendance ID is null or empty")
            return
        }

        try {
            attendanceManager.updateAttendance(
                attendanceId = attendanceId,
                updateData = mapOf(
                    "workMinutes" to workMinutes.value,
                    "overtimeMinutes" to overtimeMinutes.value,
                    // Total minutes will be calculated automatically in the updateAttendance function
                )
            )
        } catch (e: Exception) {
            Log.e("AttendanceUpdate", "Failed to update work hours", e)
        }
    }

    suspend fun updateWorkDescription(attendanceId: String?, workDescription: String) {
        if (attendanceId.isNullOrBlank()) {
            Log.e("AttendanceUpdate", "Attendance ID is null or empty")
            return
        }

        try {
            attendanceManager.updateAttendance(
                attendanceId = attendanceId,
                updateData = mapOf(
                    "workDescription" to workDescription
                )
            )
        } catch (e: Exception) {
            Log.e("AttendanceUpdate", "Failed to update Work Description", e)
        }
    }

}