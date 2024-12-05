package com.gity.feliyaattendance.helper

import android.content.Context
import android.util.Log
import com.gity.feliyaattendance.data.local.AttendanceDataStoreManager
import com.gity.feliyaattendance.data.local.ProjectDataStoreManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Calendar
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
            dataStore.clearClockOutData()
            projectDataStore.clearProjectData()
        } catch (e: Exception) {
            // Handle upload error
            throw AttendanceUploadException("Failed to upload attendance data", e)
        }
    }

    //    Update Attendance Function
    suspend fun updateAttendance(attendanceId: String, updateData: Map<String, Any>) {
        try {
            // Validasi input
            require(attendanceId.isNotBlank()) { "Attendance ID cannot be empty" }
            require(updateData.isNotEmpty()) { "Update data cannot be empty" }

            // Daftar field yang diizinkan untuk diupdate
            val allowedFields = setOf(
                "status",
                "workDescription",
                "workMinutes",
                "overtimeMinutes",
                "totalMinutes",
                "workHoursFormatted",
                "overtimeHoursFormatted",
                "totalHoursFormatted"
            )

            // Filter hanya field yang diizinkan
            val filteredUpdateData = updateData.filterKeys { it in allowedFields }

            // Tambahkan perhitungan ulang jam jika ada perubahan menit
            val updatedData = if (filteredUpdateData.containsKey("workMinutes") ||
                filteredUpdateData.containsKey("overtimeMinutes")
            ) {
                val workMinutes = filteredUpdateData["workMinutes"] as? Int
                    ?: (firestore.collection("attendance")
                        .document(attendanceId)
                        .get()
                        .await()
                        .getLong("workMinutes")?.toInt() ?: 0)

                val overtimeMinutes = filteredUpdateData["overtimeMinutes"] as? Int
                    ?: (firestore.collection("attendance")
                        .document(attendanceId)
                        .get()
                        .await()
                        .getLong("overtimeMinutes")?.toInt() ?: 0)

                val totalMinutes = workMinutes + overtimeMinutes

                filteredUpdateData.toMutableMap().apply {
                    put("workHoursFormatted", "${workMinutes / 60}:${workMinutes % 60}")
                    put("overtimeHoursFormatted", "${overtimeMinutes / 60}:${overtimeMinutes % 60}")
                    put("totalMinutes", totalMinutes)
                    put("totalHoursFormatted", "${totalMinutes / 60}:${totalMinutes % 60}")
                }
            } else {
                filteredUpdateData
            }

            // Lakukan update di Firestore
            firestore.collection("attendance")
                .document(attendanceId)
                .update(updatedData)
                .await()

        } catch (e: Exception) {
            // Log error atau tangani kesalahan
            throw AttendanceUpdateException("Gagal memperbarui data absensi", e)
        }
    }

    // New method in AttendanceManager to check total working hours for the day
    suspend fun getTotalWorkingHoursForToday(userId: String): Int {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val todayTimestamp = Timestamp(today)

        return try {
            val querySnapshot = firestore.collection("attendance")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", todayTimestamp)
                .get()
                .await()

            // Sum up total minutes for today's attendance records
            querySnapshot.documents.sumOf { document ->
                document.getLong("totalMinutes")?.toInt() ?: 0
            }
        } catch (e: Exception) {
            Log.e("AttendanceManager", "Error fetching today's working hours", e)
            0
        }
    }

//    // Contoh penggunaan
//    suspend fun exampleUpdateUsage() {
//        try {
//            // Update status absensi
//            attendanceManager.updateAttendance(
//                attendanceId = "1f49e729-2340-4ab2-8e08-a27a9bfff154",
//                updateData = mapOf(
//                    "status" to "approved",
//                    "workDescription" to "Pekerjaan selesai dengan baik"
//                )
//            )
//
//            // Update jam kerja
//            attendanceManager.updateAttendance(
//                attendanceId = "1f49e729-2340-4ab2-8e08-a27a9bfff154",
//                updateData = mapOf(
//                    "workMinutes" to 120,  // 2 jam
//                    "overtimeMinutes" to 30  // 30 menit
//                )
//            )
//        } catch (e: AttendanceUpdateException) {
//            // Tangani error
//            println("Gagal update: ${e.message}")
//        }
//    }

    class AttendanceUpdateException(message: String, cause: Throwable? = null) :
        Exception(message, cause)


    class AttendanceUploadException(message: String, cause: Throwable? = null) :
        Exception(message, cause)
}


