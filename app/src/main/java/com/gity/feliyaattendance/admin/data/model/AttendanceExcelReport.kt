package com.gity.feliyaattendance.admin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttendanceExcelReport(
    val date: Timestamp?,
    val clockInTime: Timestamp?,
    val clockOutTime: Timestamp?,
    val workHours: Double,
    val overtimeHours: Double,
    val totalHours: Double,
    val workDescription: String,
    val projectId: String
) : Parcelable
