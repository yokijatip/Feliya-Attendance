package com.gity.feliyaattendance.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
    suspend fun registerUser(email: String, password: String, name: String, role: String): Result<Unit> {
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


}