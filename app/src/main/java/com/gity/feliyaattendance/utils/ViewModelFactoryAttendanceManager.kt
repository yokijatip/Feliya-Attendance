package com.gity.feliyaattendance.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.helper.AttendanceManager
import com.gity.feliyaattendance.ui.main.attendance.edit.EditAttendanceViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactoryAttendanceManager(private val attendanceManager: AttendanceManager) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditAttendanceViewModel::class.java)) {
            return EditAttendanceViewModel(attendanceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}