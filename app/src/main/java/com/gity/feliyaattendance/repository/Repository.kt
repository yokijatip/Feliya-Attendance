package com.gity.feliyaattendance.repository

import com.gity.feliyaattendance.data.model.Project
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import kotlinx.coroutines.tasks.await

import java.util.UUID

class Repository(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) {
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
        email: String,
        password: String,
        name: String,
        role: String
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
            val roles = snapshot.get("name") as? List<String>
                ?: return Result.failure(Exception("Roles not found"))
            Result.success(roles)
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

    // Fungsi untuk menambahkan project baru
    suspend fun addProject(
        projectName: String,
        location: String,
        startDate: Date,
        endDate: Date,
        status: String,
        description: String
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
                "createdAt" to FieldValue.serverTimestamp()  // Menyimpan waktu pembuatan
            )

            firebaseFirestore.collection("projects").document(projectId).set(project).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveProjects(): Result<List<Project>> {
        return try {
            val snapshot = firebaseFirestore.collection("projects")
                .whereEqualTo("status", "Active") // Mengambil project yang statusnya "Active"
                .get()
                .await()

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

            // Siapkan data attendance
            val attendanceData = hashMapOf(
                "user_id" to userId,
                "project_id" to projectId,
                "date" to date,
                "clock_in_time" to clockInTime,
                "clock_out_time" to clockOutTime, // Nullable
                "work_proof_in" to imageUrlIn,
                "work_proof_out" to imageUrlOut, // Nullable
                "work_description" to description,
                "status" to status,
                "work_hours" to workHours,
                "overtime_hours" to workHoursOvertime
            )
            firebaseFirestore.collection("attendance")
                .add(attendanceData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}