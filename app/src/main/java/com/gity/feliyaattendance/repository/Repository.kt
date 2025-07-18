package com.gity.feliyaattendance.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import com.gity.feliyaattendance.admin.data.model.Announcement
import com.gity.feliyaattendance.admin.data.model.AttendanceExcelReport
import com.gity.feliyaattendance.admin.data.model.AttendanceRecord
import com.gity.feliyaattendance.admin.data.model.DateRange
import com.gity.feliyaattendance.admin.data.model.PerformanceSummary
import com.gity.feliyaattendance.admin.data.model.UserData
import com.gity.feliyaattendance.admin.data.model.Worker
import com.gity.feliyaattendance.admin.data.model.WorkerPerformanceData
import com.gity.feliyaattendance.data.model.Attendance
import com.gity.feliyaattendance.data.model.AttendanceDetail
import com.gity.feliyaattendance.data.model.Project
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class Repository(
    private val firebaseAuth: FirebaseAuth, private val firebaseFirestore: FirebaseFirestore
) {

    /**
    +     * Worker Performance Analysis Methods
    +     * Implementasi K-means clustering untuk analisis performa worker
    +     */

    // Get workers data for analysis
    suspend fun getWorkersData(): Result<List<UserData>> {
        return try {
            val snapshot = firebaseFirestore.collection("users")
                .whereEqualTo("role", "worker")
                .whereEqualTo("status", "activated")
                .get()
                .await()

            val workers = snapshot.documents.mapNotNull { doc ->
                UserData(
                    userId = doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    workerId = doc.getString("workerId") ?: "",
                    role = doc.getString("role") ?: "",
                    status = doc.getString("status") ?: "",
                    created = doc.getTimestamp("created")?.toDate()?.toString() ?: ""
                )
            }

//            Logging Worker data
            Log.d("Repository", "Workers Data: $workers")

            Result.success(workers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get attendance data for analysis within date range
    suspend fun getAttendanceDataForAnalysis(dateRange: DateRange): Result<List<AttendanceRecord>> {
        return try {
            val startDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateRange.startDate)
            val endDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateRange.endDate)

            val snapshot = firebaseFirestore.collection("attendance")
                .whereGreaterThanOrEqualTo("date", startDate!!)
                .whereLessThanOrEqualTo("date", endDate!!)
                .whereEqualTo("status", "approved")
                .get()
                .await()

            val attendanceRecords = snapshot.documents.mapNotNull { doc ->
                val clockInTime = doc.getTimestamp("clockInTime")
                val clockOutTime = doc.getTimestamp("clockOutTime")

                AttendanceRecord(
                    attendanceId = doc.id,
                    userId = doc.getString("userId") ?: "",
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                        doc.getTimestamp("date")?.toDate() ?: Date()
                    ),
                    clockInTime = clockInTime?.toDate().toString(),
                    clockOutTime = clockOutTime?.toDate()?.toString() ?: "",
                    status = doc.getString("status") ?: "",
                    workMinutes = doc.getLong("workHours")?.toInt() ?: 0,
                    overtimeMinutes = doc.getLong("overtimeHours")?.toInt() ?: 0,
                    totalMinutes = (doc.getLong("workHours")?.toInt()
                        ?: 0) + (doc.getLong("overtimeHours")?.toInt() ?: 0),
                    workDescription = doc.getString("workDescription") ?: ""
                )
            }

            Result.success(attendanceRecords)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Process worker performance data for ML analysis
    fun processWorkerPerformanceData(
        workers: List<UserData>,
        attendanceRecords: List<AttendanceRecord>,
        dateRange: DateRange
    ): List<WorkerPerformanceData> {
        val workingDays = calculateWorkingDays(dateRange)

        return workers.map { worker ->
            val workerAttendance = attendanceRecords.filter { it.userId == worker.userId }

            WorkerPerformanceData(
                userId = worker.userId,
                name = worker.name,
                email = worker.email,
                workerId = worker.workerId,
                attendanceRate = calculateAttendanceRate(workerAttendance, workingDays),
                avgWorkHours = calculateAvgWorkHours(workerAttendance),
                punctualityScore = calculatePunctualityScore(workerAttendance),
                consistencyScore = calculateConsistencyScore(workerAttendance),
                totalRecords = workerAttendance.size
            )
        }
    }

    // Calculate attendance rate
    private fun calculateAttendanceRate(
        attendance: List<AttendanceRecord>,
        workingDays: Int
    ): Double {
        val approvedDays = attendance.size
        return if (workingDays > 0) {
            min((approvedDays.toDouble() / workingDays) * 100, 100.0)
        } else 0.0
    }

    // Calculate average work hours
    private fun calculateAvgWorkHours(attendance: List<AttendanceRecord>): Double {
        return if (attendance.isNotEmpty()) {
            attendance.map { it.workMinutes / 60.0 }.average()
        } else 0.0
    }

    // Calculate punctuality score
    private fun calculatePunctualityScore(attendance: List<AttendanceRecord>): Double {
        if (attendance.isEmpty()) return 0.0

        val punctualDays = attendance.count { record ->
            isPunctual(record.clockInTime)
        }

        return (punctualDays.toDouble() / attendance.size) * 100
    }

    // Calculate consistency score
    private fun calculateConsistencyScore(attendance: List<AttendanceRecord>): Double {
        if (attendance.size < 2) return 0.0

        val workHours = attendance.map { it.workMinutes / 60.0 }
        val mean = workHours.average()
        val variance = workHours.map { (it - mean) * (it - mean) }.average()
        val stdDev = kotlin.math.sqrt(variance)

        // Convert to consistency score (lower std_dev = higher consistency)
        val maxStd = 4.0 // Assume max std deviation of 4 hours
        return max(0.0, (maxStd - stdDev) / maxStd * 100)
    }

    // Check if worker was punctual
    private fun isPunctual(clockInTime: String): Boolean {
        return try {
            if (clockInTime.isBlank()) return false

            // Parse different time formats from Firebase
            // Format: "Fri Dec 13 07:49:32 GMT+07:00 2024" or similar
            val timePattern = "\\d{1,2}:\\d{2}:\\d{2}".toRegex()
            val timeMatch = timePattern.find(clockInTime) ?: return false

            val timePart = timeMatch.value
            val hour = timePart.split(":")[0].toInt()

            // Consider punctual if clocked in at 8 AM or earlier
            hour <= 8
        } catch (e: Exception) {
            false
        }
    }

    // Calculate working days in date range (excluding weekends)
    private fun calculateWorkingDays(dateRange: DateRange): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = sdf.parse(dateRange.startDate) ?: return 0
            val endDate = sdf.parse(dateRange.endDate) ?: return 0

            val calendar = Calendar.getInstance()
            calendar.time = startDate

            var workingDays = 0
            while (calendar.time <= endDate) {
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                // Monday = 2, Friday = 6 in Calendar
                if (dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY) {
                    workingDays++
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            workingDays
        } catch (e: Exception) {
            0
        }
    }

    // Generate performance summary
    fun generatePerformanceSummary(workers: List<WorkerPerformanceData>): PerformanceSummary {
        val highPerformers = workers.count { it.performanceLabel == "High Performer" }
        val mediumPerformers = workers.count { it.performanceLabel == "Medium Performer" }
        val lowPerformers = workers.count { it.performanceLabel == "Low Performer" }

        val avgAttendanceRate = if (workers.isNotEmpty()) {
            workers.map { it.attendanceRate }.average()
        } else 0.0

        val avgWorkHours = if (workers.isNotEmpty()) {
            workers.map { it.avgWorkHours }.average()
        } else 0.0

        return PerformanceSummary(
            totalWorkers = workers.size,
            highPerformers = highPerformers,
            mediumPerformers = mediumPerformers,
            lowPerformers = lowPerformers,
            averageAttendanceRate = avgAttendanceRate,
            averageWorkHours = avgWorkHours
        )
    }

    // Get workers by performance level for filtering
    fun getWorkersByPerformance(
        workers: List<WorkerPerformanceData>,
        performanceLevel: String
    ): List<WorkerPerformanceData> {
        return workers.filter { it.performanceLabel == performanceLevel }
    }


    //    Login
    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
//            Attempt Login
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId =
                result.user?.uid ?: return Result.failure(Exception("User ID is not found"))

//            Check Role in Firebase Firestore
            val snapshot = firebaseFirestore.collection("users").document(userId).get().await()
            val role =
                snapshot.getString("role") ?: return Result.failure(Exception("Role not found"))

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Register
    suspend fun registerUser(
        email: String, password: String, name: String, role: String
    ): Result<Unit> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("User ID not found"))

            //  Auto-generate workerId menggunakan UUI
            val workerId = UUID.randomUUID().toString()

            val user = hashMapOf(
                "email" to email,
                "name" to name,
                "role" to role,
                "workerId" to workerId,
                "created" to FieldValue.serverTimestamp(), //  Menyimpan Waktu saat user dibuat (timestamp)
                "status" to "pending",
                "profileImageUrl" to ""
            )
            firebaseFirestore.collection("users").document(userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Fetch Roles
    suspend fun fetchRoles(): Result<List<String>> {
        return try {
            val snapshot = firebaseFirestore.collection("roles").document("role").get().await()

            // Pendekatan 1: Menggunakan safe casting dengan konversi eksplisit
            val roles = when (val rolesData = snapshot.get("name")) {
                is List<*> -> rolesData.filterIsInstance<String>()
                else -> return Result.failure(Exception("Roles not found or invalid type"))
            }

            if (roles.isEmpty()) {
                Result.failure(Exception("No roles found"))
            } else {
                Result.success(roles)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Fetch Name
    suspend fun fetchName(): Result<String> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("User ID not found"))
            val snapshot = firebaseFirestore.collection("users").document(userId).get().await()

            if (snapshot.exists() && snapshot.contains("name")) {
                val name = snapshot.getString("name")
                Result.success(name ?: "Unknown Name")
            } else {
                Result.failure(Exception("Name field not found in document"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchEmail(): Result<String> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("User ID not found"))
            val snapshot = firebaseFirestore.collection("users").document(userId).get().await()

            if (snapshot.exists() && snapshot.contains("email")) {
                val email = snapshot.getString("email")
                Result.success(email ?: "Unknown Email")
            } else {
                Result.failure(Exception("Email field not found in document"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserImageProfile(): Result<String> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("User ID not found"))
            val snapshot = firebaseFirestore.collection("users").document(userId).get().await()

            if (snapshot.exists() && snapshot.contains("profileImageUrl")) {
                val email = snapshot.getString("profileImageUrl")
                Result.success(email ?: "Unknown Profile Image URL")
            } else {
                Result.failure(Exception("Image Profile URL field not found in document"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fungsi untuk menambahkan project baru
    suspend fun addProject(
        projectName: String,
        location: String,
        startDate: Date,
        endDate: Date,
        status: String,
        description: String,
        projectImage: String,
    ): Result<Unit> {
        return try {
            val projectId = UUID.randomUUID().toString()  // Generate projectId secara otomatis
            val userId = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("User ID not found"))

            val project = hashMapOf(
                "projectId" to projectId,
                "userId" to userId,  // ID admin yang menambahkan project
                "projectName" to projectName,
                "location" to location,
                "startDate" to startDate,
                "endDate" to endDate,
                "status" to status,
                "description" to description,
                "createdAt" to FieldValue.serverTimestamp(),  // Menyimpan waktu pembuatan
                "projectImage" to projectImage,
            )

            firebaseFirestore.collection("projects").document(projectId).set(project).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Attendance
    suspend fun attendance(
        userId: String,
        projectId: String,
        date: Date,
        clockInTime: Timestamp,
        clockOutTime: Timestamp?,
        imageUrlIn: String,
        imageUrlOut: String?,
        description: String,
        status: String?,
        workHours: Int = 0,
        workHoursOvertime: Int = 0
    ): Result<Unit> {
        return try {
            val attendanceId = UUID.randomUUID().toString()
            // Siapkan data attendance
            val attendanceData = hashMapOf(
                "attendanceId" to attendanceId,
                "userId" to userId,
                "projectId" to projectId,
                "date" to date,
                "clockInTime" to clockInTime,
                "clockOutTime" to clockOutTime, // Nullable
                "workProofIn" to imageUrlIn,
                "workProofOut" to imageUrlOut, // Nullable
                "workDescription" to description,
                "status" to status,
                "workHours" to workHours,
                "overtimeHours" to workHoursOvertime
            )
            firebaseFirestore.collection("attendance").add(attendanceData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Get Active Projects
    suspend fun getActiveProjects(): Result<List<Project>> {
        return try {
            val snapshot = firebaseFirestore.collection("projects")
                .whereEqualTo("status", "Active") // Mengambil project yang statusnya "Active"
                .get().await()

            val activeProjects = snapshot.documents.mapNotNull { document ->
                // Mendapatkan data dari setiap document
                document.toObject(Project::class.java)?.apply {
                    projectId = document.id // Menyimpan ID document ke dalam model
                }
            }

            Result.success(activeProjects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mengambil jumlah pekerja (worker)
    suspend fun getWorkersCount(): Result<Int> {
        return try {
            val snapshot = firebaseFirestore.collection("users").whereEqualTo(
                "role", "worker"
            ) // Asumsi bahwa role pekerja disimpan sebagai "worker"
                .get().await()

            // Mengembalikan jumlah pekerja
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Get Project Count
    suspend fun getProjectCount(): Result<Int> {
        return try {
            val snapshot = firebaseFirestore.collection("projects").get().await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get All Project
    suspend fun getAllProject(
        orderBy: String = "asc",
        filterStatus: String? = null
    ): Result<List<Project>> {
        return try {
            // Buat query dasar
            val query = firebaseFirestore.collection("projects")
                .orderBy(
                    "projectName",
                    if (orderBy == "asc") Query.Direction.ASCENDING else Query.Direction.DESCENDING
                )

            // Ambil snapshot dari query
            val snapshot = query.get().await()

            // Peta dokumen menjadi objek Project
            val projects = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Project::class.java)?.apply {
                    projectId = documentSnapshot.id
                }
            }

            // Jika filterStatus tidak null, filter daftar proyek
            val filteredProjects = if (filterStatus != null) {
                projects.filter { project ->
                    when (filterStatus) {
                        "Active" -> project.status == "Active"
                        "Inactive" -> project.status == "Inactive"
                        "Completed" -> project.status == "Completed"
                        else -> true // Jika filterStatus tidak cocok, tidak ada filter yang diterapkan
                    }
                }
            } else {
                projects // Jika tidak ada filter, kembalikan semua proyek
            }

            Result.success(filteredProjects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDetailProject(projectId: String): Result<Project> {
        return try {
            // Get project document from Firestore using the projectId
            val snapshot = firebaseFirestore.collection("projects")
                .document(projectId)  // Fix: Use projectId directly instead of string "projectId"
                .get()
                .await()

            if (snapshot.exists()) {
                // Convert document to Project object and set the projectId
                val project = snapshot.toObject(Project::class.java)?.apply {
                    this.projectId = snapshot.id
                }

                if (project != null) {
                    Result.success(project)
                } else {
                    Result.failure(Exception("Failed to parse project data"))
                }
            } else {
                Result.failure(Exception("Project not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProjectStatus(projectId: String, newStatus: String): Result<Unit> {
        return try {
            // Update the project status in Firestore
            firebaseFirestore.collection("projects")
                .document(projectId)
                .update("status", newStatus)
                .await() // Await for the update to complete

            Result.success(Unit) // Return success result
        } catch (e: Exception) {
            Result.failure(e) // Return failure result
        }
    }

    //    Get Pending Count Attendance
    suspend fun getAttendancePending(): Result<Int> {
        return try {
            val snapshot =
                firebaseFirestore.collection("attendance").whereEqualTo("status", "pending").get()
                    .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Get list Attendance / Activity
    suspend fun getAttendanceByUserId(userId: String): Result<List<Attendance>> {
        return try {
            val snapshot =
                firebaseFirestore.collection("attendance")
                    .whereEqualTo("userId", userId)
                    .limit(10)
                    .get()
                    .await()

            val attendanceList = snapshot.documents.mapNotNull { document ->
                document.toObject(Attendance::class.java)?.apply {
                    attendanceId = document.id
                }
            }

            Result.success(attendanceList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Get attendance pending
    suspend fun getAllAttendancePending(): Result<List<Attendance>> {
        return try {
            val snapshot = firebaseFirestore.collection("attendance")
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val attendanceStatus = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Attendance::class.java)?.apply {
                    attendanceId = documentSnapshot.id
                }
            }

            Result.success(attendanceStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Get attendance approved
    suspend fun getAllAttendanceApproved(): Result<List<Attendance>> {
        return try {
            val snapshot =
                firebaseFirestore.collection("attendance").whereEqualTo("status", "approved").get()
                    .await()

            val attendanceStatus = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Attendance::class.java)?.apply {
                    attendanceId = documentSnapshot.id
                }
            }

            Result.success(attendanceStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    History Attendance List Approved filter by Month and Year
    suspend fun getApprovedAttendanceByMonthAndYear(
        userId: String,
        year: Int,
        month: Int
    ): Result<List<Attendance>> {
        return try {
            val startOfMonth = Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfMonth = Calendar.getInstance().apply {
                set(year, month, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MILLISECOND, -1) // Ambil hari terakhir bulan ini
            }.time

            val snapshot = firebaseFirestore.collection("attendance")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "approved")
                .whereGreaterThanOrEqualTo("date", startOfMonth)
                .whereLessThanOrEqualTo("date", endOfMonth)
                .get()
                .await()

            val attendanceList = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Attendance::class.java)?.apply {
                    attendanceId = documentSnapshot.id
                }
            }

            Result.success(attendanceList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    History Attendance List Approved filter by Month and Year
    suspend fun getRejectedAttendanceByMonthAndYear(
        userId: String,
        year: Int,
        month: Int
    ): Result<List<Attendance>> {
        return try {
            val startOfMonth = Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfMonth = Calendar.getInstance().apply {
                set(year, month, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MILLISECOND, -1) // Ambil hari terakhir bulan ini
            }.time

            val snapshot = firebaseFirestore.collection("attendance")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "rejected")
                .whereGreaterThanOrEqualTo("date", startOfMonth)
                .whereLessThanOrEqualTo("date", endOfMonth)
                .get()
                .await()

            val attendanceList = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Attendance::class.java)?.apply {
                    attendanceId = documentSnapshot.id
                }
            }

            Result.success(attendanceList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    History Attendance List Approved filter by Month and Year
    suspend fun getPendingAttendanceByMonthAndYear(
        userId: String,
        year: Int,
        month: Int
    ): Result<List<Attendance>> {
        return try {
            val startOfMonth = Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfMonth = Calendar.getInstance().apply {
                set(year, month, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MILLISECOND, -1) // Ambil hari terakhir bulan ini
            }.time

            val snapshot = firebaseFirestore.collection("attendance")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "pending")
                .whereGreaterThanOrEqualTo("date", startOfMonth)
                .whereLessThanOrEqualTo("date", endOfMonth)
                .get()
                .await()

            val attendanceList = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Attendance::class.java)?.apply {
                    attendanceId = documentSnapshot.id
                }
            }

            Result.success(attendanceList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    History Attendance List Approved filter by Month and Year
    suspend fun getAllAttendanceByMonthAndYear(
        userId: String,
        year: Int,
        month: Int
    ): Result<List<Attendance>> {
        return try {
            val startOfMonth = Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfMonth = Calendar.getInstance().apply {
                set(year, month, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MILLISECOND, -1) // Ambil hari terakhir bulan ini
            }.time

            val snapshot = firebaseFirestore.collection("attendance")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startOfMonth)
                .whereLessThanOrEqualTo("date", endOfMonth)
                .get()
                .await()

            val attendanceList = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Attendance::class.java)?.apply {
                    attendanceId = documentSnapshot.id
                }
            }

            Result.success(attendanceList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Get attendance rejected
    suspend fun getAllAttendanceRejected(): Result<List<Attendance>> {
        return try {
            val snapshot =
                firebaseFirestore.collection("attendance").whereEqualTo("status", "rejected").get()
                    .await()

            val attendanceStatus = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Attendance::class.java)?.apply {
                    attendanceId = documentSnapshot.id
                }
            }

            Result.success(attendanceStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Change attendance status berdasarkan attendanceId
    suspend fun updateAttendanceStatus(attendanceId: String, newStatus: String): Result<Unit> {
        return try {
            firebaseFirestore.collection("attendance").document(attendanceId)
                .update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Change status Account
    suspend fun updateStatusAccount(userId: String, newStatus: String): Result<Unit> {
        return try {
            firebaseFirestore.collection("users").document(userId)
                .update("status", newStatus).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Get Detail Attendance
    suspend fun getAttendanceDetail(attendanceId: String): Result<AttendanceDetail> {
        return try {
            val attendanceSnapshot =
                firebaseFirestore.collection("attendance").document(attendanceId).get().await()
            val attendance =
                attendanceSnapshot.toObject(Attendance::class.java) ?: return Result.failure(
                    Exception("Attendance not found")
                )

//            Mengambil data worker berdasarkan userId dari attendance
            val userSnapshot =
                firebaseFirestore.collection("users").document(attendance.userId).get().await()
            val workerName = userSnapshot.getString("name") ?: "Unkown User"

            val projectSnapshot =
                firebaseFirestore.collection("projects").document(attendance.projectId).get()
                    .await()

            val project = projectSnapshot.toObject(Project::class.java) ?: return Result.failure(
                Exception("Project not found")
            )

            val attendanceDetail = AttendanceDetail(
                attendance = attendance, workerName = workerName, projectName = project
            )

            Result.success(attendanceDetail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Get Worker List
    suspend fun getWorkerList(): Result<List<Worker>> {
        return try {
            val snapshot =
                firebaseFirestore.collection("users").whereEqualTo("role", "worker").orderBy("name")
                    .get().await()

            val workerList = snapshot.documents.mapNotNull { document ->
                document.toObject(Worker::class.java)?.apply {
                    id = document.id
                }
            }

            Result.success(workerList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Get Worker Detail
    suspend fun getWorkerDetail(workerId: String): Result<Worker> {
        return try {
            val snapshot = firebaseFirestore.collection("users").document(workerId).get().await()

            val workerDetail = snapshot.toObject(Worker::class.java)?.apply {
                id = snapshot.id
            }

            if (workerDetail != null) {
                Result.success(workerDetail)
            } else {
                Result.failure(Exception("Worker Detail Not Found!"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    suspend fun getMonthlyDashboard(userId: String): Result<MonthlyDashboard> {
//        return try {
//            // Set start of month
//            val startOfMonth = Calendar.getInstance().apply {
//                set(Calendar.DAY_OF_MONTH, 1)
//                set(Calendar.HOUR_OF_DAY, 0)
//                set(Calendar.MINUTE, 0)
//                set(Calendar.SECOND, 0)
//                set(Calendar.MILLISECOND, 0)
//            }.time
//
//            // Set end of month
//            val endOfMonth = Calendar.getInstance().apply {
//                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
//                set(Calendar.HOUR_OF_DAY, 23)
//                set(Calendar.MINUTE, 59)
//                set(Calendar.SECOND, 59)
//                set(Calendar.MILLISECOND, 999)
//            }.time
//
//            // Log untuk debugging
//            Log.d("Repository", "Fetching attendance for userId: $userId")
//            Log.d("Repository", "Start date: $startOfMonth")
//            Log.d("Repository", "End date: $endOfMonth")
//
//            val snapshot = firebaseFirestore.collection("attendance").whereEqualTo("userId", userId)
//                .whereGreaterThanOrEqualTo("date", startOfMonth)
//                .whereLessThanOrEqualTo("date", endOfMonth).get().await()
//
//            // Log jumlah dokumen yang ditemukan
//            Log.d("Repository", "Found ${snapshot.size()} documents")
//
//            val totalAttendance = snapshot.size()
//            var totalOvertimeHours = 0.0
//
//            snapshot.documents.forEach { document ->
////                // Log setiap dokumen untuk debugging
////                Log.d("Repository", "Document ID: ${document.id}")
////                Log.d("Repository", "Date: ${document.getTimestamp("date")}")
////                Log.d("Repository", "Overtime hours: ${document.getDouble("overtimeHours")}")
//
//                val overtimeHours = document.getDouble("overtimeHours") ?: 0.0
//                if (overtimeHours > 0) {
//                    totalOvertimeHours += overtimeHours
//                }
//            }
//
//            Log.d(
//                "Repository",
//                "Final counts - Attendance: $totalAttendance, Overtime: $totalOvertimeHours"
//            )
//
//            Result.success(
//                MonthlyDashboard(
//                    totalAttendance = totalAttendance, totalOvertimeHours = totalOvertimeHours
//                )
//            )
//        } catch (e: Exception) {
//            Log.e("Repository", "Error fetching monthly dashboard", e)
//            Result.failure(e)
//        }
//    }

    //    Generate Excel Report
    suspend fun generateExcelReport(
        userId: String, startTimestamp: Timestamp, endTimestamp: Timestamp
    ): Result<List<AttendanceExcelReport>> {
        return try {
            val snapshot = firebaseFirestore.collection("attendance")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startTimestamp)
                .whereLessThanOrEqualTo("date", endTimestamp)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            // Get all unique project IDs from the attendance records
            val projectIds = snapshot.documents.mapNotNull { it.getString("projectId") }.distinct()

            // Fetch all referenced projects in one batch
            val projectsSnapshot = projectIds.map { projectId ->
                firebaseFirestore.collection("projects").document(projectId).get().await()
            }

            // Create a map of projectId to projectName for quick lookup
            val projectNameMap = projectsSnapshot.associate { doc ->
                doc.id to (doc.getString("projectName") ?: "Unknown Project")
            }

            val attendanceReports = snapshot.documents.map { document ->
                val projectId = document.getString("projectId") ?: ""
                AttendanceExcelReport(
                    date = document.getTimestamp("date") ?: Timestamp.now(),
                    clockInTime = document.getTimestamp("clockInTime"),
                    clockOutTime = document.getTimestamp("clockOutTime"),
                    workHours = document.getString("workHoursFormatted") ?: "",
                    overtimeHours = document.getString("overtimeHoursFormatted") ?: "",
                    totalHours = document.getString("totalHoursFormatted") ?: "",
                    workDescription = document.getString("workDescription") ?: "",
                    status = document.getString("status") ?: "",
                    projectId = projectId,
                    projectName = projectNameMap[projectId] ?: "Unknown Project"
                )
            }

            Result.success(attendanceReports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    Generate Excel File
    fun generateExcelFile(
        context: Context,
        userId: String,
        userName: String,
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        attendanceReports: List<AttendanceExcelReport>
    ): Result<File> {
        return try {
            // Buat workbook baru
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Laporan Absensi")

            // Format tanggal
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

            // Mendapatkan tahun dan bulan dari startTimestamp
            val year = yearFormat.format(startTimestamp.toDate())
            val month = monthFormat.format(startTimestamp.toDate())

            // Header
            val headerRow = sheet.createRow(0)
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.LAVENDER.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
                wrapText = true
                setFont(workbook.createFont().apply {
                    bold = true
                    color = IndexedColors.WHITE.index
                })
            }

            // Style untuk total
            val totalStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
                setFont(workbook.createFont().apply {
                    bold = true
                })
            }

            // Style untuk status
            val approvedStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.SEA_GREEN.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
                setFont(workbook.createFont().apply {
                    color = IndexedColors.WHITE.index
                })
            }

            val pendingStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            }

            val rejectedStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.RED.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
                setFont(workbook.createFont().apply {
                    color = IndexedColors.WHITE.index
                })
            }

            val headers = listOf(
                "Tanggal",
                "Jam Masuk",
                "Jam Keluar",
                "Jam Kerja",
                "Jam Lembur",
                "Total Jam Kerja",
                "Deskripsi Pekerjaan",
                "Status",
                "Nama Proyek"
            )

            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).apply {
                    setCellValue(header)
                    cellStyle = headerStyle
                }
            }

            // Isi data
            attendanceReports.forEachIndexed { index, report ->
                val row = sheet.createRow(index + 1)

                row.createCell(0).setCellValue(dateFormat.format(report.date?.toDate() ?: Date()))
                row.createCell(1)
                    .setCellValue(report.clockInTime?.let { dateTimeFormat.format(it.toDate()) }
                        ?: "")
                row.createCell(2)
                    .setCellValue(report.clockOutTime?.let { dateTimeFormat.format(it.toDate()) }
                        ?: "")
                row.createCell(3).setCellValue(report.workHours)
                row.createCell(4).setCellValue(report.overtimeHours)
                row.createCell(5).setCellValue(report.totalHours)
                row.createCell(6).setCellValue(report.workDescription)

                // Tambahkan kondisi warna untuk status
                val statusCell = row.createCell(7)
                statusCell.setCellValue(report.status)
                statusCell.cellStyle = when (report.status.lowercase()) {
                    "approved" -> approvedStyle
                    "pending" -> pendingStyle
                    "rejected" -> rejectedStyle
                    else -> workbook.createCellStyle() // Default style jika status tidak sesuai
                }

                row.createCell(8).setCellValue(report.projectName)
            }

            // Function untuk menghitung total menit dari format "HH:mm"
            fun calculateTotalMinutes(timeStr: String): Int {
                return try {
                    val parts = timeStr.trim().split(":")
                    if (parts.size == 2) {
                        val hours = parts[0].trim().toIntOrNull() ?: 0
                        val minutes = parts[1].trim().toIntOrNull() ?: 0
                        (hours * 60) + minutes
                    } else {
                        0
                    }
                } catch (e: Exception) {
                    0
                }
            }

            // Function untuk memformat menit ke format "HH:mm" dengan Locale yang eksplisit
            fun formatMinutesToTime(totalMinutes: Int): String {
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
            }

            // Hitung total untuk setiap jenis jam
            var totalWorkMinutes = 0
            var totalOvertimeMinutes = 0
            var totalAllMinutes = 0

            attendanceReports.forEach { report ->
                totalWorkMinutes += calculateTotalMinutes(report.workHours)
                totalOvertimeMinutes += calculateTotalMinutes(report.overtimeHours)
                totalAllMinutes += calculateTotalMinutes(report.totalHours)
            }

            // Tambahkan baris total
            val totalRow = sheet.createRow(attendanceReports.size + 1)

            // Buat cell "Total" di kolom pertama
            totalRow.createCell(0).apply {
                setCellValue("Total")
                cellStyle = totalStyle
            }

            // Set nilai total untuk setiap jenis jam
            totalRow.createCell(3).apply {
                setCellValue(formatMinutesToTime(totalWorkMinutes))
                cellStyle = totalStyle
            }

            totalRow.createCell(4).apply {
                setCellValue(formatMinutesToTime(totalOvertimeMinutes))
                cellStyle = totalStyle
            }

            totalRow.createCell(5).apply {
                setCellValue(formatMinutesToTime(totalAllMinutes))
                cellStyle = totalStyle
            }

            // Merge cells untuk label "Total"
            sheet.addMergedRegion(
                CellRangeAddress(
                    attendanceReports.size + 1, // first row (0-based)
                    attendanceReports.size + 1, // last row
                    0, // first column (0-based)
                    2  // last column
                )
            )

            // Auto size kolom
            headers.indices.forEach { sheet.setColumnWidth(it, 6000) }

            // Buat nama file
            val fileName =
                "Laporan_Absensi_${userName}_${dateFormat.format(startTimestamp.toDate())}_sampai_${
                    dateFormat.format(endTimestamp.toDate())
                }.xlsx"

            // Buat struktur folder
            val documentsPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val baseDirectory = File(documentsPath, "LaporanAbsensi")
            val yearDirectory = File(baseDirectory, year)
            val monthDirectory = File(yearDirectory, month)
            val userDirectory = File(monthDirectory, userName)

            // Buat folder secara berurutan
            baseDirectory.mkdirs()
            yearDirectory.mkdirs()
            monthDirectory.mkdirs()
            userDirectory.mkdirs()

            // Simpan file di folder user
            val file = File(userDirectory, fileName)

            FileOutputStream(file).use { fileOut ->
                workbook.write(fileOut)
            }

            workbook.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    //    Delete Worker Account
    suspend fun deleteWorker(workerId: String): Result<Unit> {
        return try {
            firebaseFirestore.collection("users").document(workerId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDetailAccount(userId: String): Result<Worker> {
        return try {
            // Mendapatkan referensi koleksi "workers" di Firestore
            val documentSnapshot =
                firebaseFirestore.collection("users").document(userId).get().await()

            if (documentSnapshot.exists()) {
                val worker = documentSnapshot.toObject(Worker::class.java)
                Result.success(worker ?: Worker())  // Jika worker null, kembalikan Worker kosong
            } else {
                Result.failure(Exception("Worker not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAttendance(attendanceId: String): Result<Unit> {
        return try {
            firebaseFirestore.collection("attendance").document(attendanceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDetailAnnouncement(announcementId: String): Result<Announcement> {
        return try {
            val documentSnapshot =
                firebaseFirestore.collection("announcement").document(announcementId).get().await()
            if (documentSnapshot.exists()) {
                val announcement = documentSnapshot.toObject(Announcement::class.java)
                Result.success(announcement ?: Announcement())
            } else {
                Result.failure(Exception("Announcement Not Found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(announcementId: String): Result<Unit> {
        return try {
            firebaseFirestore.collection("announcement").document(announcementId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAnnouncement(
        announcement: String,
        createdBy: String,
        createdByName: String,
        createdByEmail: String,
        imageAnnouncement: String,
        imageUser: String
    ): Result<Unit> {
        return try {
            val announcementId = UUID.randomUUID().toString()

            val announcementData = hashMapOf(
                "announcementId" to announcementId,
                "announcement" to announcement,
                "createdAt" to FieldValue.serverTimestamp(),
                "createdBy" to createdBy,
                "createdByName" to createdByName,
                "createdByEmail" to createdByEmail,
                "imageAnnouncement" to imageAnnouncement,
                "imageUser" to imageUser
            )

            firebaseFirestore.collection("announcement").add(announcementData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAnnouncementList(selectedDate: Date? = null): Result<List<Announcement>> {
        return try {
            // Use selected date or current date if not specified
            val calendar = Calendar.getInstance().apply {
                time = selectedDate ?: Date()
            }

            // Set to first day of the month at start of day
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.time

            // Set to last day of the month at end of day
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfMonth = calendar.time

            // Fetch announcements within the specified month
            val snapshot = firebaseFirestore.collection("announcement")
                .whereGreaterThanOrEqualTo("createdAt", startOfMonth)
                .whereLessThanOrEqualTo("createdAt", endOfMonth)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            // Map documents to Announcement objects
            val announcements = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Announcement::class.java)?.apply {
                    id = documentSnapshot.id
                }
            }

            Result.success(announcements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadUserProfileImage(imageUrl: String): Result<String> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: throw IllegalStateException("User not authenticated")

            firebaseFirestore.collection("users")
                .document(userId)
                .update("profileImageUrl", imageUrl)
                .await()

            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     *
     *
     * Batas Dev Code Baru
     *
     *
     * **/

    // Method to calculate monthly work hours for a specific worker
    suspend fun calculateMonthlyWorkHours(
        workerId: String,
        year: Int,
        month: Int
    ): Result<Pair<String, String>> {
        return try {
            // Fetch all attendance records for the given worker, year, and month
            val attendanceResult = getAllAttendanceByMonthAndYear(workerId, year, month)

            attendanceResult.map { attendanceList ->
                // Calculate total work minutes and overtime minutes
                var totalWorkMinutes = 0
                var totalOvertimeMinutes = 0

                attendanceList.forEach { attendance ->
                    totalWorkMinutes += attendance.workMinutes
                    totalOvertimeMinutes += attendance.overtimeMinutes
                }

                // Format hours
                val workHours = formatMinutesToHoursMinutes(totalWorkMinutes)
                val overtimeHours = formatMinutesToHoursMinutes(totalOvertimeMinutes)

                Pair(workHours, overtimeHours)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper method to format minutes to hours:minutes
    private fun formatMinutesToHoursMinutes(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}:${minutes.toString().padStart(2, '0')}"
    }

}