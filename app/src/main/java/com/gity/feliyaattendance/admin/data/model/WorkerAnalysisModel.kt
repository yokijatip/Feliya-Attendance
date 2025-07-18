package com.gity.feliyaattendance.admin.data.model

/**
 * Data models untuk Worker Performance Analysis
 */

data class WorkerPerformanceData(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val workerId: String = "",
    val attendanceRate: Double = 0.0,
    val avgWorkHours: Double = 0.0,
    val punctualityScore: Double = 0.0,
    val consistencyScore: Double = 0.0,
    val totalRecords: Int = 0,
    val cluster: Int = -1,
    val performanceLabel: String = "",
    val confidence: Double = 0.0
)

data class AttendanceRecord(
    val attendanceId: String = "",
    val userId: String = "",
    val date: String = "",
    val clockInTime: String = "",
    val clockOutTime: String = "",
    val status: String = "",
    val workMinutes: Int = 0,
    val overtimeMinutes: Int = 0,
    val totalMinutes: Int = 0,
    val workDescription: String = ""
)

data class UserData(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val workerId: String = "",
    val role: String = "",
    val status: String = "",
    val created: String = ""
)

data class DateRange(
    val startDate: String,
    val endDate: String
)

data class AnalysisResult(
    val workers: List<WorkerPerformanceData>,
    val summary: PerformanceSummary
)

data class PerformanceSummary(
    val totalWorkers: Int,
    val highPerformers: Int,
    val mediumPerformers: Int,
    val lowPerformers: Int,
    val averageAttendanceRate: Double,
    val averageWorkHours: Double
)