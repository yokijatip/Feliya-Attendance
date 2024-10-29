package com.gity.feliyaattendance.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Attendance(
    var attendanceId: String = "",
    var userId: String = "",
    var projectId: String = "",
    var date: Timestamp? = null,
    var clockInTime: Timestamp? = null,
    var clockOutTime: Timestamp? = null,
    var workProofIn: String = "",
    var workProofOut: String = "",
    var workDescription: String = "",
    var status: String = "",
    var workHours: Int? = null,
    var overtimeHours: Int? = null,
    var totalHours: Int? = null,
) : Parcelable
