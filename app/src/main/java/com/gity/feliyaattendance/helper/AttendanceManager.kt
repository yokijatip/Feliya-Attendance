package com.gity.feliyaattendance.helper

import android.content.Context
import com.gity.feliyaattendance.data.local.AttendanceDataStoreManager
import com.gity.feliyaattendance.data.local.ProjectDataStoreManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Date

class AttendanceManager(
    private val context: Context,
    private val dataStore: AttendanceDataStoreManager,
    private val projectDataStore: ProjectDataStoreManager,
    private val firestore: FirebaseFirestore
) {

    suspend fun clockIn(
        userId: String,
        projectId: String,
        date: Date,
        clockIn: Timestamp,
        imageUrlIn: String
    ) {
        dataStore.saveClockInData(userId, projectId, date, clockIn, imageUrlIn)
    }

    suspend fun clockOut(
        clockOut: Timestamp,
        imageUrlOut: String,
        status: String,
        workHours: Int,
        workHoursOvertime: Int,
        description: String
    ) {
        dataStore.saveClockOutData(clockOut, imageUrlOut, status, workHours, workHoursOvertime, description)
        uploadAttendanceData()
    }

    private suspend fun uploadAttendanceData() {
        val userId = dataStore.userId.first() ?: throw IllegalStateException("User ID not found")
        val projectId = dataStore.projectId.first() ?: throw IllegalStateException("Project ID not found")
        val date = dataStore.date.first() ?: throw IllegalStateException("Date not found")
        val clockIn = dataStore.clockIn.first() ?: throw IllegalStateException("Clock in time not found")
        val clockOut = dataStore.clockOut.first() ?: throw IllegalStateException("Clock out time not found")
        val imageUrlIn = dataStore.imageUrlIn.first() ?: throw IllegalStateException("Clock in image URL not found")
        val imageUrlOut = dataStore.imageUrlOut.first() ?: throw IllegalStateException("Clock out image URL not found")
        val description = dataStore.description.first() ?: ""
        val status = dataStore.status.first() ?: throw IllegalStateException("Status not found")
        val workHours = dataStore.workHours.first() ?: 0
        val workHoursOvertime = dataStore.workHoursOvertime.first() ?: 0

        val attendanceData = hashMapOf(
            "user_id" to userId,
            "project_id" to projectId,
            "date" to date,
            "clock_in_time" to clockIn,
            "clock_out_time" to clockOut,
            "work_proof_in" to imageUrlIn,
            "work_proof_out" to imageUrlOut,
            "work_description" to description,
            "status" to status,
            "work_hours" to workHours,
            "overtime_hours" to workHoursOvertime
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

    suspend fun getClockInData(): ClockInData? {
        return try {
            ClockInData(
                userId = dataStore.userId.first() ?: return null,
                projectId = dataStore.projectId.first() ?: return null,
                date = dataStore.date.first() ?: return null,
                clockIn = dataStore.clockIn.first() ?: return null,
                imageUrlIn = dataStore.imageUrlIn.first() ?: return null
            )
        } catch (e: Exception) {
            null
        }
    }

    data class ClockInData(
        val userId: String,
        val projectId: String,
        val date: Date,
        val clockIn: Timestamp,
        val imageUrlIn: String
    )

    class AttendanceUploadException(message: String, cause: Throwable? = null) : Exception(message, cause)
}


