package com.gity.feliyaattendance.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
    suspend fun registerUser(email: String, password: String): Result<Unit> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("User ID not found"))
            val user = hashMapOf(
                "email" to email,
                "role" to "worker" // Default Role
            )
            firebaseFirestore.collection("users").document(userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}