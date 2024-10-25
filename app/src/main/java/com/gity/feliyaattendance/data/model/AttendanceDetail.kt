package com.gity.feliyaattendance.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttendanceDetail(
    val attendance: Attendance,
    val workerName: String? = null,
    val projectName: Project,
) : Parcelable
