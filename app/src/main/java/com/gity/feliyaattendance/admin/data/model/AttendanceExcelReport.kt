package com.gity.feliyaattendance.admin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttendanceExcelReport(
    var date: Timestamp?,
    var clockInTime: Timestamp?,
    var clockOutTime: Timestamp?,
    var workHours: String = "",
    var overtimeHours: String = "",
    var totalHours: String = "",
    var workDescription: String = "",
    var status: String = "",
    var projectName: String = "",
    var projectId: String = "",
) : Parcelable
