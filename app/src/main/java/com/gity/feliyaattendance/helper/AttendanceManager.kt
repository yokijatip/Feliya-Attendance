package com.gity.feliyaattendance.helper

import android.content.Context
import com.gity.feliyaattendance.data.local.AttendanceDataStoreManager
import com.gity.feliyaattendance.data.local.ProjectDataStoreManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AttendanceManager(
    private val context: Context,
    private val dataStore: AttendanceDataStoreManager,
    private val projectDataStore: ProjectDataStoreManager,
    private val firestore: FirebaseFirestore
) {

    suspend fun clockIn(
        userId: String,
        projectId: String,
        date: Timestamp,
        clockIn: Timestamp,
        imageUrlIn: String
    ) {
        dataStore.saveClockInData(userId, projectId, date, clockIn, imageUrlIn)
    }

    suspend fun clockOut(
        clockOut: Timestamp,
        imageUrlOut: String,
        status: String,
        workMinutes: Int,
        overtimeMinutes: Int,
        totalMinutes: Int,
        description: String,
    ) {
        dataStore.saveClockOutData(
            clockOut,
            imageUrlOut,
            status,
            workMinutes,
            overtimeMinutes,
            totalMinutes,
            description
        )
        uploadAttendanceData()
    }

    private suspend fun uploadAttendanceData() {
        val userId = dataStore.userId.first() ?: throw IllegalStateException("User ID not found")
        val projectId =
            dataStore.projectId.first() ?: throw IllegalStateException("Project ID not found")
        val date = dataStore.date.first() ?: throw IllegalStateException("Date not found")
        val clockIn =
            dataStore.clockIn.first() ?: throw IllegalStateException("Clock in time not found")
        val clockOut =
            dataStore.clockOut.first() ?: throw IllegalStateException("Clock out time not found")
        val imageUrlIn = dataStore.imageUrlIn.first()
            ?: throw IllegalStateException("Clock in image URL not found")
        val imageUrlOut = dataStore.imageUrlOut.first()
            ?: throw IllegalStateException("Clock out image URL not found")
        val description = dataStore.description.first() ?: ""
        val status = dataStore.status.first() ?: throw IllegalStateException("Status not found")
        val attendanceId = UUID.randomUUID().toString()
        val workMinutes = dataStore.workMinutes.first() ?: 0
        val overtimeMinutes = dataStore.overtimeMinutes.first() ?: 0
        val totalMinutes = dataStore.totalMinutes.first() ?: (workMinutes + overtimeMinutes)

        // Format waktu untuk tampilan yang lebih baik
        val workHoursFormatted = "${workMinutes / 60}:${workMinutes % 60}"
        val overtimeHoursFormatted = "${overtimeMinutes / 60}:${overtimeMinutes % 60}"
        val totalHoursFormatted = "${totalMinutes / 60}:${totalMinutes % 60}"

        val attendanceData = hashMapOf(
            "attendanceId" to attendanceId,
            "userId" to userId,
            "projectId" to projectId,
            "date" to date,
            "clockInTime" to clockIn,
            "clockOutTime" to clockOut,
            "workProofIn" to imageUrlIn,
            "workProofOut" to imageUrlOut,
            "workDescription" to description,
            "status" to status,
            "workMinutes" to workMinutes,
            "overtimeMinutes" to overtimeMinutes,
            "totalMinutes" to totalMinutes,
            "workHoursFormatted" to workHoursFormatted,
            "overtimeHoursFormatted" to overtimeHoursFormatted,
            "totalHoursFormatted" to totalHoursFormatted
        )

        try {
            firestore.collection("attendance")
                .add(attendanceData)
                .await()

            // Clear DataStore after successful upload
            dataStore.clearClockInOutData()
            projectDataStore.clearProjectData()
        } catch (e: Exception) {
            // Handle upload error
            throw AttendanceUploadException("Failed to upload attendance data", e)
        }
    }


    class AttendanceUploadException(message: String, cause: Throwable? = null) :
        Exception(message, cause)
}


