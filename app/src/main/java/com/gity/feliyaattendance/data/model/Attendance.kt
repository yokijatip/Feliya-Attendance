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
    var workMinutes: Int = 0,
    var overtimeMinutes: Int = 0,
    var totalMinutes: Int = 0,
    var workHoursFormatted: String = "",
    var overtimeHoursFormatted: String = "",
    var totalHoursFormatted: String = ""
) : Parcelable {
    // Formatter functions untuk menampilkan waktu
    fun getFormattedWorkHours(): String {
        return formatMinutesToReadableTime(workMinutes)
    }

    fun getFormattedOvertimeHours(): String {
        return formatMinutesToReadableTime(overtimeMinutes)
    }

    fun getFormattedTotalHours(): String {
        return formatMinutesToReadableTime(totalMinutes)
    }

    private fun formatMinutesToReadableTime(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return when {
            hours > 0 && remainingMinutes > 0 -> "$hours jam $remainingMinutes menit"
            hours > 0 -> "$hours jam"
            remainingMinutes > 0 -> "$remainingMinutes menit"
            else -> "0 menit"
        }
    }

    // Function untuk mendapatkan durasi kerja dari clockIn dan clockOut
    fun calculateWorkDuration(): String {
        if (clockInTime == null || clockOutTime == null) return "-"

        val durationMillis = clockOutTime!!.toDate().time - clockInTime!!.toDate().time
        val minutes = (durationMillis / (1000 * 60)).toInt()

        return formatMinutesToReadableTime(minutes)
    }
}
